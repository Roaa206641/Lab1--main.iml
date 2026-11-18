package org.openjfx.lab2;

import java.util.concurrent.BlockingQueue;

public class FailureWorker extends Thread {
    private final BlockingQueue<FailedAttempt> queue;
    private final AttemptTracker tracker;

    public FailureWorker(String name, BlockingQueue<FailedAttempt> queue, AttemptTracker tracker) {
        super(name);
        this.queue = queue;
        this.tracker = tracker;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                FailedAttempt attempt = queue.take();
                LoginResult result = tracker.recordFailure(attempt.getEmail());
                attempt.setResult(result);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}