package org.openjfx.lab2;

import javafx.application.Platform;
import java.util.function.Consumer;

public class CountdownTask implements Runnable {
    private final long totalSeconds;
    private final Consumer<String> onTick;
    private final Runnable onFinish;

    public CountdownTask(long totalSeconds, Consumer<String> onTick, Runnable onFinish) {
        this.totalSeconds = totalSeconds;
        this.onTick = onTick;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        try {
            for (long sec = totalSeconds; sec > 0; sec--) {
                final long remaining = sec;
                Platform.runLater(() ->
                        onTick.accept("החשבון חסום, נותרו " + remaining + " שניות")
                );
                Thread.sleep(1000);
            }
            Platform.runLater(onFinish);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}