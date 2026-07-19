package JavaFX;

import SpringBoot.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;

public abstract class AbstractFX {
    protected final ObjectMapper mapper = new ObjectMapper();
    protected final SwitchScenes handler = new SwitchScenes();
    protected UserPrefs userPrefs = new UserPrefs();
    protected User user = userPrefs.getSavedUser();
    protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");
}
