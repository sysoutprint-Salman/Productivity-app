package SpringBoot;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static SpringBoot.KanbanController.Section.*;

public class KanbanController {
    protected enum Section {ID, TITLE, DESCRIPTION, COLOR, STATUS, POSITION}
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

    @PutMapping("/{boardId}")
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
                board.getCreationDate(),
                board.getUserId()
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
    @GetMapping("/{listId}")
    protected KList findListById(@PathVariable Long listId) {
        return listRepository.findListById(listId);
    }

    @PostMapping
    protected void createList(@RequestBody KList list) {
        listRepository.create(list);
        System.out.println("SpringBoot: List created successfully.");
    }

    @PutMapping("/{listId}")
    protected void updateList(@PathVariable Long listId, @RequestBody KList newListInfo){
        listRepository.updateList(listId, newListInfo);
        System.out.println("SpringBoot: List updated successfully.");
    }

    @PutMapping("/{listId}/modular")
    protected void updateListSection(@PathVariable Long listId, @RequestParam KanbanController.Section section, KList newListInfo){
        Object valueToUpdate = switch (section) {
            case TITLE -> newListInfo.getTitle();
            case POSITION -> newListInfo.getPosition();
            case STATUS -> newListInfo.getStatus();
            case COLOR -> newListInfo.getHexColor();
            default -> null;
        };
        if (valueToUpdate == null){
            System.out.println("SpringBoot: Null value detected, list couldn't be updated.");
        } else {
            listRepository.updateListSection(listId,valueToUpdate,section);
            System.out.println("SpringBoot: List section sucessfully updated.");
        }

    }
}

@Repository
class KListRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final KListRowMapper kListRowMapper = new KListRowMapper();


    protected List<KList> findByStatus(Long userId, Long boardId, KList.ListStatus status){
        return jdbc.query("SELECT * FROM lists WHERE user_id = ? AND board_id = ? AND status = ?",
                kListRowMapper, userId, boardId, status);
    }


    protected void create(KList list) {
        jdbc.update(
                "INSERT INTO lists (user_id, board_id, position, title, hex_color, status) VALUES (?, ?, ?, ?, ?, ?)",
                list.getUserId(),
                list.getBoardId(),
                list.getPosition(),
                list.getTitle(),
                list.getHexColor(),
                list.getStatus().name()
        );
    }
    protected void updateList(Long ListId, KList newListInfo){
        jdbc.update(
                "UPDATE lists SET user_id = ?, board_id = ?, position = ?, title = ?, hex_color = ?, status = ?" +
                        "WHERE list_id = ?",
                newListInfo.getUserId(),
                newListInfo.getBoardId(),
                newListInfo.getPosition(),
                newListInfo.getTitle(),
                newListInfo.getHexColor(),
                newListInfo.getStatus().name(),
                ListId
        );
    }
    protected void updateListSection(Long ListId, Object value, KanbanController.Section section){
        String query;
        switch (section){
            case TITLE : query = "UPDATE lists SET title = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
            case POSITION : query = "UPDATE lists SET position = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
            case STATUS: query = "UPDATE lists SET status = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
            case COLOR: query = "UPDATE lists SET color = ? WHERE list_id = ?";
                jdbc.update(query, value, ListId);
                break;
        }
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
                rs.getLong("user_id"),
                rs.getLong("board_id"),
                rs.getLong("position"),
                rs.getString("title"),
                rs.getString("hex_color"),
                KList.ListStatus.valueOf(rs.getString("status"))
        );
    }
}

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
class CardController {
    private final CardRepository cardRepository;

    @PostMapping
    protected void createCard(@RequestBody Card card) {
        cardRepository.createCard(card);
    }
    @GetMapping("/parentList/{listId}")
    protected List<Card> getCardsByList(@PathVariable Long listId) {
        return cardRepository.findAllCardsByListId(listId);
    }

    @GetMapping("/status")
    protected List<Card> findByStatus(Long userId, Long boardId, @RequestParam Card.CardStatus cardStatus){
        return cardRepository.findByStatus(userId, boardId, cardStatus);
    }
    @PutMapping("/{cardId}/modular")
    protected void updateCardSection(@PathVariable Long cardId, @RequestBody Card newCardInfo, @RequestParam KanbanController.Section section){
        Object valueToUpdate = switch (section){
            case ID -> newCardInfo.getListId();
            case DESCRIPTION -> newCardInfo.getDescription();
            case COLOR -> newCardInfo.getHexColor();
            case POSITION -> newCardInfo.getPosition();
            default -> null;
        };
        if (!(valueToUpdate == null)){
            cardRepository.updateCardSection(cardId,valueToUpdate,section);
        } else {
            System.out.println("SpringBoot: Null value detected, card couldn't be updated.");
        }
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

    protected List<Card> findByStatus(Long userId, Long boardId, Card.CardStatus status){
        return jdbc.query("SELECT * FROM cards WHERE user_id = ? AND board_id = ? AND status = ?",
                cardRowMapper, userId, boardId, status);
    }
    protected void createCard(Card card) {
        jdbc.update(
                "INSERT INTO cards (user_id, list_id, board_id, position, description, hex_color, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                card.getUserId(),
                card.getListId(),
                card.getBoardId(),
                card.getPosition(),
                card.getDescription(),
                card.getHexColor(),
                card.getStatus().name()
        );
    }

    protected void updateCardSection(Long cardId, Object value, KanbanController.Section section){
        String query;
        switch (section){
            case ID : query = "UPDATE cards SET list_id = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case DESCRIPTION : query = "UPDATE cards SET description = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case POSITION : query = "UPDATE cards SET position = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case STATUS: query = "UPDATE cards SET status = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
            case COLOR: query = "UPDATE cards SET color = ? WHERE card_id = ?";
                jdbc.update(query, value, cardId);
                break;
        }
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
                rs.getLong("user_id"),
                rs.getLong("list_id"),
                rs.getLong("board_id"),
                rs.getLong("position"),
                rs.getString("description"),
                rs.getString("hex_color"),
                Card.CardStatus.valueOf(rs.getString("status"))
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
}

@Repository
class ReminderRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final ReminderRowMapper reminderRowMapper = new ReminderRowMapper();
    protected List<Reminder> findByBoardId(Long boardId) {
        return jdbc.query(
                "SELECT * FROM reminders WHERE board_id = ?",
                reminderRowMapper, boardId
        );
    }

    protected void create(Reminder reminder) {
        jdbc.update(
                "INSERT INTO reminders (board_id, reminder_title, description, priority, date_time) VALUES (?, ?, ?, ?, ?)",
                reminder.getBoardId(),
                reminder.getReminderTitle(),
                reminder.getDescription(),
                reminder.getPriority().name(),
                reminder.getDateTime()
        );
    }
    //DELETE reminder
    //UPDATE
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
                rs.getObject("date_time", LocalDateTime.class)
        );
    }
}

