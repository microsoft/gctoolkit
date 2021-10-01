package com.microsoft.gctoolkit.vertx.internal.util.concurrent;

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
    private static final class Sync extends
            AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        protected int tryAcquireShared(int unused) {
            return (getState() == 1) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int unused) {
            setState(1);
            return true;
        }
    }

    private final Sync sync;

    /**
     * Constructs a {@code StartingGun}.
     */
    public StartingGun() {
        this.sync = new Sync();
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
