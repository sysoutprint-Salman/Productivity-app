package JavaFX;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class ViewManager {

    private static final ViewManager INSTANCE = new ViewManager();
    private final Map<Enums.Scene, Parent> roots = new EnumMap<>(Enums.Scene.class);
    private final Map<Enums.Scene, Object> controllers = new EnumMap<>(Enums.Scene.class);
    @Setter private Stage stage;
    private Scene mainScene;

    private ViewManager() {}

    public static ViewManager getInstance() {
        return INSTANCE;
    }

    private Parent getRoot(Enums.Scene target) {
        return roots.computeIfAbsent(target, this::loadRoot);
    }

    private Parent loadRoot(Enums.Scene target) {
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
            return root;
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

        Parent root = getRoot(target);

        if (mainScene == null) {
            mainScene = new Scene(root);
            stage.setScene(mainScene);
            stage.setMaximized(true);
            stage.show();
        } else {
            mainScene.setRoot(root);
        }

        Object controller = controllers.get(target);

        if (controller instanceof AbstractFX fx) {
            fx.highlightNav();
        }
    }
}