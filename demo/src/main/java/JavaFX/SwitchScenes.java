package JavaFX;


import SpringBoot.Rest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.function.Consumer;


public class SwitchScenes {

    private final String stageTitle = "Productivity App";
    public SwitchScenes(){}

    public void switchScene(ActionEvent event, String fxmlPath, Consumer<Object> afterLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath + ".fxml"));
            Parent root = loader.load();

            Stage stage = null;
            Object source = event.getSource();

            if (source instanceof Node node) {
                // Attempting to get the stage from any UI element.
                stage = (Stage) node.getScene().getWindow();
            } else if (source instanceof MenuItem menuItem) {
                // Attempting to get the stage through a menuitem's popup window.
                if (menuItem.getParentPopup() != null) {
                    stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
                }
                // Attempting to get the stage from the clicked event, likely a button.
                else if (event.getTarget() instanceof Node node) {
                    stage = (Stage) node.getScene().getWindow();
                }
            }

            if (stage == null) {
                System.err.println("Could not resolve Stage from event source: " + source + " (Ignorable)");

                // Attempting to use the first visible stage available
                stage = (Stage) Stage.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null);
                if (stage == null) {
                    return;
                }
            }

            stage.setScene(new Scene(root));
            stage.show();

            afterLoad.accept(loader.getController());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToLogin(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle(stageTitle);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            //Since login uses a different stage than the rest of the app, an explicit shutdown of SB is needed
            stage.setOnCloseRequest(e -> {
                ConfigurableApplicationContext ctx = Rest.getApplicationContext();
                if (ctx != null) {
                    ctx.close();
                    System.exit(0);
                }
            });
        } catch (IOException | RuntimeException ex) {
            System.err.println("Error trying to load switchToLogin.");
            ex.printStackTrace();
        }
    }
    public void switchToTasks(Stage curStage) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/tasks.fxml"));
                Parent root = loader.load();
                TaskFX FXHandler = loader.getController();
                Scene scene = new Scene(root);
                curStage.setTitle(stageTitle);
                curStage.setScene(scene);
                curStage.centerOnScreen();
                curStage.show();
                Platform.runLater(FXHandler::getByPosted);
            } catch (IOException | RuntimeException ex) {
                System.err.println("Error trying to load switchToTasks.");
                ex.printStackTrace();
            }
    }
}
