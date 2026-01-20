package SpringBoot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KList {
    private Long listId;
    private Long userId;
    private Long boardId;
    private String title;
    private String hexColor;
    private ListStatus status;
    public enum ListStatus {ACTIVE, ARCHIVED, DELETED}
}
