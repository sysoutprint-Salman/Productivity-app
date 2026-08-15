package to_do;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.testng.Assert.*;

public class TodoTest {
    private final TodoService todoService = new TodoService();

    @Test
    public void createTask() {
        Task task = new Task();
        task.setUserId(1L);
        task.setTitle("Test Task with SQLite!");
        task.setDescription("Test Description");
        task.setDate(LocalDate.of(2026, 8, 20));
        task.setStatus(Task.Status.POSTED);

        todoService.createTask(task);

        assertNotNull(task.getCreationDate());
    }
    @Test
    public void getById() {
        Task task = todoService.getById(183L);
        System.out.println(task);
        assertNotNull(task);
        assertEquals(Long.valueOf(183L), task.getId());
    }
    @Test
    public void getByUserIdAndStatus() {
        List<Task> tasks =
                todoService.getByUserIdAndStatus(
                        8L,
                        Task.Status.POSTED
                );
        System.out.println(tasks);
        assertNotNull(tasks);

        for (Task task : tasks) {
            assertEquals(Long.valueOf(8L), task.getUserId());
            assertEquals(task.getStatus(), Task.Status.POSTED);
        }
    }
    @Test
    public void updateTask() {
        Task task = todoService.getById(170L);
        System.out.println("Fetched: " + task);
        assertNotNull(task);

        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        task.setDate(LocalDate.now());
        task.setStatus(Task.Status.COMPLETED);
        task.setCreationDate(LocalDateTime.now());

        todoService.updateTask(1L, task);

        Task updatedTask = todoService.getById(1L);
        System.out.println("Updated: " + task);
        assertEquals(updatedTask.getTitle(), "Updated Title");
        assertEquals(updatedTask.getDescription(), "Updated Description");
    }
    @Test
    public void updateSection() {
        Task task = todoService.getById(170L);
        System.out.println("Fetched: " + task);
        assertNotNull(task);

        task.setStatus(Task.Status.DELETED);

        todoService.updateSection(170L, "status", task);

        Task updatedTask = todoService.getById(170L);
        System.out.println("Updated: " + task);
        assertEquals(
                updatedTask.getDescription(),
                "Updated through SQLite!"
        );
    }
    @Test
    public void deleteTask() {
        Task task = todoService.getById(1L);

        assertNotNull(task);

        todoService.deleteTask(1L);

        Task deletedTask = todoService.getById(1L);

        assertNull(deletedTask);
    }
}
