package kanban.reminder;


import database.DatabaseManager;
import kanban.Enums;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReminderRepo {

    private final ReminderMapper reminderMapper =
            new ReminderMapper();

    public List<Reminder> findByBoardId(Long boardId) {

        String sql = """
                SELECT * FROM reminders
                WHERE board_id = ?
                """;

        List<Reminder> reminders = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, boardId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reminders.add(
                            reminderMapper.mapRow(rs)
                    );
                }
            }

            return reminders;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve reminders",
                    e
            );
        }
    }

    public void create(Reminder reminder) {

        String sql = """
                INSERT INTO reminders
                    (board_id, reminder_title,
                     description, priority, due_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, reminder.getBoardId());
            stmt.setString(
                    2,
                    reminder.getReminderTitle()
            );
            stmt.setString(
                    3,
                    reminder.getDescription()
            );
            stmt.setString(
                    4,
                    reminder.getPriority().name()
            );

            if (reminder.getDueDate() == null) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(
                        5,
                        reminder.getDueDate().format(
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                );
            }

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    reminder.setReminderId(
                            keys.getLong(1)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to create reminder",
                    e
            );
        }
    }

    public void delete(Long reminderId) {

        String sql =
                "DELETE FROM reminders WHERE reminder_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, reminderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete reminder",
                    e
            );
        }
    }

    public void updateReminderSection(Long reminderId, Object value, Enums.Section section) {
        String sql;

        switch (section) {
            case TITLE:
                sql = "UPDATE reminders SET reminder_title = ? WHERE reminder_id = ?";
                break;
            case DESCRIPTION:
                sql = "UPDATE reminders SET description = ? WHERE reminder_id = ?";
                break;
            case PRIORITY:
                sql = "UPDATE reminders SET priority = ? WHERE reminder_id = ?";
                break;
            case DUE_DATE:
                sql = "UPDATE reminders SET due_date = ? WHERE reminder_id = ?";
                break;
            default:
                return;
        }

        if (value == null) return;

        if (section == Enums.Section.DUE_DATE && value instanceof LocalDateTime dateTime)
            value = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        else if (section == Enums.Section.PRIORITY && value instanceof Enum<?> priority)
            value = priority.name();

        updateValue(sql, value, reminderId);
    }

    public void updateReminderSection(Long reminderId, Enums.Section section, Reminder reminder) {
        String sql;
        Object value;

        switch (section) {
            case TITLE:
                value = reminder.getReminderTitle();
                sql = "UPDATE reminders SET reminder_title = ? WHERE reminder_id = ?";
                break;
            case DESCRIPTION:
                value = reminder.getDescription();
                sql = "UPDATE reminders SET description = ? WHERE reminder_id = ?";
                break;
            case PRIORITY:
                value = reminder.getPriority() == null ? null : reminder.getPriority().name();
                sql = "UPDATE reminders SET priority = ? WHERE reminder_id = ?";
                break;
            case DUE_DATE:
                value = reminder.getDueDate() == null
                        ? null
                        : reminder.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                sql = "UPDATE reminders SET due_date = ? WHERE reminder_id = ?";
                break;
            default:
                return;
        }

        if (value == null) return;
        updateValue(sql, value, reminderId);
    }

    public void updateReminderSections(Long reminderId, Map<Enums.Section, Object> updates) {
        if (updates == null || updates.isEmpty()) return;

        StringBuilder sql = new StringBuilder("UPDATE reminders SET ");
        List<Object> values = new ArrayList<>();

        for (Map.Entry<Enums.Section, Object> entry : updates.entrySet()) {
            String column = switch (entry.getKey()) {
                case TITLE -> "reminder_title";
                case DESCRIPTION -> "description";
                case PRIORITY -> "priority";
                case DUE_DATE -> "due_date";
                default -> null;
            };

            if (column == null) continue;

            Object value = entry.getValue();

            if (value instanceof Enum<?> enumValue)
                value = enumValue.name();
            else if (value instanceof LocalDateTime dateTime)
                value = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            if (!values.isEmpty()) sql.append(", ");
            sql.append(column).append(" = ?");
            values.add(value);
        }

        if (values.isEmpty()) return;

        sql.append(" WHERE reminder_id = ?");

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                if (value == null) stmt.setNull(i + 1, Types.NULL);
                else stmt.setObject(i + 1, value);
            }

            stmt.setLong(values.size() + 1, reminderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reminder sections", e);
        }
    }

    private void updateValue(String sql, Object value, Long reminderId) {
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, value);
            stmt.setLong(2, reminderId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update reminder",
                    e
            );
        }
    }

    public void updateFull(Long reminderId, Reminder reminder) {
        String sql = """
                UPDATE reminders
                SET reminder_title = ?,
                    description = ?,
                    priority = ?,
                    due_date = ?
                WHERE reminder_id = ?
                """;
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reminder.getReminderTitle());
            stmt.setString(2, reminder.getDescription());
            stmt.setString(3, reminder.getPriority().name());

            if (reminder.getDueDate() == null) {
                stmt.setNull(4, Types.VARCHAR);
            } else {
                stmt.setString(
                        4,
                        reminder.getDueDate().format(
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                );
            }

            stmt.setLong(5, reminderId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update reminder",
                    e
            );
        }
    }
}
class ReminderMapper {

    public Reminder mapRow(ResultSet rs)
            throws SQLException {

        Reminder reminder = new Reminder();

        reminder.setReminderId(
                rs.getLong("reminder_id")
        );

        reminder.setBoardId(
                rs.getLong("board_id")
        );

        reminder.setReminderTitle(
                rs.getString("reminder_title")
        );

        reminder.setDescription(
                rs.getString("description")
        );

        reminder.setPriority(
                Reminder.Priority.valueOf(
                        rs.getString("priority")
                )
        );

        String dueDate = rs.getString("due_date");

        if (dueDate != null) {
            reminder.setDueDate(
                    LocalDateTime.parse(
                            dueDate,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
            );
        }

        return reminder;
    }
}
