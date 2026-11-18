package org.openjfx.lab2;

public class BlockCheckThread extends Thread {
    private final String email;
    private final AttemptTracker tracker;
    private volatile boolean blocked;
    private volatile long remainingSeconds;

    public BlockCheckThread(String email, AttemptTracker tracker) {
        super("BlockCheckThread");
        this.email = email;
        this.tracker = tracker;
    }

    @Override
    public void run() {
        blocked = tracker.isBlocked(email);
        if (blocked) {
            remainingSeconds = tracker.getRemainingBlockSeconds(email);
        }
    }

    public boolean isBlocked() {
        return blocked;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}