package JavaFX;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class UI extends Application {
    private final LogInFX logInFX = new LogInFX();

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            //logInFX.autoLogIn(primaryStage);
            ViewManager.getInstance().setStage(primaryStage);
            ViewManager.getInstance().switchScene(Enums.Scene.TO_DO);
            primaryStage.setAlwaysOnTop(true);
            primaryStage.toFront();
            primaryStage.requestFocus();
            primaryStage.setMaximized(true);
            primaryStage.setAlwaysOnTop(false);
            primaryStage.getIcons().add(new Image("/Images/App Icon.png"));
            primaryStage.setOnCloseRequest(event -> shutdown());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void shutdown() {
        /*ConfigurableApplicationContext ctx = Rest.getApplicationContext();
        if (ctx != null) {
            ctx.close();
        }*/
        Platform.exit();
        System.exit(0);
    }
}


