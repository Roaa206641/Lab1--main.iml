package org.openjfx.lab2;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.openjfx.lab2.controller.LoginController;
import org.openjfx.lab2.controller.WelcomeController;

public class App extends Application {

    private static Stage stage;
    private static UsersRepository repository;
    private static AttemptTracker tracker;
    private static BlockingQueue<FailedAttempt> failureQueue;
    private static FailureWorker worker1;
    private static FailureWorker worker2;
    private static LoginController currentLoginController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;

        // 1. Validate n and t from Program arguments (or ask user manually)
        int[] params = resolveParams(getParameters().getRaw());
        if (params == null) {
            Platform.exit();
            return;
        }
        int maxAttempts = params[0];
        int blockSeconds = params[1];

        // 2. Initialize shared state and start 2 daemon FailureWorker threads
        repository = new UsersRepository();
        tracker = new AttemptTracker(maxAttempts, blockSeconds);
        failureQueue = new LinkedBlockingQueue<>();

        worker1 = new FailureWorker("FailureWorker-1", failureQueue, tracker);
        worker2 = new FailureWorker("FailureWorker-2", failureQueue, tracker);
        worker1.start();
        worker2.start();

        stage.setResizable(false);
        // Closing the window interrupts all active threads and stops the app
        stage.setOnCloseRequest(event -> {
            shutdownThreads();
            Platform.exit();
        });

        showLogin();
        stage.show();
    }

    private int[] resolveParams(List<String> rawArgs) {
        int[] parsed = tryParseArgs(rawArgs);
        if (parsed != null) {
            return parsed;
        }

        // Ask the user manually if arguments are missing or invalid
        String prompt = "הארגומנטים חסרים או לא תקינים.\nהזינו n (מספר ניסיונות >= 1) ו-t (שניות חסימה >= 1) מופרדים ברווח (לדוגמה: 3 10):";
        while (true) {
            TextInputDialog dialog = new TextInputDialog("3 10");
            dialog.setTitle("הגדרת פרמטרים (n, t)");
            dialog.setHeaderText("ארגומנטים לא תקינים");
            dialog.setContentText(prompt);

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return null; // User clicked Cancel
            }

            String[] parts = result.get().trim().split("\\s+");
            parsed = tryParseArgs(List.of(parts));
            if (parsed != null) {
                return parsed;
            }
            prompt = "קלט שגוי! יש להזין שני מספרים שלמים הגדולים או שווים ל-1 (לדוגמה: 3 10):";
        }
    }

    private int[] tryParseArgs(List<String> args) {
        if (args == null || args.size() < 2) {
            return null;
        }
        try {
            int n = Integer.parseInt(args.get(0));
            int t = Integer.parseInt(args.get(1));
            if (n >= 1 && t >= 1) {
                return new int[]{n, t};
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static void shutdownThreads() {
        if (currentLoginController != null) {
            currentLoginController.stopCountdown();
        }
        if (worker1 != null) {
            worker1.interrupt();
        }
        if (worker2 != null) {
            worker2.interrupt();
        }
    }

    public static UsersRepository getRepository() {
        return repository;
    }

    public static AttemptTracker getTracker() {
        return tracker;
    }

    public static BlockingQueue<FailedAttempt> getFailureQueue() {
        return failureQueue;
    }

    public static void showLogin() throws IOException {
        FXMLLoader loader = loadFxml("login.fxml");
        Parent root = loader.load();
        currentLoginController = loader.getController();

        stage.setTitle("Users Login");
        stage.setScene(new Scene(root));
    }

    public static void showWelcome(User user) throws IOException {
        if (currentLoginController != null) {
            currentLoginController.stopCountdown();
        }
        FXMLLoader loader = loadFxml("welcome.fxml");
        Parent root = loader.load();
        WelcomeController controller = loader.getController();
        controller.setUser(user);

        stage.setTitle("Welcome");
        stage.setScene(new Scene(root));
    }

    private static FXMLLoader loadFxml(String name) {
        return new FXMLLoader(App.class.getResource(name));
    }

    public static void main(String[] args) {
        launch(args);
    }
}