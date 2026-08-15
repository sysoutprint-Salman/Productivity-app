package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import to_do.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;

    @GetMapping("/all/{userId}")
    protected List<Task> getAllTasksByUserId(@PathVariable Long userId) {
        return taskRepository.findAllByUserId(userId);
    }

    @GetMapping("/{id}")
    protected Task getById(@PathVariable Long id) {
        return taskRepository.getById(id);
    }

    @GetMapping("/filter")
    protected List<Task> getByUserIdAndStatus(@RequestParam Long userId, @RequestParam Task.Status status) {
        return taskRepository.getByUserIdAndStatus(userId, status);
    }

    @PostMapping
    protected void createTask(@RequestBody Task task) {
        task.setCreationDate(LocalDateTime.now());
        taskRepository.createTask(task);
    }

    @PutMapping("/{id}")
    protected ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task receivedInfo) {

        Task task = taskRepository.getById(id);

        task.setTitle(receivedInfo.getTitle());
        task.setDescription(receivedInfo.getDescription());
        task.setDate(receivedInfo.getDate());

        taskRepository.updateTask(task);

        return ResponseEntity.ok("SpringBoot: Task successfully updated.");
    }

    @PutMapping("/{id}/modular")
    protected ResponseEntity<?> updateSection(@PathVariable Long id, @RequestParam String section, @RequestBody Task receivedInfo) {
        Object value;

        switch (section) {
            case "date":
                value = receivedInfo.getDate();
                break;

            case "description":
                value = receivedInfo.getDescription();
                break;

            case "status":
                value = receivedInfo.getStatus().name();
                break;

            default:
                return ResponseEntity.badRequest().body("Invalid part: " + section);
        }

        taskRepository.updateSection(id, section, value);

        return ResponseEntity.ok("SpringBoot: The section of the task was successfully updated.");
    }
}

@Repository
 class TaskRepository {

    @Autowired
    private JdbcTemplate jdbc;

    private final TaskRowMapper rowMapper = new TaskRowMapper();

    protected List<Task> findAllByUserId(Long userId) {
        return jdbc.query("SELECT * FROM tasks WHERE user_id = ?", rowMapper, userId);
    }

    protected Task getById(Long id) {
        return jdbc.queryForObject(
                "SELECT * FROM tasks WHERE id = ?",
                rowMapper,
                id
        );
    }

    protected List<Task> getByUserIdAndStatus(Long userId, Task.Status status) {
        return jdbc.query(
                "SELECT * FROM tasks WHERE user_id = ? AND status = ? ORDER BY id",
                rowMapper,
                userId,
                status.name()
        );
    }

    protected void createTask(Task task) {
        jdbc.update(
                "INSERT INTO tasks (user_id, title, description, date, status, creation_date) VALUES (?, ?, ?, ?, ?, ?)",
                task.getUserId(),
                task.getTitle(),
                task.getDescription(),
                task.getDate(),
                task.getStatus().name(),
                task.getCreationDate()
        );
    }

    protected void updateTask(Task task) {
        jdbc.update(
                "UPDATE tasks SET title = ?, description = ?, date = ?, status = ? WHERE id = ?",
                task.getTitle(),
                task.getDescription(),
                task.getDate(),
                task.getStatus().name(),
                task.getId()
        );
    }

    protected void updateSection(Long id, String section, Object value) {

        switch (section) {
            case "date":
                jdbc.update(
                        "UPDATE tasks SET date = ? WHERE id = ?",
                        value,
                        id
                );
                return;

            case "description":
                jdbc.update(
                        "UPDATE tasks SET description = ? WHERE id = ?",
                        value,
                        id
                );
                return;

            case "status":
                jdbc.update(
                        "UPDATE tasks SET status = ? WHERE id = ?",
                        value,
                        id
                );
                return;

            default:
        }
    }

    protected void deleteTask(Long id) {
        jdbc.update("DELETE FROM tasks WHERE id = ?", id);
    }
}
class TaskRowMapper implements RowMapper<Task> {
    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {

        Task task = new Task();

        task.setId(rs.getLong("id"));
        task.setUserId(rs.getLong("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(Task.Status.valueOf(rs.getString("status")));
        task.setDate(rs.getObject("date", LocalDate.class));
        task.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
        return task;
    }
}

