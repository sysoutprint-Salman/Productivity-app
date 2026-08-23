package kanban.card;



import database.DatabaseManager;
import JavaFX.Enums;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardRepo {

    private final CardMapper cardMapper = new CardMapper();

    public List<Card> findAllCardsByListId(Long listId) {

        String sql = """
                SELECT * FROM cards
                WHERE list_id = ?
                """;

        List<Card> cards = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, listId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(cardMapper.mapRow(rs));
                }
            }

            return cards;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find cards by list",
                    e
            );
        }
    }

    public List<Card> findByStatus(Long boardId, Enums.CS status) {

        String sql = """
                SELECT * FROM cards
                WHERE board_id = ? AND status = ?
                """;

        List<Card> cards = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, boardId);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(cardMapper.mapRow(rs));
                }
            }

            return cards;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find cards by status",
                    e
            );
        }
    }

    public Card createCard(Card card) {

        String sql = """
                INSERT INTO cards
                    (list_id, board_id, card_position,
                     description, hex_color, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            if (card.getColumnId() == null) {
                stmt.setNull(1, Types.BIGINT);
            } else {
                stmt.setLong(1, card.getColumnId());
            }

            stmt.setLong(2, card.getBoardId());

            if (card.getCardPosition() == null) {
                stmt.setNull(3, Types.BIGINT);
            } else {
                stmt.setLong(3, card.getCardPosition());
            }

            stmt.setString(4, card.getDescription());
            stmt.setString(5, card.getHexColor());
            stmt.setString(6, card.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    card.setCardId(keys.getLong(1));
                }
            }

            return card;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create card", e);
        }
    }

    public void updateCardSections(Long cardId, Map<Enums.Section, Object> updates) {
        if (updates == null || updates.isEmpty()) return;

        StringBuilder sql = new StringBuilder("UPDATE cards SET ");
        List<Object> values = new ArrayList<>();

        for (Map.Entry<Enums.Section, Object> entry : updates.entrySet()) {
            String column = switch (entry.getKey()) {
                case ID -> "list_id";
                case DESCRIPTION -> "description";
                case COLOR -> "hex_color";
                case POSITION -> "card_position";
                case STATUS -> "status";
                default -> throw new IllegalStateException("Unexpected value: " + entry.getKey());
            };

            if (!values.isEmpty()) sql.append(", ");
            sql.append(column).append(" = ?");
            values.add(entry.getValue());
        }

        sql.append(" WHERE card_id = ?");

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++)
                stmt.setObject(i + 1, values.get(i));

            stmt.setLong(values.size() + 1, cardId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update card sections", e);
        }
    }

    public void updateCardSection(Long cardId, Object value, Enums.Section section) {
        String sql;
        switch (section) {
            case ID:
                sql = "UPDATE cards SET list_id = ? WHERE card_id = ?";
                break;

            case DESCRIPTION:
                sql = "UPDATE cards SET description = ? WHERE card_id = ?";
                break;

            case COLOR:
                sql = "UPDATE cards SET hex_color = ? WHERE card_id = ?";
                break;

            case POSITION:
                sql = "UPDATE cards SET card_position = ? WHERE card_id = ?";
                break;

            case STATUS:
                sql = "UPDATE cards SET status = ? WHERE card_id = ?";
                break;

            default:
                return;
        }

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (value == null) {
                stmt.setNull(1, Types.BIGINT);
            } else {
                stmt.setObject(1, value);
            }

            stmt.setLong(2, cardId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update card section",
                    e
            );
        }
    }

    public void updateCardSectionBatch(List<Card> cardsToUpdate, Enums.Section section) {
        String sql = switch (section) {
            case ID -> "UPDATE cards SET list_id = ? WHERE card_id = ?";
            case DESCRIPTION ->
                    "UPDATE cards SET description = ? WHERE card_id = ?";
            case COLOR ->
                    "UPDATE cards SET hex_color = ? WHERE card_id = ?";
            case POSITION ->
                    "UPDATE cards SET card_position = ? WHERE card_id = ?";
            case STATUS ->
                    "UPDATE cards SET status = ? WHERE card_id = ?";
            default -> null;
        };

        if (sql == null) {
            return;
        }

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {

                for (Card card : cardsToUpdate) {

                    Object value = switch (section) {
                        case ID -> card.getColumnId();
                        case DESCRIPTION -> card.getDescription();
                        case COLOR -> card.getHexColor();
                        case POSITION -> card.getCardPosition();
                        case STATUS -> card.getStatus().name();
                        default -> null;
                    };

                    if (value == null) {
                        stmt.setNull(1, Types.BIGINT);
                    } else {
                        stmt.setObject(1, value);
                    }

                    stmt.setLong(2, card.getCardId());

                    stmt.addBatch();
                }

                stmt.executeBatch();
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to batch update cards",
                    e
            );
        }
    }

    public void updateCardToInboxed(Long cardId) {

        String sql = """
                UPDATE cards
                SET card_position = NULL,
                    status = 'INBOXED',
                    list_id = NULL
                WHERE card_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cardId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to move card to inbox",
                    e
            );
        }
    }

    public void updateCardsToParentArchived(Long listId) {

        String sql = """
                UPDATE cards
                SET status = ?
                WHERE list_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(
                    1,
                    Enums.CS.PARENT_ARCHIVED.name()
            );

            stmt.setLong(2, listId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to archive child cards",
                    e
            );
        }
    }

    public void updateCardsToParentDeleted(Long listId) {

        String sql = """
                UPDATE cards
                SET status = ?
                WHERE list_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(
                    1,
                    Enums.CS.PARENT_DELETED.name()
            );

            stmt.setLong(2, listId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete child cards",
                    e
            );
        }
    }

    public void deleteCard(Long cardId) {

        String sql = "DELETE FROM cards WHERE card_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cardId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete card",
                    e
            );
        }
    }
}

class CardMapper {

    public Card mapRow(ResultSet rs) throws SQLException {

        Card card = new Card();

        card.setCardId(rs.getLong("card_id"));

        long listId = rs.getLong("list_id");
        card.setColumnId(rs.wasNull() ? null : listId);

        card.setBoardId(rs.getLong("board_id"));

        long position = rs.getLong("card_position");
        card.setCardPosition(
                rs.wasNull() ? null : position
        );

        card.setDescription(
                rs.getString("description")
        );

        card.setHexColor(
                rs.getString("hex_color")
        );

        card.setStatus(
                Enums.CS.valueOf(
                        rs.getString("status")
                )
        );

        return card;
    }
}
