package JavaFX;

import SpringBoot.Task;
import SpringBoot.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Data;
import org.jetbrains.annotations.UnknownNullability;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Data
public class TaskFX{
    @FXML
    public VBox mainTaskVbox;
    @FXML
    public VBox DTvbox;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    private final HTTPHandler httpHandler = new HTTPHandler();
    public MenuButton sortButton;
    private Task.Status status;
    protected final SwitchScenes handler = new SwitchScenes();
    public Label taskLabel;
    public Button createTaskButton;
    private Timer timer = new Timer();
    private boolean isTaskScheduled = false;
    private final int DELAY = 1000;
    private final ObjectMapper mapper = new ObjectMapper();
    public enum Sort {A_Z, DUE_DATE, NEWEST}
    private Sort currentSortOption;
    private final ToggleGroup sortGroup = new ToggleGroup();
    private LocalDateTime completedTaskTime;
    private AI_AssistantFX ai;
    private NotebookFX notebooks;
    private UserPrefs userPrefs = new UserPrefs();
    private User user = userPrefs.getSavedUser();


    public MenuItem gptMenuItem;
    public MenuItem viewNotebook;
    public RadioMenuItem  A_Z;
    public RadioMenuItem  Due_Date;
    public RadioMenuItem  Newest;

    public TaskFX(){}
    public void createTask() {
        Stage createTaskStage = new Stage();
        createTaskStage.setTitle("Create New Task");

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        DatePicker picker = new DatePicker();
        picker.setPromptText("mm/dd/yyyy");

        picker.setPrefWidth(100);

        HBox timeHbox = new HBox(10, picker); //, comboBoxTimes(), am, pm);
        timeHbox.getStyleClass().add("pickers_hbox");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button createButton = new Button("Create");
        createButton.setOnAction(event -> {
            String title = titleField.getText();
            LocalDate date = picker.getValue();
            String description = descriptionArea.getText();
            if (title != null && picker.getValue() != null && descriptionArea.getText() != null) {
                String taskJson = String.format(
                        "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\", \"status\":\"POSTED\", \"userId\":\"%s\"}",
                        title, date != null ? date.toString() : "", description, user.getUserId()
                );
                httpHandler.POST("tasks", taskJson);
            } else {return;}
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::getByPosted);
            createTaskStage.close();
        });

        VBox createTaskVbox = new VBox(10, titleField, timeHbox, descriptionArea, createButton);
        createTaskVbox.setPadding(new Insets(20));
        createTaskVbox.getStyleClass().add("create_task_vbox");

        Scene createTaskScene = new Scene(createTaskVbox, 350, 300);
        createTaskStage.setScene(createTaskScene);
        createTaskStage.show();
    }

    public void editTask(Long id) {
        Stage editStage = new Stage();
        editStage.setTitle("Edit Task");

        TextField editTitle = new TextField();
        editTitle.setPromptText("Task Title");

        DatePicker editPicker = new DatePicker();
        editPicker.setPromptText("mm/dd/yyyy");
        editPicker.setPrefWidth(100);

        HBox editHBox = new HBox(10, editPicker);

        TextArea editDescriptionArea = new TextArea();
        editDescriptionArea.setPromptText("Description");
        Button editButton = new Button("Edit");

        editButton.setOnAction(event -> {
            String title = editTitle.getText();
            LocalDate date = editPicker.getValue();
            String description = editDescriptionArea.getText();
            String json = String.format(
                    "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\"}",
                    title,
                    date != null ? date.toString() : "",
                    description
            );
            if (title != null && date != null && description != null) {
                httpHandler.UPDATE(json, "tasks/"+id);
            } else {return;}
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::getByPosted);
            editStage.close();
        });

        VBox editTaskFormLayout = new VBox(10, editTitle, editHBox, editDescriptionArea, editButton);
        editTaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(editTaskFormLayout, 350, 300);
        editStage.setScene(formScene);
        editStage.show();
    }
    public void editDate(Long id){
        Stage editStage = new Stage();
        editStage.setTitle("Edit Date");

        DatePicker editPicker = new DatePicker();
        editPicker.setPromptText("mm/dd/yyyy");
        editPicker.setPrefWidth(100);
        editPicker.setEditable(false);

        TextArea editDescriptionArea = new TextArea();
        editDescriptionArea.setPromptText("Description");
        Button editButton = new Button("Edit");

        editButton.setOnAction(event -> {
            LocalDate date = editPicker.getValue();
            String updatedJson = String.format(
                    "{\"date\":\"%s\"}", date != null ? date.toString() : "");
            if (date != null) {httpHandler.UPDATE(updatedJson,"tasks/" + id + "/modular?section=date");}
            else {return;}
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::getByPosted);
            editStage.close();
        });

        VBox editTaskFormLayout = new VBox(10, editPicker, editButton);
        editTaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(editTaskFormLayout, 200, 100);
        editStage.setScene(formScene);
        editStage.show();
    }
    public void autoUpdateDescription(TextArea notepadArea, Long taskId){
            if (isTaskScheduled) {
                timer.cancel();
                timer = new Timer();
            }
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            String updatedText = notepadArea.getText();
                            Map<String, String> updateMap = new HashMap<>();
                            updateMap.put("description", updatedText);
                            String updatedJson = mapper.writeValueAsString(updateMap);
                            httpHandler.UPDATE(updatedJson,"tasks/" + taskId + "/modular?section=description");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    isTaskScheduled = false;
                }
            };
            timer.schedule(task, DELAY);
            isTaskScheduled = true;
    }
    public <T> List<T> sort(List<T> list, Sort sortOption ){
        if (list.isEmpty()) return list;
        if (sortOption == null) return list;

        Object typeIdentity = list.get(0);
        if (typeIdentity instanceof Task){
            List<Task> taskList = (List<Task>) list;
            switch (sortOption){
                case A_Z:
                    taskList.sort(Comparator.comparing(Task::getTitle));
                    break;
                case DUE_DATE:
                    taskList.sort(Comparator.comparing(Task::getDate));
                    break;
                case NEWEST:
                    taskList.sort(Comparator.comparing(Task::getCreationDate).reversed());
                    break;
            }
            return (List<T>) taskList;
        }
        return Collections.emptyList();
    }
    public void sortTodo(){
        A_Z.setToggleGroup(sortGroup);
        Due_Date.setToggleGroup(sortGroup);
        Newest.setToggleGroup(sortGroup);

        A_Z.setOnAction(e -> {
            this.currentSortOption = Sort.A_Z;
            userPrefs.saveSortOption(currentSortOption);
            getByPosted();
        });
        Newest.setOnAction(e ->{
            this.currentSortOption = Sort.NEWEST;
            getByPosted();
        });
        Due_Date.setOnAction(e ->{
            this.currentSortOption = Sort.DUE_DATE;
            getByPosted();
        });
    }
    public TaskFX.Sort sortConvertor( String sortOption){
        switch (sortOption){
            case "A_Z":
                return Sort.A_Z;
            case "NEWEST":
                return Sort.NEWEST;
            case "DUE_DATE":
                return Sort.DUE_DATE;
        }
        return null;
    }
    public void todo(Task.Status status){
        try{
            mainTaskVbox.getChildren().clear();
            List<Task> tasks = httpHandler.GET("tasks/filter?userId=" + user.getUserId()+ "&status=" + status, Task.class);
            tasks = sort(tasks, currentSortOption); //TODO: Use preference

            if (tasks.isEmpty()) {
                Label emptyLabel = new Label(
                        status.equals(Task.Status.POSTED) ? "Your todo list looks empty. You can add some tasks by pressing the \"New Task\" button." :
                                status.equals(Task.Status.DELETED) ? "Deleted tasks can be recovered here. After 30 days, they will be permanently deleted." :
                                        "This is where completed tasks are, hooray!");
                emptyLabel.setWrapText(true);
                emptyLabel.getStyleClass().add("emptyLabel");
                mainTaskVbox.getChildren().add(emptyLabel);
                taskLabel.setText(
                        status.equals(Task.Status.POSTED) ? "TODO" :
                                status.equals(Task.Status.COMPLETED) ? "Completed" :
                                        "Deleted"
                );
                sortButton.setVisible(status.equals(Task.Status.POSTED));
                createTaskButton.setVisible(status.equals(Task.Status.POSTED));
            }
            else {
                tasks.forEach(task -> {
                TitledPane taskCard = new TitledPane();
                RadioButton radio = new RadioButton(); radio.setPrefWidth(30);
                ToggleGroup group = new ToggleGroup();
                radio.setToggleGroup(group);
                Button dateButton  = new Button("Due: " + task.getDate().format(dateFormatter));
                Label taskTitle = new Label(task.getTitle()); taskTitle.setPrefWidth(Region.USE_COMPUTED_SIZE);
                TextArea descriptionArea = new TextArea(task.getDescription());
                HBox taskHbox = new HBox(10);

                taskHbox.getStyleClass().add("task_hbox");
                taskHbox.getChildren().addAll(radio, taskTitle, dateButton);
                taskHbox.setAlignment(Pos.CENTER);
                taskHbox.prefWidthProperty().bind(taskCard.widthProperty().subtract(35));
                HBox.setHgrow(taskTitle, Priority.ALWAYS);
                taskTitle.setMaxWidth(Double.MAX_VALUE);


                taskCard.setExpanded(false);
                taskCard.setGraphic(taskHbox);
                taskCard.getStyleClass().add("task");

                descriptionArea.setPrefHeight(50);
                descriptionArea.setWrapText(true);

                VBox taskContent = new VBox();
                taskContent.setSpacing(8);
                taskCard.setContent(taskContent);
                descriptionArea.maxWidthProperty().bind(taskContent.widthProperty());

                sortTodo();
                if (status.equals(Task.Status.POSTED)){
                    taskLabel.setText("TODO");
                    sortButton.setVisible(true);
                    createTaskButton.setVisible(true);
                    taskContent.getChildren().addAll(new Label("Description:"), descriptionArea);
                    descriptionArea.setOnKeyReleased(e->{
                        autoUpdateDescription(descriptionArea,task.getId());
                    });
                    dateButton.setOnAction(e -> { editDate(task.getId());});
                    radio.setOnAction(f -> {
                        if (radio.isSelected()){
                            String json = String.format("{\"status\":\"%s\"}", Task.Status.COMPLETED);
                            httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                            mainTaskVbox.getChildren().remove(taskCard);
                        }
                    });
                    ContextMenu rightClickMenu = new ContextMenu();
                    MenuItem completeItem = new MenuItem("Complete");
                    MenuItem editItem = new MenuItem("Edit");
                    MenuItem deleteItem = new MenuItem("Delete");
                    rightClickMenu.getItems().addAll(completeItem, editItem, deleteItem);
                    taskCard.setOnContextMenuRequested(e -> {
                        rightClickMenu.show(taskCard, e.getScreenX(), e.getScreenY());
                        completeItem.setOnAction(f -> {
                            Task.Status saveStatus = Task.Status.COMPLETED;
                            String json = String.format("{\"status\":\"%s\"}", saveStatus);
                            httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                            this.completedTaskTime = LocalDateTime.now();
                            mainTaskVbox.getChildren().remove(taskCard);
                        });
                        editItem.setOnAction(f -> {
                            editTask((Long) taskCard.getUserData());
                        });
                        deleteItem.setOnAction(f -> {
                            Task.Status saveStatus = Task.Status.DELETED;
                            String json = String.format("{\"status\":\"%s\"}", saveStatus);
                            httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                            mainTaskVbox.getChildren().remove(taskCard);
                        });
                    });
                }

                else if (status.equals(Task.Status.DELETED)){
                    taskLabel.setText("Deleted");
                    sortButton.setVisible(false);
                    createTaskButton.setVisible(false);
                    taskHbox.getChildren().remove(radio);
                    taskTitle.setText("Deleted: " + task.getTitle());
                    descriptionArea.setDisable(true);
                    taskContent.getChildren().addAll(
                            new Label("Due: " + task.getDate().format(dateFormatter),
                                    descriptionArea));

                    ContextMenu rightClickMenu = new ContextMenu();
                    MenuItem recoverItem = new MenuItem("Recover");
                    rightClickMenu.getItems().add(recoverItem);
                    recoverItem.setOnAction(f -> {
                        Task.Status saveStatus = Task.Status.POSTED;
                        String json = String.format("{\"status\":\"%s\"}", saveStatus);
                        recoverItem.setDisable(true);
                        httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                        mainTaskVbox.getChildren().remove(taskCard);
                    });
                    taskCard.setContextMenu(rightClickMenu);
                }

                else if (status.equals(Task.Status.COMPLETED)){
                    taskLabel.setText("Completed");
                    sortButton.setVisible(false);
                    createTaskButton.setVisible(false);
                    dateButton.setText("Completed");
                    taskHbox.getChildren().remove(radio);
                    descriptionArea.setText(task.getDescription());
                    descriptionArea.setDisable(true);
                    taskContent.getChildren().add(descriptionArea);
                }

                taskCard.setUserData(task.getId());
                mainTaskVbox.getChildren().add(taskCard);
            });
                }
        }catch (Exception e){
            System.err.println("Error occurred trying to load tasks.");
        }
    }
    public void getByPosted(){
        todo(Task.Status.POSTED);
    }
    public void getByDeleted(){
        todo(Task.Status.DELETED);
    }
    public void getByCompleted(){
        todo(Task.Status.COMPLETED);
    }
    public void switchToGPT(ActionEvent event) {
        handler.switchScene(event, "AI", consumer->{
            ai = (AI_AssistantFX) consumer;
            ai.GETChatlogs();
        });
    }
    public void switchToNotebook(ActionEvent event) {
        handler.switchScene(event, "notebook", consumer->{
            notebooks = (NotebookFX) consumer;
            notebooks.GETNotebooks();
        });
    }
}
