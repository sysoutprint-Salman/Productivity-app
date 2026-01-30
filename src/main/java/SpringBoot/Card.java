package SpringBoot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    private Long cardId;
    private Long listId;
    private Long boardId;
    private Long cardPosition;
    private String description;
    private String hexColor;
    private CardStatus status;
    public enum CardStatus {ACTIVE, ARCHIVED, PARENT_ARCHIVED, DELETED}
}
