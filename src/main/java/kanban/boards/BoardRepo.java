package kanban.boards;


import database.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoardRepo {

    private final BoardMapper boardMapper = new BoardMapper();

    public List<Board> findAllByUserId(Long userId) {
        String sql = "SELECT * FROM boards WHERE user_id = ?";

        List<Board> boards = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    boards.add(boardMapper.mapRow(rs));
                }
            }

            return boards;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve boards", e);
        }
    }

    public Board getBoardById(Long boardId) {
        String sql = "SELECT * FROM boards WHERE board_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, boardId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return boardMapper.mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve board", e);
        }
    }

    public Long createBoard(Board board) {
        String sql = """
                INSERT INTO boards
                    (board_title, user_id, creation_date)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, board.getBoardTitle());
            stmt.setLong(2, board.getUserId());

            if (board.getCreationDate() == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(
                        3,
                        board.getCreationDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
            }

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    board.setBoardId(id);
                    return id;
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create board", e);
        }
    }

    public void createLoadedBoard(Board board,
            List<Map<String, Object>> loadedContent) {
        createBoard(board);
    }

    public void deleteBoard(Long boardId) {
        String sql = "DELETE FROM boards WHERE board_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, boardId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete board", e);
        }
    }

    public void updateBoardTitle(Long boardId, String newTitle) {
        String sql = """
                UPDATE boards
                SET board_title = ?
                WHERE board_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTitle);
            stmt.setLong(2, boardId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update board title", e);
        }
    }
}

class BoardMapper {

    public Board mapRow(ResultSet rs) throws SQLException {

        Board board = new Board();

        board.setBoardId(rs.getLong("board_id"));
        board.setUserId(rs.getLong("user_id"));
        board.setBoardTitle(rs.getString("board_title"));

        String creationDate = rs.getString("creation_date");

        if (creationDate != null) {
            board.setCreationDate(
                    LocalDateTime.parse(
                            creationDate,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
            );
        }

        return board;
    }
}
