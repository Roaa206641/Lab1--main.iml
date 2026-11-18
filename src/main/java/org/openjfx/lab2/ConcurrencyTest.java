package org.openjfx.lab2;

public class ConcurrencyTest {

    public static String runTest() {
        int n = 10_000;
        int t = 10;
        int numThreads = 20;
        int failuresPerThread = 499; // 20 * 499 = 9,980
        String testEmail = "stress@test.com";

        AttemptTracker tracker = new AttemptTracker(n, t);
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < failuresPerThread; j++) {
                    tracker.recordFailure(testEmail);
                }
            }, "StressThread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Test interrupted";
            }
        }

        // Next 19 failures must NOT block
        for (int i = 1; i <= 19; i++) {
            LoginResult res = tracker.recordFailure(testEmail);
            if (res.getStatus() == LoginResult.Status.BLOCKED) {
                return "FAIL: Blocked too early at extra failure #" + i;
            }
        }

        // The 20th extra failure (total 10,000) MUST block
        LoginResult finalRes = tracker.recordFailure(testEmail);
        if (finalRes.getStatus() == LoginResult.Status.BLOCKED) {
            return "PASS: 9,980 + 19 did not block, 10,000th failure blocked!";
        } else {
            return "FAIL: Lost updates! Still not blocked after 10,000 failures (remaining: "
                    + finalRes.getRemainingAttempts() + ")";
        }
    }
}