package kanban.card;

import JavaFX.Enums;
import kanban.column.Column;
import kanban.column.ColumnService;
import org.junit.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CardTest{
    private final CardService cardService = new CardService();
    private final ColumnService columnService =
            new ColumnService();
    private static final Long USER_ID = 2L;
    private static final Long boardId = 1L;


    private Long getExistingColumnId(Long boardId) {
        List<Column> columns = columnService.findAllColumnsByBoardId(boardId);
        assertFalse(columns.isEmpty(), "The test board needs an existing column.");
        return columns.getFirst().getColumnId();
    }

    @Test
    public void findCardsByColumn() {
        Long columnId = getExistingColumnId(boardId);
        List<Card> cards = cardService.findAllCardsByColumnId(columnId);
        assertNotNull(cards);
        assertTrue(cards.stream().allMatch(card -> columnId.equals(card.getColumnId())));
    }

    @Test
    public void findCardsByStatus() {
        List<Card> cards = cardService.findByStatus(boardId, Enums.CS.INBOXED);

        assertNotNull(cards);

        assertTrue(cards.stream().allMatch(card -> card.getStatus() == Enums.CS.INBOXED));
    }

    @Test
    public void createAndDeleteCard() {
        Long columnId = getExistingColumnId(boardId);

        Card card = new Card(null, columnId, boardId, 999L,
                "JUnit Test Card", "#FFFFFF", Enums.CS.ACTIVE
        );

        Card created = cardService.createCard(card);
        assertNotNull(created);
        assertNotNull(created.getCardId());
        List<Card> cards = cardService.findAllCardsByColumnId(columnId);

        assertTrue(cards.stream().anyMatch(c -> c.getCardId().equals(created.getCardId()))
        );

        cardService.deleteCard(created.getCardId());

        assertFalse(cardService.findAllCardsByColumnId(columnId)
                .stream().anyMatch(c -> c.getCardId().equals(created.getCardId()))
        );
    }

    @Test
    public void updateCardSection() {
        Long columnId = getExistingColumnId(boardId);

        Card card = new Card(null, columnId, boardId, 999L,
                "Original Card", "#FFFFFF", Enums.CS.ACTIVE
        );

        Card created = cardService.createCard(card);

        Long cardId = created.getCardId();
        cardService.updateCardSection(cardId, "Updated Card", Enums.Section.DESCRIPTION);
        List<Card> cards = cardService.findAllCardsByColumnId(columnId);

        Card updated = cards.stream()
                .filter(c -> c.getCardId().equals(cardId))
                .findFirst().orElseThrow();

        assertEquals(updated.getDescription(), "Updated Card");
        cardService.deleteCard(cardId);
    }

    @Test
    public void moveCardToInbox() {
        Long columnId = getExistingColumnId(boardId);
        Card card = new Card(null, columnId, boardId, 1L,
                "Inbox Test", "#FFFFFF", Enums.CS.ACTIVE);

        Card created = cardService.createCard(card);

        cardService.updateCardToInboxed(created.getCardId());

        Card updated = cardService
                .findByStatus(boardId, Enums.CS.INBOXED).stream()
                .filter(c -> c.getCardId().equals(created.getCardId()))
                .findFirst().orElse(null);

        assertNotNull(updated);
        assertEquals(updated.getStatus(), Enums.CS.INBOXED);
        assertNull(updated.getColumnId());
        assertNull(updated.getCardPosition());

        cardService.deleteCard(
                created.getCardId()
        );
    }
}
