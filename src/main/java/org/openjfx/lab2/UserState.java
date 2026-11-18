package org.openjfx.lab2;

public class UserState {
    private int failedAttempts = 0;
    private long blockedUntilMillis = 0;

    public synchronized boolean isBlocked() {
        long now = System.currentTimeMillis();
        if (blockedUntilMillis > 0 && now >= blockedUntilMillis) {
            failedAttempts = 0;
            blockedUntilMillis = 0;
            return false;
        }
        return blockedUntilMillis > now;
    }

    public synchronized long getRemainingBlockSeconds() {
        if (!isBlocked()) {
            return 0;
        }
        long remainingMillis = blockedUntilMillis - System.currentTimeMillis();
        return Math.max(1, (long) Math.ceil(remainingMillis / 1000.0));
    }

    public synchronized LoginResult recordFailure(int maxAttempts, int blockSeconds) {
        if (isBlocked()) {
            return LoginResult.blocked(getRemainingBlockSeconds());
        }

        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            blockedUntilMillis = System.currentTimeMillis() + (blockSeconds * 1000L);
            return LoginResult.blocked(blockSeconds);
        }

        int remaining = maxAttempts - failedAttempts;
        return LoginResult.failedWithRemaining(remaining);
    }

    public synchronized void reset() {
        failedAttempts = 0;
        blockedUntilMillis = 0;
    }

    public synchronized int getFailedAttempts() {
        return failedAttempts;
    }
}