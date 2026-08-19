package kanban.column;

import kanban.Enums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Column {
    private Long columnId;
    private Long boardId;
    private Long columnPosition;
    private String title;
    private String hexColor;
    private Enums.LS status;
}
