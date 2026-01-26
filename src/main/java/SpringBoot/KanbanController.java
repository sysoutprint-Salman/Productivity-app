package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class KanbanController {
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
class BoardController {
    private final BoardRepository boardRepository;

    @GetMapping("/user/{userId}")
    protected List<Board> getAllBoardsByUserId(@PathVariable Long userId) {
        return boardRepository.findByUserId(userId);
    }

    @PostMapping
    protected void createBoard(@RequestBody Board board) {
        boardRepository.createBoard(board);
    }

    @GetMapping("/{boardId}")
    protected Board getBoardById(@PathVariable Long boardId) {
        return boardRepository.findById(boardId);
    }

    @PutMapping("/{boardId}")
    protected void updateBoardTitle(@PathVariable Long id, @RequestBody String newTitle){
        boardRepository.updateBoardTitle(id, newTitle);
    }
}

@Repository
class BoardRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final BoardRowMapper boardRowMapper = new BoardRowMapper();

    protected List<Board> findByUserId(Long userId) {
        return jdbc.query("SELECT * FROM boards WHERE user_id = ?",
                boardRowMapper, userId);
    }

    protected Board findById(Long boardId) {
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
    protected void deleteBoard(Long id){
        jdbc.update("DELETE from boards WHERE id = ?", id);
    }

    protected void updateBoardTitle(Long id, String newTitle){
        jdbc.update("UPDATE board SET board_title = ? WHERE id = ?", newTitle, id);
    }

    //DELETE board
    //UPDATE boardtitle
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
public class KListController {


    private final KListRepository listRepository;
    @GetMapping("/board/{boardId}")
    protected List<KList> getListsByBoard(@PathVariable Long boardId) {
        return listRepository.findByBoardId(boardId);
    }

    @PostMapping
    protected void createList(@RequestBody KList list) {
        listRepository.create(list);
    }
}

@Repository
class KListRepository {


    @Autowired
    private JdbcTemplate jdbc;
    protected List<KList> findByBoardId(Long boardId) {
        return jdbc.query(
                "SELECT * FROM lists WHERE board_id = ?",
                new KListRowMapper(), boardId
        );
    }

    protected void create(KList list) {
        jdbc.update(
                "INSERT INTO lists (user_id, board_id, title, hex_color, status) VALUES (?, ?, ?, ?, ?)",
                list.getUserId(),
                list.getBoardId(),
                list.getTitle(),
                list.getHexColor(),
                list.getStatus().name()
        );
    }
    //DELETE list
    //Change STATUS to archive
}



class KListRowMapper implements RowMapper<KList> {
    @Override
    public KList mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new KList(
                rs.getLong("list_id"),
                rs.getLong("user_id"),
                rs.getLong("board_id"),
                rs.getString("title"),
                rs.getString("hex_color"),
                KList.ListStatus.valueOf(rs.getString("status"))
        );
    }
}

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {


    private final CardRepository cardRepository;
    @GetMapping("/list/{listId}")
    protected List<Card> getCardsByList(@PathVariable Long listId) {
        return cardRepository.findByListId(listId);
    }

    @PostMapping
    protected void createCard(@RequestBody Card card) {
        cardRepository.create(card);
    }

}

@Repository
class CardRepository {


    @Autowired
    private JdbcTemplate jdbc;
    protected List<Card> findByListId(Long listId) {
        return jdbc.query(
                "SELECT * FROM cards WHERE list_id = ?",
                new CardRowMapper(), listId
        );
    }

    protected void create(Card card) {
        jdbc.update(
                "INSERT INTO cards (user_id, list_id, board_id, description, hex_color, status) VALUES (?, ?, ?, ?, ?, ?)",
                card.getUserId(),
                card.getListId(),
                card.getBoardId(),
                card.getDescription(),
                card.getHexColor(),
                card.getStatus().name()
        );
    }
    //DELETE card
    //UPDATE card info
    //Change STATUS to ARCHIVE or PARENT_ARCHIVE
}

class CardRowMapper implements RowMapper<Card> {
    @Override
    public Card mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Card(
                rs.getLong("card_id"),
                rs.getLong("user_id"),
                rs.getLong("list_id"),
                rs.getLong("board_id"),
                rs.getString("description"),
                rs.getString("hex_color"),
                Card.CardStatus.valueOf(rs.getString("status"))
        );
    }
}

@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class ReminderController {


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
    protected List<Reminder> findByBoardId(Long boardId) {
        return jdbc.query(
                "SELECT * FROM reminders WHERE board_id = ?",
                new ReminderRowMapper(), boardId
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
}
