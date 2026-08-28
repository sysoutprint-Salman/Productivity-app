package JavaFX;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import lombok.Data;

@Data
public class NavigationFX {
    @FXML private ToggleGroup navigation;
    @FXML private ToggleButton toDoButton;
    @FXML private ToggleButton aiChatButton;
    @FXML private ToggleButton notebookButton;
    @FXML private ToggleButton kanbanButton;



    @FXML
    private void openToDo(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.TO_DO);

    }

    @FXML
    private void openAIChat(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.AI_CHAT);
    }

    @FXML
    private void openNotebook(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.NOTEBOOK);
    }

    @FXML
    private void openKanban(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.KANBAN);
    }
}