package org.openjfx.lab2.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.openjfx.lab2.App;
import org.openjfx.lab2.ConcurrencyTest;
import org.openjfx.lab2.CountdownTask;
import org.openjfx.lab2.LoginResult;
import org.openjfx.lab2.LoginTask;

public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private Thread countdownThread;

    @FXML
    void onLoginClick(ActionEvent event) {
        String email = username.getText().trim();
        String pass = password.getText();

        setInputsDisabled(true);
        errorLabel.setText("");

        LoginTask task = new LoginTask(
                email,
                pass,
                App.getRepository(),
                App.getTracker(),
                App.getFailureQueue(),
                this::handleLoginResult
        );

        Thread loginThread = new Thread(task, "LoginTaskThread");
        loginThread.setDaemon(true);
        loginThread.start();
    }

    @FXML
    void onConcurrencyTestClick(ActionEvent event) {
        Thread testThread = new Thread(() -> {
            String result = ConcurrencyTest.runTest();
            System.out.println(result);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Concurrency Test Result");
                alert.setHeaderText("20-Thread Stress Test (n = 10,000)");
                alert.setContentText(result);
                alert.showAndWait();
            });
        }, "ConcurrencyTestRunner");
        testThread.setDaemon(true);
        testThread.start();
    }

    private void handleLoginResult(LoginResult result) {
        switch (result.getStatus()) {
            case SUCCESS -> {
                stopCountdown();
                errorLabel.setText("");
                try {
                    App.showWelcome(result.getUser());
                } catch (IOException e) {
                    errorLabel.setText("Error loading welcome screen");
                    setInputsDisabled(false);
                }
            }
            case INVALID_CREDENTIALS -> {
                errorLabel.setText("פרטים שגויים");
                setInputsDisabled(false);
            }
            case FAILED_WITH_REMAINING -> {
                errorLabel.setText("פרטים שגויים. נותרו " + result.getRemainingAttempts() + " ניסיונות");
                setInputsDisabled(false);
            }
            case BLOCKED -> {
                startCountdown(result.getRemainingSeconds());
            }
        }
    }

    private void startCountdown(long seconds) {
        stopCountdown();
        CountdownTask countdownTask = new CountdownTask(
                seconds,
                message -> errorLabel.setText(message),
                () -> {
                    errorLabel.setText("");
                    setInputsDisabled(false);
                }
        );
        countdownThread = new Thread(countdownTask, "CountdownThread");
        countdownThread.setDaemon(true);
        countdownThread.start();
    }

    public void stopCountdown() {
        if (countdownThread != null && countdownThread.isAlive()) {
            countdownThread.interrupt();
        }
    }

    private void setInputsDisabled(boolean disabled) {
        username.setDisable(disabled);
        password.setDisable(disabled);
        if (loginButton != null) {
            loginButton.setDisable(disabled);
        }
    }
}