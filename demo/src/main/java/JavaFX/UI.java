package JavaFX;

import SpringBoot.Rest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.prefs.Preferences;


//Putting SpringApp & Rest annotations here didn't work likely because of Application extension.
@Slf4j
public class UI extends Application {
    private final LogInFX logInFX = new LogInFX();

    public static void main(String[] args) {
       Rest.context = SpringApplication.run(Rest.class, args);
        launch(args);

    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            logInFX.autoLogIn(primaryStage);
            // Shutdown on window close
            primaryStage.setOnCloseRequest(event -> shutdown());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void shutdown() {
        ConfigurableApplicationContext ctx = Rest.getApplicationContext();
        if (ctx != null) {
            ctx.close();
        }
        Platform.exit(); //exits out of both javafx window & springboot
        System.exit(0);
    }
}


