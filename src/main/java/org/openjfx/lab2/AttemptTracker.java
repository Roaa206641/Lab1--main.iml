package org.openjfx.lab2;

import java.util.concurrent.ConcurrentHashMap;

public class AttemptTracker {
    private final ConcurrentHashMap<String, UserState> states = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final int blockSeconds;

    public AttemptTracker(int maxAttempts, int blockSeconds) {
        this.maxAttempts = maxAttempts;
        this.blockSeconds = blockSeconds;
    }

    private UserState getOrCreateState(String email) {
        return states.computeIfAbsent(email.toLowerCase(), k -> new UserState());
    }

    public boolean isBlocked(String email) {
        UserState state = states.get(email.toLowerCase());
        return state != null && state.isBlocked();
    }

    public long getRemainingBlockSeconds(String email) {
        UserState state = states.get(email.toLowerCase());
        return state == null ? 0 : state.getRemainingBlockSeconds();
    }

    public LoginResult recordFailure(String email) {
        return getOrCreateState(email).recordFailure(maxAttempts, blockSeconds);
    }

    public void reset(String email) {
        UserState state = states.get(email.toLowerCase());
        if (state != null) {
            state.reset();
        }
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getBlockSeconds() {
        return blockSeconds;
    }
}