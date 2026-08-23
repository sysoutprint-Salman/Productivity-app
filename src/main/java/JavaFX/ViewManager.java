package JavaFX;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class ViewManager {

    private static final ViewManager INSTANCE = new ViewManager();
    private final Map<Enums.Scene, Scene> scenes = new EnumMap<>(Enums.Scene.class);
    private final Map<Enums.Scene, Object> controllers = new EnumMap<>(Enums.Scene.class);
    private Stage stage;

    private ViewManager() {}

    public static ViewManager getInstance() {
        return INSTANCE;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Scene getScene(Enums.Scene target) {
        return scenes.computeIfAbsent(target, this::loadScene);
    }

    private Scene loadScene(Enums.Scene target) {
        String path = switch (target) {
            case TO_DO -> "/JavaFX/tasks.fxml";
            case AI_CHAT -> "/JavaFX/AI.fxml";
            case NOTEBOOK -> "/JavaFX/notebook.fxml";
            case KANBAN -> "/JavaFX/kanbanBoard.fxml";
        };
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            controllers.put(target, loader.getController());
            return new Scene(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + path, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getController(Enums.Scene target) {
        return (T) controllers.get(target);
    }

    public void switchScene(Enums.Scene target) {
        if (stage == null) {
            throw new IllegalStateException("Stage not set on ViewManager yet.");
        }
        stage.setScene(getScene(target));
        stage.setMaximized(true);
        stage.show();
    }

    public void switchScene(Enums.Scene target, ToggleButton source) {
        if (stage == null) {
            stage = (Stage) source.getScene().getWindow();
        }
        switchScene(target);
    }
}