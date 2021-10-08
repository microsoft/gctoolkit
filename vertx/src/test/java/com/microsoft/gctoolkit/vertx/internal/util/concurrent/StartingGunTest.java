package com.microsoft.gctoolkit.vertx.internal.util.concurrent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StartingGunTest {

  private static final int PAUSE = 300;

  @Test
  public void testStartingGunNoInterruptions() throws InterruptedException {
    StartingGun sg = new StartingGun();
    Thread[] threads = startWaitingThreads(sg);

    Thread.sleep(PAUSE);

    allThreadsAlive(threads);

    sg.ready();

    allThreadsDead(threads);
  }

  private Thread[] startWaitingThreads(StartingGun sg) {
    Thread[] threads = new Thread[10];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(sg::awaitUninterruptibly);
      threads[i].start();
    }
    return threads;
  }

  @Test
  public void testAwaitAfterReady() throws InterruptedException {
    StartingGun sg = new StartingGun();
    sg.ready();
    Thread[] threads = startWaitingThreads(sg);
    allThreadsDead(threads);
  }

  @Test
  public void testInterruptedStatus() throws InterruptedException {
    StartingGun sg = new StartingGun();
    Thread testingThread = Thread.currentThread();
    Thread thread = new Thread(() ->
    {
      while (testingThread.getState() != Thread.State.WAITING) {
        Thread.yield();
      }
      // just in case the state was set early
      LockSupport.parkNanos(PAUSE * 1_000_000);
      testingThread.interrupt();

      LockSupport.parkNanos(PAUSE * 1_000_000);
      sg.ready();
    });
    thread.start();

    sg.awaitUninterruptibly();
    assertTrue(testingThread.isInterrupted());
    thread.join(); // wait for that thread to finish
  }

  @Test
  public void testMultipleReadyCallsNoInterruptions() throws InterruptedException {
    StartingGun sg = new StartingGun();
    Thread[] threads = startWaitingThreads(sg);

    Thread.sleep(PAUSE);

    allThreadsAlive(threads);

    sg.ready();

    allThreadsDead(threads);

    threads = new Thread[10];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(sg::awaitUninterruptibly);
      threads[i].start();
    }
    Thread.sleep(PAUSE);
    allThreadsDead(threads);
  }

  @Test
  public void testStartingGunInterruptions() throws InterruptedException {
    StartingGun sg = new StartingGun();
    Thread[] threads = startWaitingThreads(sg);

    Thread.sleep(PAUSE);

    allThreadsAlive(threads);

    for (Thread thread : threads) thread.interrupt();

    Thread.sleep(PAUSE);

    sg.ready();

    allThreadsDead(threads);
  }

  private void allThreadsDead(Thread[] threads)
      throws InterruptedException {
    for (Thread thread : threads) {
      thread.join(PAUSE);
      assertFalse(thread.isAlive());
    }
  }

  private void allThreadsAlive(Thread[] threads) {
    for (Thread thread : threads) {
      assertTrue(thread.isAlive());
    }
  }
}
