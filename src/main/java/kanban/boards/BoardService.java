package kanban.boards;


import java.util.List;
import java.util.Map;

public class BoardService {

    private final BoardRepo boardRepo;

    public BoardService() {
        boardRepo = new BoardRepo();
    }

    public List<Board> findAllByUserId(Long userId) {
        return boardRepo.findAllByUserId(userId);
    }

    public Board getBoardById(Long boardId) {
        return boardRepo.getBoardById(boardId);
    }

    public Long createBoard(Board board) {
        return boardRepo.createBoard(board);
    }

    public void createLoadedBoard(Board board,
            List<Map<String, Object>> loadedContent) {
        boardRepo.createLoadedBoard(board, loadedContent);
    }

    public void deleteBoard(Long boardId) {
        boardRepo.deleteBoard(boardId);
    }

    public void updateBoardTitle(Long boardId, String newTitle) {
        boardRepo.updateBoardTitle(boardId, newTitle);
    }
}
