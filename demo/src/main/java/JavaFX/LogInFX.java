package JavaFX;

import SpringBoot.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Data
public class LogInFX {
    private final SwitchScenes switchScenes = new SwitchScenes();
    private final HTTPHandler httpHandler = new HTTPHandler();
    private final UserPrefs userPrefs = new UserPrefs();
    private TaskFX taskFX = new TaskFX();
    //Login
    public TextField userLogInField;
    public Button logInBut;
    public Hyperlink registerHL;
    public VBox loginVbox;
    private Label notFoundMessage = new Label("Username or email not found, try again.");

    //Register
    public TextField createUsername;
    public TextField enterEmail;
    public Button createAccBut;
    public Label feedbackLabel;
    public Hyperlink loginHL;
    public VBox registerVbox;

    private enum Case {NOT_FOUND, ALREADY_EXISTS, EMPTY, NO_VALID_USERNAME, NO_VALID_EMAIL, NO_SPACE_ALLOWED}
    private final ObjectMapper mapper = new ObjectMapper();

    public LogInFX(){}
    public void initialize(){
        if (userLogInField != null && logInBut != null) {
            userLogInField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    logInBut.fire();
                }
            });
            logInBut.setDefaultButton(true);
        }

        if (createUsername != null && enterEmail != null && createAccBut != null) {
            EventHandler<KeyEvent> enterKeyPressed = event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    createAccBut.fire();
                }
            };
            createUsername.setOnKeyPressed(enterKeyPressed);
            enterEmail.setOnKeyPressed(enterKeyPressed);
        }
    }
    public void autoLogIn(Stage curStage){
        String storedUsername = userPrefs.getStoredUsername();
        String storedEmail = userPrefs.getStoredEmail();
        if (storedUsername != null || storedEmail != null){
            //Switch to tasks scene
            switchScenes.switchToTasks(curStage);
        } else {
            //Switch to Log scene
            switchScenes.switchToLogin();
        }
    }
    public void onLogIn(ActionEvent event) throws JsonProcessingException {
        String emailOrUsernameCredential = userLogInField.getText().trim();
        boolean existingUser = httpHandler.GET(
                "users/existing?username=" + URLEncoder.encode(emailOrUsernameCredential, StandardCharsets.UTF_8)  +
                        "&email=" + URLEncoder.encode(emailOrUsernameCredential, StandardCharsets.UTF_8)
        );
        if (existingUser){
            userPrefs.saveToPref(emailOrUsernameCredential); //Saves username in registry for quick login
            switchScenes.switchScene(event, "tasks",controller ->{
                taskFX = (TaskFX) controller;
                taskFX.setUser(userPrefs.getSavedUser());
                taskFX.getByPosted();}
            );
        } else {
            if (!loginVbox.getChildren().contains(notFoundMessage)){
                notFoundMessage.getStyleClass().add("errorMessage");
                loginVbox.getChildren().add(notFoundMessage);
            }
        }
    }
    @FXML
    public void onRegister(ActionEvent event){
        String username = createUsername.getText().trim();
        String email = enterEmail.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            edgeCase(Case.EMPTY);
        } else {
            boolean existingUser = httpHandler.GET(
                    "users/existing?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                            "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
            );
            if (existingUser) {
                edgeCase(Case.ALREADY_EXISTS);
            } else {
                userPrefs.setUsername(username);
                userPrefs.setEmail(email.toLowerCase());
                if (registerRequirements(username, email)){
                    createUsername.clear(); enterEmail.clear();
                    switchScenes.switchScene(event, "tasks", controller -> {
                                taskFX = (TaskFX) controller;
                                taskFX.setUser(userPrefs.getSavedUser());
                                taskFX.getByPosted();
                            }
                    );
                }
            }
        }
    }
    public boolean registerRequirements(String username, String email){
         if (username.contains(" ") || email.contains(" ")) {
            edgeCase(Case.NO_SPACE_ALLOWED);
            return false;
        }
         else if (!username.matches("^(?=.*[A-Za-z])(?=.*\\d)\\S{6,}$")){
            edgeCase(Case.NO_VALID_USERNAME);
            return false;
        } else if (!email.matches("^(?=[A-Za-z0-9._%+-]*[A-Za-z])[A-Za-z0-9._%+-]+@(gmail|hotmail|outlook)\\.com$")) {
            edgeCase(Case.NO_VALID_EMAIL);
            return false;
        }  else {
            userPrefs.saveUser();
            return true;
        }
    }

    public void edgeCase(Case issue){
        switch (issue){
            case EMPTY :
                feedbackLabel.setText("Username or email can't be empty.");

                break;
            case NO_VALID_USERNAME :
                feedbackLabel.setText("Username needs to be 6 characters long and contain atleast 1 number.");

                break;
            case NO_VALID_EMAIL :
                feedbackLabel.setText("A valid email is required.");

                break;
            case NOT_FOUND :
                feedbackLabel.setText("Username or email not found, try again.");

                break;
            case ALREADY_EXISTS :
                feedbackLabel.setText("Username or email already exists.");
                break;
            case NO_SPACE_ALLOWED:
                feedbackLabel.setText("No spaces allowed in username or password.");
                break;
        }
        feedbackLabel.setVisible(true);
    }

    public void switchToLogin(ActionEvent event){ //Used to switch from register to login & back
        switchScenes.switchScene(event, "login", controller ->{});
    }
    public void switchToRegister(ActionEvent event){
        switchScenes.switchScene(event, "register", controller ->{});
    }


}
