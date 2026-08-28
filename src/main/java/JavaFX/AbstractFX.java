package JavaFX;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import lombok.Getter;
import user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;

public abstract class AbstractFX {
    protected final ObjectMapper mapper = new ObjectMapper();
    protected UserPrefs userPrefs = new UserPrefs();
    protected User user = userPrefs.getSavedUser();
    protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");

    @Getter @FXML protected NavigationFX navController;

    public abstract void highlightNav(); // each page implements its own one-liner


}
