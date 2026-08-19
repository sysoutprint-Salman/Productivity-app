package kanban.card;

import kanban.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private Long cardId;
    private Long columnId;
    private Long boardId;
    private Long cardPosition;
    private String description;
    private String hexColor;
    private Enums.CS status;
}
