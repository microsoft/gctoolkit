// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

final class BoundedLineSpliterator implements Spliterator<String> {

    private static final int NO_PENDING_CHARACTER = -2;

    private final BufferedReader reader;
    private final int maximumLineCharacters;
    private final Path path;
    private final String archiveEntry;
    private int pendingCharacter = NO_PENDING_CHARACTER;
    private boolean closed;

    BoundedLineSpliterator(
            Reader reader,
            int maximumLineCharacters,
            Path path,
            String archiveEntry) {
        this.reader = new BufferedReader(reader);
        this.maximumLineCharacters = maximumLineCharacters;
        this.path = path;
        this.archiveEntry = archiveEntry;
    }

    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        Objects.requireNonNull(action);
        if (closed) {
            return false;
        }

        StringBuilder line = new StringBuilder(Math.min(maximumLineCharacters, 8192));
        try {
            while (true) {
                int character = readCharacter();
                if (character == -1) {
                    close();
                    if (line.length() == 0) {
                        return false;
                    }
                    action.accept(line.toString());
                    return true;
                }
                if (character == '\n') {
                    action.accept(line.toString());
                    return true;
                }
                if (character == '\r') {
                    int following = readCharacter();
                    if (following != '\n' && following != -1) {
                        pendingCharacter = following;
                    }
                    action.accept(line.toString());
                    if (following == -1) {
                        close();
                    }
                    return true;
                }
                if (line.length() == maximumLineCharacters) {
                    throw new LogFileReadLimitExceededException(
                            LogFileReadLimitExceededException.LimitType.LINE_CHARACTERS,
                            path,
                            archiveEntry,
                            Integer.toString(maximumLineCharacters),
                            Integer.toString(maximumLineCharacters + 1));
                }
                line.append((char) character);
            }
        } catch (IOException exception) {
            closeAfterFailure(exception);
            throw new UncheckedIOException(exception);
        } catch (RuntimeException exception) {
            closeAfterFailure(exception);
            throw exception;
        }
    }

    private int readCharacter() throws IOException {
        if (pendingCharacter != NO_PENDING_CHARACTER) {
            int character = pendingCharacter;
            pendingCharacter = NO_PENDING_CHARACTER;
            return character;
        }
        return reader.read();
    }

    private void closeAfterFailure(Throwable failure) {
        try {
            close();
        } catch (IOException closeException) {
            failure.addSuppressed(closeException);
        }
    }

    void closeUnchecked() {
        try {
            close();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void close() throws IOException {
        if (!closed) {
            closed = true;
            reader.close();
        }
    }

    @Override
    public Spliterator<String> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }
}
