package kanban.card;

import kanban.Enums;

import java.util.List;
import java.util.Map;

public class CardService {

    private final CardRepo cardRepo;

    public CardService() {
        cardRepo = new CardRepo();
    }

    public Card createCard(Card card) {
        return cardRepo.createCard(card);
    }

    public List<Card> findAllCardsByColumnId(Long listId) {
        return cardRepo.findAllCardsByListId(listId);
    }

    public List<Card> findByStatus(Long boardId, Enums.CS status) {
        return cardRepo.findByStatus(boardId, status);
    }

    public void updateCardSections(Long cardId, Map<Enums.Section, Object> updates){
        cardRepo.updateCardSections(cardId, updates);
    }

    public void updateCardSection(Long cardId, Object value, Enums.Section section) {
        if (value == null) {
            System.out.println(
                    "Null value detected, card couldn't be updated."
            );
            return;
        }

        cardRepo.updateCardSection(cardId, value, section);
    }

    public void updateCardSectionBatch(List<Card> cards, Enums.Section section) {
        cardRepo.updateCardSectionBatch(cards, section);
    }

    public void updateCardToInboxed(Long cardId) {
        cardRepo.updateCardToInboxed(cardId);
    }

    public void updateCardsToParentArchived(Long listId) {
        cardRepo.updateCardsToParentArchived(listId);
    }

    public void updateCardsToParentDeleted(Long listId) {
        cardRepo.updateCardsToParentDeleted(listId);
    }

    public void deleteCard(Long cardId) {
        cardRepo.deleteCard(cardId);
    }
}
