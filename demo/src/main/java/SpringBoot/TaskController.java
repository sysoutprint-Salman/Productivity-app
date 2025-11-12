package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor //Constructor injection via Lombok
public class TaskController {
    private final TaskService taskService;
    private final TaskRepository taskRepository;

    @GetMapping("/{id}") //GET request
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    @GetMapping("/{status}")
    public List<Task> findByStatus(@PathVariable Task.Status status){
        return taskService.findByStatus(status);
    }
    @GetMapping("/filter")
    public List<Task> findByIdAndStatus(@RequestParam Long userId, @RequestParam Task.Status status){
        return taskService.findByIdAndStatus(userId, status);
    }
    @PostMapping //POST request
    public Task createTask(@RequestBody Task task) {
        task.setCreationDate(LocalDateTime.now());
        return taskService.createTask(task);
    }

    @PutMapping("/{id}") //The main UPDATE METHOD
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task recievedInfo){
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()){
            Task existingTask = task.get();
            existingTask.setTitle(recievedInfo.getTitle());
            existingTask.setDate(recievedInfo.getDate());
            existingTask.setDescription(recievedInfo.getDescription());
            taskRepository.save(existingTask);
            return ResponseEntity.ok("SpringBoot: Task successfully updated.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}/modular")
    public ResponseEntity<?> updateSection(@PathVariable Long id, @RequestParam String section, @RequestBody Task recievedInfo){
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task existingTask = task.get();
            switch (section) {
                case "date":
                    if (recievedInfo.getDate() != null) {
                    existingTask.setDate(recievedInfo.getDate());
                    break;}
                    else {return ResponseEntity.badRequest().build();}
                case "description":
                    if (recievedInfo.getDescription() != null) {
                    existingTask.setDescription(recievedInfo.getDescription());
                    break;}
                    else {return ResponseEntity.badRequest().build();}
                case "status":
                    if (recievedInfo.getStatus() != null) {
                    existingTask.setStatus(recievedInfo.getStatus());
                    break;}
                    else {return ResponseEntity.badRequest().build();}
                default:
                    return ResponseEntity.badRequest().body("Invalid part: " + section);
            }
            taskRepository.save(existingTask);
            return ResponseEntity.ok("SpringBoot: The section of the task was successfully updated.");
        }
        else {return ResponseEntity.notFound().build();}
    }

    //For now deleting tasks isn't important.
    /*@DeleteMapping("/{id}") //DELETE request
    public ResponseEntity<?> deleteTask(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean archive) {
        try{
            if (archive){
            Task task = taskService.getTaskById(id);
            DeletedTask deletedTask = new DeletedTask();
            deletedTask.setTitle(task.getTitle());
            deletedTask.setDescription(task.getDescription());
            deletedTask.setDate(task.getDate());
            deletedTask.setDeletedDate(LocalDateTime.now());
            deletedTaskService.insertDeletedTask(deletedTask);
            taskService.deleteTask(id);
            return ResponseEntity.ok("SpringBoot: Task sent to deleted tasks successfully.");}
            else{taskService.deleteTask(id);
                return ResponseEntity.ok("SpringBoot: Task deleted successfully.");}
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SpringBoot: Task couldn't be deleted with id: " + id);
        }
    }*/
}
@Service
@RequiredArgsConstructor //constructor (Lombok)
class TaskService {
    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public List<Task> findByStatus(Task.Status status) {
        return taskRepository.findByStatus(status);
    }
    public Task getTaskById(Long id){
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("SpringBoot: Task not found with id: " + id));
    }
    public List<Task> findByIdAndStatus(Long userId, Task.Status status){
        return taskRepository.findByUserIdAndStatus(userId, status);

    }
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
@Repository //Repo works as a data access layer which interacts with the database
interface TaskRepository extends JpaRepository<Task, Long> { //Repo uses Task type & Long for ID

    List<Task> findByStatus(Task.Status status);
    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);
}