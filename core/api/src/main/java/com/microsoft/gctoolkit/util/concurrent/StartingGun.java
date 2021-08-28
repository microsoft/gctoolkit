package com.microsoft.gctoolkit.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A StartingGun for services, where we can wait for one particular service
 * to start up.
 * <p>
 * Instead of {@link java.util.concurrent.CountDownLatch}, we have a custom
 * concurrency utility called StartingGun, which is like a CountDownLatch with
 * a count of 1 and where the awaitUninterruptibly() method does not throw an
 * exception. Similarly to the CountDownLatch, this uses the
 * {@link AbstractQueuedSynchronizer} for the synchronization.
 *
 * @author Dr Heinz M. Kabutz
 */
public class StartingGun {
    private static final int UNUSED = 0;

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

        protected int tryAcquireShared(int unused) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int unused) {
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

    /**
     * Wait for the starting gun to fire, without propagating the
     * InterruptedException.
     */
    public void awaitUninterruptibly() {
        sync.acquireShared(UNUSED);
    }

    /**
     * Indicate that the service is ready for operation.
     */
    public void ready() {
        sync.releaseShared(UNUSED);
    }
}
