package kanban.boards;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.testng.Assert.*;

public class BoardsTest {

    private final BoardService boardService = new BoardService();

    private static final Long USER_ID = 2L;

    @Test
    public void findAllByUserId() {
        List<Board> boards =
                boardService.findAllByUserId(USER_ID);

        assertNotNull(boards);
        assertTrue(
                boards.stream()
                        .allMatch(board ->
                                board.getUserId().equals(USER_ID))
        );
    }

    @Test
    public void getBoardById() {
        List<Board> boards =
                boardService.findAllByUserId(USER_ID);

        assertFalse(
                boards.isEmpty(),
                "User 2 needs an existing board for this test."
        );

        Long boardId = boards.get(0).getBoardId();

        Board board =
                boardService.getBoardById(boardId);

        assertNotNull(board);
        assertEquals(boardId, board.getBoardId());
        assertEquals(USER_ID, board.getUserId());
    }

    @Test
    public void createAndDeleteBoard() {

        Board board = new Board(
                null,
                USER_ID,
                "JUnit Test Board",
                LocalDateTime.now()
        );

        Long boardId =
                boardService.createBoard(board);

        assertNotNull(boardId);

        Board created =
                boardService.getBoardById(boardId);

        assertNotNull(created);
        assertEquals("JUnit Test Board",
                created.getBoardTitle());
        assertEquals(USER_ID,
                created.getUserId());

        boardService.deleteBoard(boardId);

        assertNull(
                boardService.getBoardById(boardId)
        );
    }

    @Test
    public void updateBoardTitle() {

        Board board = new Board(
                null,
                USER_ID,
                "Temporary Board",
                LocalDateTime.now()
        );

        Long boardId =
                boardService.createBoard(board);

        boardService.updateBoardTitle(
                boardId,
                "Updated Board"
        );

        Board updated =
                boardService.getBoardById(boardId);

        assertEquals(
                "Updated Board",
                updated.getBoardTitle()
        );

        boardService.deleteBoard(boardId);
    }
}
