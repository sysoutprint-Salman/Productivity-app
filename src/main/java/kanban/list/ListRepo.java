package kanban.list;

import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import kanban.list.List;

public class ListRepo {

    private final ListMapper listMapper = new ListMapper();

    public List<List> findByStatus(Long boardId, Enum.LS status) {
        String sql = """
                SELECT * FROM lists
                WHERE board_id = ? AND status = ?
                """;

        List<List> lists = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, boardId);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lists.add(listMapper.mapRow(rs));
                }
            }

            return lists;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find lists by status", e);
        }
    }

    public void create(List list) {
        String sql = """
                INSERT INTO lists
                    (board_id, list_position, title, hex_color, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, list.getBoardId());

            if (list.getListPosition() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setLong(2, list.getListPosition());
            }

            stmt.setString(3, list.getTitle());
            stmt.setString(4, list.getHexColor());
            stmt.setString(5, list.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    list.setListId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create list", e);
        }
    }

    public void updateList(Long listId, List newListInfo) {
        String sql = """
                UPDATE lists
                SET board_id = ?,
                    list_position = ?,
                    title = ?,
                    hex_color = ?,
                    status = ?
                WHERE list_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, newListInfo.getBoardId());

            if (newListInfo.getListPosition() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setLong(2, newListInfo.getListPosition());
            }

            stmt.setString(3, newListInfo.getTitle());
            stmt.setString(4, newListInfo.getHexColor());
            stmt.setString(5, newListInfo.getStatus().name());
            stmt.setLong(6, listId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update list", e);
        }
    }

    public void updateListSection(
            Long listId,
            Enum.Section section,
            Object value) {

        String sql;

        switch (section) {
            case TITLE:
                sql = "UPDATE lists SET title = ? WHERE list_id = ?";
                break;

            case POSITION:
                sql = "UPDATE lists SET list_position = ? WHERE list_id = ?";
                break;

            case STATUS:
                sql = "UPDATE lists SET status = ? WHERE list_id = ?";
                break;

            case COLOR:
                sql = "UPDATE lists SET hex_color = ? WHERE list_id = ?";
                break;

            default:
                return;
        }

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, value);
            stmt.setLong(2, listId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update list section", e);
        }
    }

    public void updateStatusAndPosition(
            Long listId,
            Enum.LS status,
            Long position) {

        String sql;

        if (position == null) {
            sql = """
                    UPDATE lists
                    SET status = ?, list_position = NULL
                    WHERE list_id = ?
                    """;
        } else {
            sql = """
                    UPDATE lists
                    SET status = ?, list_position = ?
                    WHERE list_id = ?
                    """;
        }

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            if (position == null) {
                stmt.setLong(2, listId);
            } else {
                stmt.setLong(2, position);
                stmt.setLong(3, listId);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update list status and position",
                    e
            );
        }
    }

    public void updateListPositions(
            List<Long> listIds,
            List<Long> listPositions) {

        if (listIds.size() != listPositions.size()) {
            throw new IllegalArgumentException(
                    "ListId and listPosition lists must be the same length."
            );
        }

        String sql = """
                UPDATE lists
                SET list_position = ?
                WHERE list_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                for (int i = 0; i < listIds.size(); i++) {

                    if (listPositions.get(i) == null) {
                        stmt.setNull(1, Types.INTEGER);
                    } else {
                        stmt.setLong(1, listPositions.get(i));
                    }

                    stmt.setLong(2, listIds.get(i));
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
                    "Failed to update list positions",
                    e
            );
        }
    }

    public void deleteList(Long listId) {
        String sql = "DELETE FROM lists WHERE list_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, listId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete list", e);
        }
    }

    public List<List> findAllListsByBoardId(Long boardId) {
        String sql = "SELECT * FROM lists WHERE board_id = ?";

        List<List> lists = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, boardId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lists.add(listMapper.mapRow(rs));
                }
            }

            return lists;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find lists by board",
                    e
            );
        }
    }

    public List findListById(Long listId) {
        String sql = "SELECT * FROM lists WHERE list_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, listId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return listMapper.mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find list", e);
        }
    }
}

class ListMapper {

    public List mapRow(ResultSet rs) throws SQLException {

        kanban.list.List list = new kanban.list.List();

        list.setListId(rs.getLong("list_id"));
        list.setBoardId(rs.getLong("board_id"));

        long position = rs.getLong("list_position");

        if (rs.wasNull()) {
            list.setListPosition(null);
        } else {
            list.setListPosition(position);
        }

        list.setTitle(rs.getString("title"));
        list.setHexColor(rs.getString("hex_color"));
        list.setStatus(
                Enum.LS.valueOf(rs.getString("status"))
        );

        return list;
    }
}
