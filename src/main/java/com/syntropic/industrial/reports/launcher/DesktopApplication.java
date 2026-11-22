package com.syntropic.industrial.reports.launcher;

import com.syntropic.industrial.reports.ReportsApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CompletableFuture;

public class DesktopApplication extends Application {

    private CompletableFuture<ConfigurableApplicationContext> springContextFuture;

    private static final String APP_URL = "http://127.0.0.1:8080/";

    @Override
    public void init() {
        // Capture the classloader that actually loaded DesktopApplication
        ClassLoader appClassLoader = getClass().getClassLoader();

        springContextFuture = CompletableFuture.supplyAsync(() -> {
            Thread.currentThread().setContextClassLoader(appClassLoader);
            return new SpringApplicationBuilder(ReportsApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .headless(false)
                    .run();
        });
    }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);

        StackPane root = new StackPane(webView);
        Scene scene = new Scene(root, 1280, 800);

        stage.setTitle("Reports");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        // Shut down Spring Boot and force JVM exit when red 'X' is clicked
        stage.setOnCloseRequest(event -> shutdownAndExit());

        stage.show();

        // Load the HTML only after Spring Boot has started.
        springContextFuture.whenComplete((context, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    Throwable cause = error.getCause() != null ? error.getCause() : error;
                    Label errorLabel = new Label(
                            "Unable to start Industrial Reporting.\n"
                                    + cause.getMessage()
                    );
                    root.getChildren().setAll(errorLabel);
                    return;
                }

                webView.getEngine().load(APP_URL);
            });
        });
    }

    @Override
    public void stop() {
        // Also handles macOS Cmd+Q or Application menu Quit
        shutdownAndExit();
    }

    private void shutdownAndExit() {
        try {
            if (springContextFuture != null && springContextFuture.isDone()) {
                springContextFuture.get().close();
            }
        } catch (Exception ignored) {
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }
}
