package to_do;


import java.time.LocalDateTime;
import java.util.List;

public class TodoService {

    private final TodoRepo taskRepository;

    public TodoService() {
        this.taskRepository = new TodoRepo();
    }

    public List<Task> getAllTasksByUserId(Long userId) {
        return taskRepository.findAllByUserId(userId);
    }

    public Task getById(Long id) {
        return taskRepository.getById(id);
    }

    public List<Task> getByUserIdAndStatus(Long userId, Task.Status status) {
        return taskRepository.getByUserIdAndStatus(userId, status);
    }

    public void createTask(Task task) {
        task.setCreationDate(LocalDateTime.now());

        taskRepository.createTask(task);
    }

    public void updateTask(Long id, Task receivedInfo) {

        Task task = taskRepository.getById(id);

        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        task.setTitle(receivedInfo.getTitle());
        task.setDescription(receivedInfo.getDescription());
        task.setDate(receivedInfo.getDate());

        taskRepository.updateTask(task);
    }

    public void updateSection(Long id, String section, Task receivedInfo) {

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

            default: throw new IllegalArgumentException(
                        "Invalid task section: " + section
                );
        }

        taskRepository.updateSection(id, section, value);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteTask(id);
    }
}
