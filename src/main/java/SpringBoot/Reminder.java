package SpringBoot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reminder {
    private Long reminderId;
    private Long boardId;
    private String reminderTitle;
    private String description;
    private Priority priority;
    private LocalDateTime dueDate;
    public enum Priority {NONE, LOW, MEDIUM, HIGH}
}
