package JavaFX;

import SpringBoot.AI;
import SpringBoot.Notebook;
import SpringBoot.Task;
import SpringBoot.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.json.HTTP;

import java.util.List;
@Data
public class AppState {

    public enum Section{TASKS, CHATS, NOTEBOOKS}

    @Getter
    @Setter
    private static List<Task> tasks;
    @Getter
    @Setter
    private static List<Notebook> notebooks;
    @Getter
    @Setter
    private static List<AI> chats;

    public static void refresh(Section section){
        switch (section){
            case TASKS -> tasks = HTTPHandler.GET("tasks/all/" + User.getUserId() , Task.class);
            case CHATS -> chats = HTTPHandler.GET("gptresponses/filter?userId=" + User.getUserId(), AI.class);
            case NOTEBOOKS -> notebooks = HTTPHandler.GET("notebooks/filter?userId=" + User.getUserId(), Notebook.class);
        }
    }
}
