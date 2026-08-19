package kanban.column;

import database.DatabaseManager;
import kanban.Enums;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColumnRepo {

    private final ListMapper listMapper = new ListMapper();

    public List<Column> findByStatus(Long boardId, Enums.LS status) {
        String sql = """
                SELECT * FROM lists
                WHERE board_id = ? AND status = ?
                """;

        List<Column> lists = new ArrayList<>();

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

    public void create(Column column) {
        String sql = """
                INSERT INTO lists
                    (board_id, list_position, title, hex_color, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, column.getBoardId());

            if (column.getColumnPosition() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setLong(2, column.getColumnPosition());
            }

            stmt.setString(3, column.getTitle());
            stmt.setString(4, column.getHexColor());
            stmt.setString(5, column.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    column.setColumnId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create list", e);
        }
    }

    public void updateColumn(Long listId, Column newColumnInfo) {
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

            stmt.setLong(1, newColumnInfo.getBoardId());

            if (newColumnInfo.getColumnPosition() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setLong(2, newColumnInfo.getColumnPosition());
            }

            stmt.setString(3, newColumnInfo.getTitle());
            stmt.setString(4, newColumnInfo.getHexColor());
            stmt.setString(5, newColumnInfo.getStatus().name());
            stmt.setLong(6, listId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update list", e);
        }
    }

    public void updateColumnSection(Long listId, Enums.Section section, Object value) {

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

    public void updateStatusAndPosition(Long listId, Enums.LS status, Long position) {

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

    public void updateColumnPositions(List<Column> columnsToUpdate) {

        String sql = """
            UPDATE lists
            SET list_position = ?
            WHERE list_id = ?
            """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {

                for (Column column : columnsToUpdate) {

                    if (column.getColumnPosition() == null) {
                        stmt.setNull(1, Types.INTEGER);
                    } else {
                        stmt.setLong(
                                1,
                                column.getColumnPosition()
                        );
                    }

                    stmt.setLong(
                            2,
                            column.getColumnId()
                    );

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
                    "Failed to update column positions",
                    e
            );
        }
    }

    public void deleteColumn(Long listId) {
        String sql = "DELETE FROM lists WHERE list_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, listId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete list", e);
        }
    }

    public List<Column> findAllColumnsByBoardId(Long boardId) {
        String sql = "SELECT * FROM lists WHERE board_id = ?";

        List<Column> lists = new ArrayList<>();

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

    public Column findColumnById(Long listId) {
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

    public Column mapRow(ResultSet rs) throws SQLException {

        Column column = new Column();

        column.setColumnId(rs.getLong("list_id"));
        column.setBoardId(rs.getLong("board_id"));

        long position = rs.getLong("list_position");

        if (rs.wasNull()) column.setColumnPosition(null);
        else column.setColumnPosition(position);

        column.setTitle(rs.getString("title"));
        column.setHexColor(rs.getString("hex_color"));
        column.setStatus(Enums.LS.valueOf(rs.getString("status")));

        return column;
    }
}
