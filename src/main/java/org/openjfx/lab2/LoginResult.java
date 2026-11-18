package org.openjfx.lab2;

public class LoginResult {
    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS,       // Email does not exist (no attempt tracking)
        FAILED_WITH_REMAINING,     // Email exists, wrong password, not yet blocked
        BLOCKED                    // Account is blocked for X seconds
    }

    private final Status status;
    private final User user;
    private final int remainingAttempts;
    private final long remainingSeconds;

    private LoginResult(Status status, User user, int remainingAttempts, long remainingSeconds) {
        this.status = status;
        this.user = user;
        this.remainingAttempts = remainingAttempts;
        this.remainingSeconds = remainingSeconds;
    }

    public static LoginResult success(User user) {
        return new LoginResult(Status.SUCCESS, user, 0, 0);
    }

    public static LoginResult invalidCredentials() {
        return new LoginResult(Status.INVALID_CREDENTIALS, null, 0, 0);
    }

    public static LoginResult failedWithRemaining(int remainingAttempts) {
        return new LoginResult(Status.FAILED_WITH_REMAINING, null, remainingAttempts, 0);
    }

    public static LoginResult blocked(long remainingSeconds) {
        return new LoginResult(Status.BLOCKED, null, 0, remainingSeconds);
    }

    public Status getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}