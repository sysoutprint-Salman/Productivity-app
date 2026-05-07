package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KanbanController {
}



@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
class BoardController {
    private final BoardRepository boardRepository;

    @GetMapping("/user/{userId}")
    protected List<Board> getAllBoardsByUserId(@PathVariable Long userId) {
        return boardRepository.findAllByUserId(userId);
    }

    @PostMapping
    protected void createBoard(@RequestBody Board boardInfo) {
        boardRepository.createBoard(boardInfo);
    }

    @GetMapping("/{boardId}")
    protected Board getBoardById(@PathVariable Long boardId) {
        return boardRepository.getBoardById(boardId);
    }

    @PatchMapping("/{boardId}")
    protected void updateBoardTitle(@PathVariable Long boardId, @RequestBody String newTitle){
        boardRepository.updateBoardTitle(boardId, newTitle);
    }

    @DeleteMapping("/{boardId}")
    protected void deleteBoard(@PathVariable Long boardId){
        boardRepository.deleteBoard(boardId);
    }
}
@Repository
class BoardRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final BoardRowMapper boardRowMapper = new BoardRowMapper();

    protected List<Board> findAllByUserId(Long userId) {
        return jdbc.query("SELECT * FROM boards WHERE user_id = ?",
                boardRowMapper, userId);
    }

    protected Board getBoardById(Long boardId) {
        return jdbc.queryForObject(
                "SELECT * FROM boards WHERE board_id = ?",
                boardRowMapper, boardId
        );
    }

    protected void createBoard(Board board) {
        jdbc.update(
                "INSERT INTO boards (board_title, user_id, creation_date) VALUES (?, ?, ?)",
                board.getBoardTitle(),
                board.getUserId(),
                board.getCreationDate()
        );
    }
    protected void deleteBoard(Long boardId){
        jdbc.update("DELETE from boards WHERE board_id = ?", boardId);
    }

    protected void updateBoardTitle(Long boardId, String newTitle){
        jdbc.update("UPDATE boards SET board_title = ? WHERE board_id = ?", newTitle, boardId);
    }
}
class BoardRowMapper implements RowMapper<Board> {
    @Override
    public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Board(
                rs.getLong("board_id"),
                rs.getLong("user_id"),
                rs.getString("board_title"),
                rs.getObject("creation_date", LocalDateTime.class)
        );
    }
}



@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
class KListController {
    private final KListRepository listRepository;

    @GetMapping("/all/{boardId}")
    protected List<KList> findAllListsByBoardId(@PathVariable Long boardId) {
        return listRepository.findAllListsByBoardId(boardId);
    }
    @GetMapping("/all/{boardId}/condition")
    protected List<KList> findByStatus(@PathVariable Long boardId, @RequestParam Enum.LS status){
        return listRepository.findByStatus(boardId, status);
    }
    @GetMapping("/{listId}")
    protected KList findListById(@PathVariable Long listId) {
        return listRepository.findListById(listId);
    }
    @PostMapping
    protected void createList(@RequestBody KList list) {
        listRepository.create(list);
    }

    @PutMapping("/{listId}")
    protected void updateList(@PathVariable Long listId, @RequestBody KList newListInfo){
        listRepository.updateList(listId, newListInfo);
    }

    @PatchMapping("/{listId}/modular")
    protected void updateListSection(@PathVariable Long listId, @RequestParam Enum.Section section, @RequestBody KList newListInfo ){
        Object valueToUpdate = switch (section) {
            case TITLE -> newListInfo.getTitle();
            case POSITION -> newListInfo.getListPosition();
            case STATUS -> newListInfo.getStatus().name();
            case COLOR -> newListInfo.getHexColor();
            default -> null;
        };
        if (valueToUpdate == null){
            throw new NullPointerException("SpringBoot: Null value detected, list couldn't be updated.");
        } else {
            listRepository.updateListSection(listId,section,valueToUpdate);
        }
    }
    @PatchMapping("/reorder")
    protected void updateListPositions(@RequestBody DTO.ListReorder listDTO){
        System.out.println("Controller test" + listDTO.getListIds() + "\n" + listDTO.getListPositions());
        listRepository.updateListPositions(listDTO.getListIds(), listDTO.getListPositions());
    }
    @PatchMapping("/{listId}/status")
    public void updateStatusAndPosition(@PathVariable Long listId, @RequestParam Enum.LS status, @RequestParam(required = false) Long position) {
        listRepository.updateStatusAndPosition(listId, status, position);
    }

    @DeleteMapping("/{listId}")
    protected void deleteList(@PathVariable Long listId){
        listRepository.deleteList(listId);
    }
}
@Repository
class KListRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final KListRowMapper kListRowMapper = new KListRowMapper();


    protected List<KList> findByStatus(Long boardId, Enum.LS status){
        return jdbc.query("SELECT * FROM lists WHERE board_id = ? AND status = ?",
                kListRowMapper, boardId, status.name());
    }
    protected void create(KList list) {
        jdbc.update(
                "INSERT INTO lists (board_id, list_position, title, hex_color, status) VALUES (?, ?, ?, ?, ?)",
                list.getBoardId(),
                list.getListPosition(),
                list.getTitle(),
                list.getHexColor(),
                list.getStatus().name()
        );
    }
    protected void updateList(Long ListId, KList newListInfo){
        jdbc.update(
                "UPDATE lists SET board_id = ?, list_position = ?, title = ?, hex_color = ?, status = ? " +
                        "WHERE list_id = ?",
                newListInfo.getBoardId(),
                newListInfo.getListPosition(),
                newListInfo.getTitle(),
                newListInfo.getHexColor(),
                newListInfo.getStatus().name(),
                ListId
        );
    }
    protected void updateListSection(Long ListId, Enum.Section section, Object value){
        String query;
        switch (section){
            case TITLE : query = "UPDATE lists SET title = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
            case POSITION : query = "UPDATE lists SET list_position = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
            case STATUS: query = "UPDATE lists SET status = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
            case COLOR: query = "UPDATE lists SET hex_color = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
        }
    }
    protected void updateStatusAndPosition(Long listId, Enum.LS status, Long position) {
        String query;
        if (position == null) {
            query = "UPDATE lists SET status = ?, list_position = NULL WHERE list_id = ?";
            jdbc.update(query, status.name(), listId);
        } else {
            query = "UPDATE lists SET status = ?, list_position = ? WHERE list_id = ?";
            jdbc.update(query, status.name(), position, listId);
        }
    }

    //Rollback on any unchecked exception
    @Transactional(rollbackFor = Exception.class)
    protected void updateListPositions(List<Long> listIds, List<Long> listPostions){
        if (listIds.size() != listPostions.size()){
            throw new IllegalArgumentException("ListId and listPosition Arraylists must be the same length.");
        }
        String query = "UPDATE lists SET list_position = ? WHERE list_id = ?";
        List<Object[]> batch = new ArrayList<>();
        for (int i = 0; i < listIds.size(); i++){
            batch.add(new Object[]{ listPostions.get(i), listIds.get(i) });
        }
        jdbc.batchUpdate(query, batch);
    }

    protected void deleteList(Long listId){
        jdbc.update("DELETE FROM lists where list_id = ?", listId);
    }

    public List<KList> findAllListsByBoardId(Long boardId) {
        return jdbc.query("SELECT * FROM lists where board_id = ?", kListRowMapper, boardId);
    }

    public KList findListById(Long listId) {
        return jdbc.queryForObject("SELECT * FROM lists where list_id = ?", kListRowMapper, listId);

    }
}
class KListRowMapper implements RowMapper<KList> {
    @Override
    public KList mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new KList(
                rs.getLong("list_id"),
                rs.getLong("board_id"),
                rs.getLong("list_position"),
                rs.getString("title"),
                rs.getString("hex_color"),
                Enum.LS.valueOf(rs.getString("status"))
        );
    }
}



@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
class CardController {
    private final CardRepository cardRepository;

    @PostMapping
    protected Card createCard(@RequestBody Card card) {
        return cardRepository.createCard(card);
    }
    @GetMapping("/lists/{listId}/cards")
    protected List<Card> findAllCardsByListId(@PathVariable Long listId) {
        return cardRepository.findAllCardsByListId(listId);
    }

    @GetMapping("/{boardId}/status")
    protected List<Card> findByStatus(@PathVariable Long boardId, @RequestParam Enum.CS cardStatus){
        return cardRepository.findByStatus(boardId, cardStatus);

    }
    @PatchMapping("/{cardId}/modular")
    protected void updateCardSection(@PathVariable Long cardId, @RequestBody Card newCardInfo, @RequestParam Enum.Section section){
        Object valueToUpdate = switch (section){
            case ID -> newCardInfo.getListId();
            case DESCRIPTION -> newCardInfo.getDescription();
            case COLOR -> newCardInfo.getHexColor();
            case POSITION -> newCardInfo.getCardPosition();
            case STATUS -> newCardInfo.getStatus().name();
            default -> null;
        };
        if (!(valueToUpdate == null)){
            cardRepository.updateCardSection(cardId,valueToUpdate,section);
        } else {
            System.out.println("SpringBoot: Null value detected, card couldn't be updated.");
        }
    }

    @PatchMapping("/batch/modular")
    protected void updateCardSectionBatch(
            @RequestBody List<Card> cardsToUpdate,
            @RequestParam Enum.Section section
    ) {
        cardRepository.updateCardSectionBatch(cardsToUpdate, section);
    }

    @PatchMapping("/{cardId}/inbox")
    protected void updateCardToInboxed(@PathVariable Long cardId) {
        cardRepository.updateCardToInboxed(cardId);
    }
    @DeleteMapping("/{cardId}")
    protected void deleteCard(@PathVariable Long cardId){
        cardRepository.deleteCard(cardId);
    }
}
@Repository
class CardRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final CardRowMapper cardRowMapper = new CardRowMapper();

    protected List<Card> findAllCardsByListId(Long listId) {
        return jdbc.query(
                "SELECT * FROM cards WHERE list_id = ?",
                cardRowMapper, listId
        );
    }

    protected List<Card> findByStatus(Long boardId, Enum.CS status){
        return jdbc.query("SELECT * FROM cards WHERE board_id = ? AND status = ?",
                cardRowMapper, boardId, status.name());
    }

    protected Card createCard(Card card) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        """
                                INSERT INTO cards (list_id, board_id, card_position, description, hex_color, status)
                                VALUES (?, ?, ?, ?, ?, ?)
                                """, new String[]{"card_id"});
                statement.setLong(1, card.getListId());
                statement.setLong(2, card.getBoardId());
                statement.setLong(3, card.getCardPosition());
                statement.setString(4, card.getDescription());
                statement.setString(5, card.getHexColor());
                statement.setString(6, card.getStatus().name());
                return statement;
            }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) card.setCardId(generatedId.longValue());
        return card;
    }

    protected void updateCardSection(Long cardId, Object value, Enum.Section section){
        String query;
        switch (section){
            case ID : query = "UPDATE cards SET list_id = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case DESCRIPTION : query = "UPDATE cards SET description = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case POSITION : query = "UPDATE cards SET card_position = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case STATUS: query = "UPDATE cards SET status = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case COLOR: query = "UPDATE cards SET hex_color = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
        }
    }

    protected void updateCardSectionBatch(List<Card> cardsToUpdate, Enum.Section section) {
        String query = switch (section) {
            case ID -> "UPDATE cards SET list_id = ? WHERE card_id = ?";
            case DESCRIPTION -> "UPDATE cards SET description = ? WHERE card_id = ?";
            case COLOR -> "UPDATE cards SET hex_color = ? WHERE card_id = ?";
            case POSITION -> "UPDATE cards SET card_position = ? WHERE card_id = ?";
            case STATUS -> "UPDATE cards SET status = ? WHERE card_id = ?";
            default -> null;
        };

        if (query == null) {
            return;
        }

        jdbc.batchUpdate(query, cardsToUpdate, cardsToUpdate.size(), (ps, card) -> {
            Object valueToUpdate = switch (section) {
                case ID -> card.getListId();
                case DESCRIPTION -> card.getDescription();
                case COLOR -> card.getHexColor();
                case POSITION -> card.getCardPosition();
                case STATUS -> card.getStatus().name();
                default -> null;
            };

            ps.setObject(1, valueToUpdate);
            ps.setLong(2, card.getCardId());
        });
    }

    protected void updateCardToInboxed(Long cardId) {
        String query = """
            UPDATE cards
            SET card_position = NULL,
                status = 'INBOXED',
                list_id = NULL
            WHERE card_id = ?
            """;

        jdbc.update(query, cardId);
    }

    protected void deleteCard(Long cardId){
        jdbc.update("DELETE FROM cards WHERE card_id = ?", cardId);
    }
}
class CardRowMapper implements RowMapper<Card> {
    @Override
    public Card mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Card(
                rs.getLong("card_id"),
                rs.getLong("list_id"),
                rs.getLong("board_id"),
                rs.getLong("card_position"),
                rs.getString("description"),
                rs.getString("hex_color"),
                Enum.CS.valueOf(rs.getString("status"))
        );
    }
}



@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
class ReminderController {

    private final ReminderRepository reminderRepository;
    @GetMapping("/board/{boardId}")
    protected List<Reminder> getRemindersByBoard(@PathVariable Long boardId) {
        return reminderRepository.findByBoardId(boardId);
    }

    @PostMapping
    protected void createReminder(@RequestBody Reminder reminder) {
        reminderRepository.create(reminder);
    }

    @DeleteMapping("/{reminderId}")
    protected void deleteReminder(@PathVariable Long reminderId) {
        reminderRepository.delete(reminderId);
    }

    @PatchMapping("/{reminderId}")
    protected void updateReminderSection(
            @PathVariable Long reminderId,
            @RequestParam Enum.Section section,
            @RequestBody Reminder reminder
    ) {
        reminderRepository.update(reminderId, section, reminder);

    }
}
@Repository
class ReminderRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final ReminderRowMapper reminderRowMapper = new ReminderRowMapper();
    protected List<Reminder> findByBoardId(Long boardId) {
        return jdbc.query(
                "SELECT * FROM reminders WHERE board_id = ?",
                reminderRowMapper,
                boardId
        );
    }

    protected void create(Reminder reminder) {
        jdbc.update(
                """
                INSERT INTO reminders (board_id, reminder_title, description, priority, due_date)
                VALUES (?, ?, ?, ?, ?)
                """,
                reminder.getBoardId(),
                reminder.getReminderTitle(),
                reminder.getDescription(),
                reminder.getPriority().name(),
                reminder.getDueDate()
        );
    }

    protected void delete(Long reminderId) {
        jdbc.update(
                "DELETE FROM reminders WHERE reminder_id = ?",
                reminderId
        );
    }

    protected void update(Long reminderId, Enum.Section section, Reminder reminder) {
        switch (section) {
            case TITLE -> {
                if (reminder.getReminderTitle() == null) return;
                jdbc.update(
                        "UPDATE reminders SET reminder_title = ? WHERE reminder_id = ?",
                        reminder.getReminderTitle(),
                        reminderId
                );
            }

            case DESCRIPTION -> {
                if (reminder.getDescription() == null) return;
                jdbc.update(
                        "UPDATE reminders SET description = ? WHERE reminder_id = ?",
                        reminder.getDescription(),
                        reminderId
                );
            }

            case PRIORITY -> {
                if (reminder.getPriority() == null) return;
                jdbc.update(
                        "UPDATE reminders SET priority = ? WHERE reminder_id = ?",
                        reminder.getPriority().name(),
                        reminderId
                );
            }

            case DUE_DATE -> {
                if (reminder.getDueDate() == null) return;
                jdbc.update(
                        "UPDATE reminders SET due_date = ? WHERE reminder_id = ?",
                        reminder.getDueDate(),
                        reminderId
                );
            }
        }
    }
    //DELETE reminder
    //PUT
}
class ReminderRowMapper implements RowMapper<Reminder> {
    @Override
    public Reminder mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Reminder(
                rs.getLong("reminder_id"),
                rs.getLong("board_id"),
                rs.getString("reminder_title"),
                rs.getString("description"),
                Reminder.Priority.valueOf(rs.getString("priority")),
                rs.getObject("due_date", LocalDateTime.class)
        );
    }
}

