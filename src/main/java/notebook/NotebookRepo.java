package notebook;

import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotebookRepo {

    private final NotebookMapper notebookMapper = new NotebookMapper();

    public List<Notebook> getAllNotebooks() {
        String sql = "SELECT * FROM notebooks";

        List<Notebook> notebooks = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                notebooks.add(notebookMapper.mapRow(rs));
            }

            return notebooks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve notebooks", e);
        }
    }

    public List<Notebook> findByUserId(Long userId) {
        String sql = "SELECT * FROM notebooks WHERE user_id = ?";

        List<Notebook> notebooks = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notebooks.add(notebookMapper.mapRow(rs));
                }
            }

            return notebooks;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve notebooks for user",
                    e
            );
        }
    }

    public Notebook getNotebook(Long id) {
        String sql = "SELECT * FROM notebooks WHERE notebook_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return notebookMapper.mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve notebook", e);
        }
    }

    public void createNotebook(Notebook notebook) {
        String sql = """
                INSERT INTO notebooks
                    (tab_title, notebook_text, user_id, hex_color)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, notebook.getTabTitle());
            stmt.setString(2, notebook.getNotebookText() != null ? notebook.getNotebookText() : "");
            stmt.setLong(3, notebook.getUserId());
            stmt.setString(4, notebook.getHexColor());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    notebook.setNotebookId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create notebook", e);
        }
    }

    public void updateNotebookText(Long id, Notebook notebook) {
        String sql = """
                UPDATE notebooks
                SET notebook_text = ?
                WHERE notebook_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notebook.getNotebookText());
            stmt.setLong(2, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update notebook text",
                    e
            );
        }
    }

    public void updateNotebookTab(Long id, Notebook notebook) {
        String sql = """
                UPDATE notebooks
                SET tab_title = ?, hex_color = ?
                WHERE notebook_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notebook.getTabTitle());
            stmt.setString(2, notebook.getHexColor());
            stmt.setLong(3, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update notebook tab",
                    e
            );
        }
    }

    public void deleteNotebook(Long id) {
        String sql = "DELETE FROM notebooks WHERE notebook_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete notebook", e);
        }
    }
}
class NotebookMapper {

    public Notebook mapRow(ResultSet rs) throws SQLException {
        Notebook notebook = new Notebook();

        notebook.setNotebookId(rs.getLong("notebook_id"));
        notebook.setUserId(rs.getLong("user_id"));
        notebook.setTabTitle(rs.getString("tab_title"));
        notebook.setNotebookText(rs.getString("notebook_text"));
        notebook.setHexColor(rs.getString("hex_color"));

        return notebook;
    }
}
