package JavaFX;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;

public abstract class AbstractFX {
    protected final ObjectMapper mapper = new ObjectMapper();
    protected final SwitchScenes handler = new SwitchScenes();
    protected UserPrefs userPrefs = new UserPrefs();
    protected User user = userPrefs.getSavedUser();
    protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");
    protected Parent toDoView;
    protected Parent aiChatView;
    protected Parent notebookView;
    protected Parent kanbanView;
    protected Stage stage;
    @FXML protected NavigationFX navController;
    protected abstract Enums.Scene getViewType();

    public void initialize() {
        navController.select(getViewType());
    }

    protected void switchScene(Enums.Scene scene, ToggleButton source) {
        ViewManager.getInstance().switchScene(scene, source);
    }
}
