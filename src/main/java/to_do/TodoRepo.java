package to_do;

import SpringBoot.Task;
import database.DatabaseManager;

import java.sql.*;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TodoRepo {

    private final TodoRowMapper rowMapper = new TodoRowMapper();

    public List<Task> findAllByUserId(Long userId) {

        String sql = """
            SELECT *
            FROM tasks
            WHERE user_id = ?
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    tasks.add(rowMapper.mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve tasks", e);
        }

        return tasks;
    }


    public Task getById(Long id) {

        String sql = """
            SELECT *
            FROM tasks
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve task", e);
        }

        return null;
    }


    public List<Task> getByUserIdAndStatus(
            Long userId,
            Task.Status status) {

        String sql = """
            SELECT *
            FROM tasks
            WHERE user_id = ?
            AND status = ?
            ORDER BY id
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, status.name());

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    tasks.add(rowMapper.mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve tasks", e);
        }

        return tasks;
    }


    public void createTask(Task task) {

        String sql = """
            INSERT INTO tasks
            (user_id, title, description, date, status, creation_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, task.getUserId());
            statement.setString(2, task.getTitle());
            statement.setString(3, task.getDescription());

            // ISO-8601
            statement.setString(
                    4,
                    task.getDate() != null
                            ? task.getDate().toString()
                            : null
            );

            statement.setString(5, task.getStatus().name());

            // ISO-8601
            statement.setString(
                    6,
                    task.getCreationDate() != null
                            ? task.getCreationDate().toString()
                            : null
            );

            statement.executeUpdate();

            // Get SQLite-generated ID
            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    task.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create task", e);
        }
    }


    public void updateTask(Task task) {

        String sql = """
            UPDATE tasks
            SET title = ?,
                description = ?,
                date = ?,
                status = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());

            statement.setString(
                    3,
                    task.getDate() != null
                            ? task.getDate().toString()
                            : null
            );

            statement.setString(4, task.getStatus().name());
            statement.setLong(5, task.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task", e);
        }
    }


    public void updateSection(
            Long id,
            String section,
            Object value) {

        String sql;

        switch (section) {

            case "date":
                sql = "UPDATE tasks SET date = ? WHERE id = ?";
                break;

            case "description":
                sql = "UPDATE tasks SET description = ? WHERE id = ?";
                break;

            case "status":
                sql = "UPDATE tasks SET status = ? WHERE id = ?";
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid task section: " + section
                );
        }

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (value == null) {
                statement.setString(1, null);
            }
            else if (value instanceof LocalDate) {
                statement.setString(1, value.toString());
            }
            else if (value instanceof LocalDateTime) {
                statement.setString(1, value.toString());
            }
            else {
                statement.setObject(1, value);
            }

            statement.setLong(2, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update task section",
                    e
            );
        }
    }


    public void deleteTask(Long id) {

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }
}
class TodoRowMapper {

    public Task mapRow(ResultSet rs) throws SQLException {

        Task task = new Task();

        task.setId(rs.getLong("id"));
        task.setUserId(rs.getLong("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(Task.Status.valueOf(rs.getString("status")));

        String date = rs.getString("date");// ISO-8601 date
        String creationDate = rs.getString("creation_date");
        if (date != null) task.setDate(LocalDate.parse(date));
        if (creationDate != null) task.setCreationDate(LocalDateTime.parse(creationDate));
        return task;
    }
}
