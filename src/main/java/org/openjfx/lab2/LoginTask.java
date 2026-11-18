package org.openjfx.lab2;

import javafx.application.Platform;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class LoginTask implements Runnable {
    private final String email;
    private final String password;
    private final UsersRepository repository;
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failureQueue;
    private final Consumer<LoginResult> onResult;

    public LoginTask(String email,
                     String password,
                     UsersRepository repository,
                     AttemptTracker tracker,
                     BlockingQueue<FailedAttempt> failureQueue,
                     Consumer<LoginResult> onResult) {
        this.email = email;
        this.password = password;
        this.repository = repository;
        this.tracker = tracker;
        this.failureQueue = failureQueue;
        this.onResult = onResult;
    }

    @Override
    public void run() {
        try {
            // 1. Check if blocked using BlockCheckThread + join()
            BlockCheckThread blockCheck = new BlockCheckThread(email, tracker);
            blockCheck.start();
            blockCheck.join();

            if (blockCheck.isBlocked()) {
                deliverResult(LoginResult.blocked(blockCheck.getRemainingSeconds()));
                return;
            }

            // 2. Validate credentials via UsersRepository
            User user = null;
            try {
                user = repository.findUser(email, password);
            } catch (Exception e) {
                // Invalid format or failed validation from Lab 1/2
                user = null;
            }

            if (user != null) {
                tracker.reset(email);
                deliverResult(LoginResult.success(user));
                return;
            }

            // 3. Failed login: check if email exists in repository
            if (!repository.emailExists(email)) {
                deliverResult(LoginResult.invalidCredentials());
                return;
            }

            // 4. Existing email + wrong password: send to FailureWorker via BlockingQueue
            FailedAttempt attempt = new FailedAttempt(email);
            failureQueue.put(attempt);
            LoginResult result = attempt.awaitResult();
            deliverResult(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void deliverResult(LoginResult result) {
        Platform.runLater(() -> onResult.accept(result));
    }
}