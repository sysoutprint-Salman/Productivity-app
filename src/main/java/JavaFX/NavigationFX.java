package JavaFX;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

public class NavigationFX {
    @FXML private ToggleButton toDoButton;
    @FXML private ToggleButton aiChatButton;
    @FXML private ToggleButton notebookButton;
    @FXML private ToggleButton kanbanButton;

    public void select(Enums.Scene scene) {
        ToggleButton btn = switch (scene) {
            case TO_DO -> toDoButton;
            case AI_CHAT -> aiChatButton;
            case NOTEBOOK -> notebookButton;
            case KANBAN -> kanbanButton;
        };
        btn.setSelected(true);
    }

    @FXML
    private void openToDo(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.TO_DO, (ToggleButton) event.getSource());

    }

    @FXML
    private void openAIChat(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.AI_CHAT, (ToggleButton) event.getSource());
    }

    @FXML
    private void openNotebook(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.NOTEBOOK, (ToggleButton) event.getSource());
    }

    @FXML
    private void openKanban(ActionEvent event) {
        ViewManager.getInstance().switchScene(Enums.Scene.KANBAN, (ToggleButton) event.getSource());
    }
}