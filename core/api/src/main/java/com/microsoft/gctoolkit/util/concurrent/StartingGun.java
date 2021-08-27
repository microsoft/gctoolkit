package com.microsoft.gctoolkit.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class StartingGun {
    /**
     * Synchronization control For StartingGun.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (; ; ) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;

    /**
     * Constructs a {@code StartingGun}.
     */
    public StartingGun() {
        this.sync = new Sync(1);
    }

    public void awaitUninterruptibly() {
        sync.acquireShared(1);
    }

    public void ready() {
        sync.releaseShared(1);
    }
}
