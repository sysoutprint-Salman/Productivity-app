package to_do;

import JavaFX.*;
import user.User;
import ai_chat.AI_AssistantFX;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kanban.KanbanFX;
import lombok.Data;
import notebook.NotebookFX;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Data
public class ToDoFX {
    @FXML
    public VBox mainTaskVbox;
    @FXML
    public VBox DTvbox;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    public MenuButton sortButton;
    private Task.Status status;
    protected final SwitchScenes handler = new SwitchScenes();
    public Label taskLabel;
    public Button createTaskButton;
    private Timer timer = new Timer();
    private boolean isTaskScheduled = false;
    private final int DELAY = 700;
    private final ObjectMapper mapper = new ObjectMapper();
    public enum Sort {A_Z, DUE_DATE, NEWEST}
    private Sort currentSortOption;
    private final ToggleGroup sortGroup = new ToggleGroup();
    private LocalDateTime completedTaskTime;
    private AI_AssistantFX ai;
    private NotebookFX notebooks;
    private UserPrefs userPrefs = new UserPrefs();
    private User user = userPrefs.getSavedUser();
    private final TodoService todoService = new TodoService();

    public MenuItem gptMenuItem;
    public MenuItem viewNotebook;
    public RadioMenuItem  A_Z;
    public RadioMenuItem  Due_Date;
    public RadioMenuItem  Newest;
    public DatePicker dateButton;
    public ScrollPane mainScrollpane;

    public HBox taskCreationHbox;
    public TextField taskCreationTF;
    public StackPane stackPane;
    public BorderPane mainBorderPane;
    public DatePicker datePicker;

    public ToDoFX(){}

    public void initialize(){
        //this.tasks = AppState.getTasks();

    }

    public void createTask(TextField text, DatePicker picker) {
            String title = text.getText();
            LocalDate date = picker.getValue();

            try {
                if (!title.isEmpty() && date != null) {
                    Task newTask = new Task();
                    newTask.setUserId(User.getUserId());
                    newTask.setDescription("");
                    newTask.setStatus(Task.Status.POSTED);
                    newTask.setTitle(title);
                    newTask.setDate(date);

                    todoService.createTask(newTask);
                    text.clear();
                    this.datePicker.setValue(date);
                }
                else {
                    //Red highlighting for text field and date icon eventually goes here
                    return;
                }

            } catch (NullPointerException e) {
                System.out.println("Error occurred attempting to create a task.");
                e.printStackTrace();
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::getByPosted);
    }

    public void editTask(Task prevInfo) {
        Stage editStage = new Stage();
        TextField editTitle = new TextField();
        DatePicker editPicker = new DatePicker();
        HBox editHBox = new HBox(10, editPicker);
        TextArea editDescriptionArea = new TextArea();
        Button editButton = new Button("Edit");

        editStage.setTitle("Edit Task");
        editTitle.setPromptText("Task Title");
        editPicker.setPromptText("mm/dd/yyyy");
        editPicker.setPrefWidth(120);
        editPicker.setEditable(false);
        editButton.setPrefSize(320,25);
        editDescriptionArea.setPromptText("Description");
        editTitle.setText(prevInfo.getTitle());
        editPicker.setValue(prevInfo.getDate());
        editDescriptionArea.setText(prevInfo.getDescription());

        editTitle.getStyleClass().add("title_box");
        editPicker.getStyleClass().add("date_picker");
        editButton.getStyleClass().add("submit_button");
        editDescriptionArea.getStyleClass().add("description_box");

        editTitle.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                editButton.fire();
            }
        });
        editButton.setDefaultButton(true);


        editButton.setOnAction(event -> {
            try {
                String title = editTitle.getText();
                LocalDate date = editPicker.getValue();
                String description = editDescriptionArea.getText();

                if (!title.isEmpty() && date != null) {
                    prevInfo.setTitle(title);
                    prevInfo.setDescription(description);
                    prevInfo.setDate(date);
                    todoService.updateTask(prevInfo.getId(),prevInfo);
                } else {
                    //Visual highlighting of text field and date picker goes here
                    return;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::getByPosted);
            editStage.close();
        });

        VBox editTaskFormLayout = new VBox(10, editTitle, editHBox, editDescriptionArea, editButton);
        editTaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(editTaskFormLayout, 350, 300);
        editStage.setScene(formScene);
        editStage.setResizable(false);
        editStage.initModality(Modality.APPLICATION_MODAL);
        formScene.getStylesheets().add("CSS/Tasks.css");
        editStage.show();
    }

    public void editDate(Long id, LocalDate selectedEditDate, Button container){
            if (selectedEditDate != null) {
                Task task = new Task();
                task.setDate(selectedEditDate);
                todoService.updateSection(id,"date",task);
                container.setText("Due: " + selectedEditDate.format(dateFormatter));
            }
            else return;
            //mainTaskVbox.getChildren().clear();
            //Platform.runLater(this::getByPosted);
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
                            Task task = new Task();
                            task.setDescription(updatedText);
                            todoService.updateSection(taskId, "description",task);
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
                    taskList.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
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

    public HBox taskCreationBar(){
        HBox bar = new HBox(5);
        TextField taskField = new TextField();
        if (datePicker == null) {
            datePicker = new DatePicker();
            datePicker.getStyleClass().add("hidden_dateP");
        }
        Button dateButton = new Button();
        ImageView calenderImg = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/calendar.png"))));
        StackPane datePane = new StackPane(dateButton, datePicker);
        Button addTaskButton = new Button();
        ImageView plusImg = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/plusButton.png"))));

        taskField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addTaskButton.fire();
            }
        });

        addTaskButton.setDefaultButton(true);
        addTaskButton.setOnAction(e -> {
            createTask(taskField,datePicker);
        });
        taskField.setPromptText("Create a task by typing here!");
        dateButton.setGraphic(calenderImg);
        addTaskButton.setGraphic(plusImg);
        taskField.getStyleClass().add("task_creation_field");
        datePicker.getStyleClass().add("hidden_dateP");
        addTaskButton.getStyleClass().add("new_task_button");
        dateButton.getStyleClass().add("new_task_button");
        bar.getStyleClass().add("task_creation_Hbar");
        bar.getChildren().addAll(taskField, datePane, addTaskButton);
        return bar;
    }

    public void todo(Task.Status status){
        try{
            mainBorderPane.setBottom(null);
            mainTaskVbox.getChildren().clear();
            List<Task> tasks =
                    todoService.getByUserIdAndStatus(
                            User.getUserId(),
                            status
                    );
            tasks = sort(tasks, currentSortOption);
            if(status.equals(Task.Status.POSTED)){
                mainBorderPane.setBottom(taskCreationBar());
            }
            if (tasks.isEmpty()) {
                Label emptyLabel = new Label(
                        status.equals(Task.Status.POSTED) ? "Your todo list looks empty. You can add some tasks by pressing the \"New Task\" button." :
                                status.equals(Task.Status.DELETED) ? "Deleted tasks can be recovered here. After 30 days, they will be permanently deleted." :
                                        "This is where completed tasks are, hooray!");
                emptyLabel.setWrapText(true);
                emptyLabel.getStyleClass().add("emptyLabel");
                mainTaskVbox.getChildren().add(emptyLabel);
                taskLabel.setText(
                        status.equals(Task.Status.POSTED) ? "To Do" :
                                status.equals(Task.Status.COMPLETED) ? "Completed" :
                                        "Deleted"
                );
                sortButton.setVisible(status.equals(Task.Status.POSTED));
            }
            else {
                tasks.forEach(task -> {
                TitledPane taskCard = new TitledPane();
                RadioButton radio = new RadioButton(); radio.setPrefWidth(30);
                ToggleGroup group = new ToggleGroup();
                DatePicker datePicker = new DatePicker();
                Button dateButton  = new Button("Due: " + task.getDate().format(dateFormatter));
                Label taskTitle = new Label(task.getTitle()); taskTitle.setPrefWidth(Region.USE_COMPUTED_SIZE);
                TextArea descriptionArea = new TextArea(task.getDescription());
                VBox taskContent = new VBox();
                StackPane stackPane = new StackPane(dateButton, datePicker);
                HBox taskHbox = new HBox(10, radio, taskTitle, stackPane);

                datePicker.getStyleClass().add("hidden_dateP");
                radio.setToggleGroup(group);
                taskHbox.getStyleClass().add("task_hbox");
                taskHbox.setAlignment(Pos.CENTER);
                taskHbox.prefWidthProperty().bind(taskCard.widthProperty().subtract(35));
                HBox.setHgrow(taskTitle, Priority.ALWAYS);
                taskTitle.setMaxWidth(Double.MAX_VALUE);

                taskCard.setExpanded(false);
                taskCard.setGraphic(taskHbox);
                taskCard.getStyleClass().add("task");

                descriptionArea.getStyleClass().add("task_description");
                descriptionArea.setWrapText(true);
                descriptionArea.maxWidthProperty().bind(taskContent.widthProperty());
                descriptionArea.setPrefHeight(50);
                descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
                    double initialHeight = 50;
                    Text helper = new Text(newText);
                    helper.setFont(descriptionArea.getFont());
                    helper.setWrappingWidth(descriptionArea.getWidth() - 20);
                    double newHeight = helper.getLayoutBounds().getHeight() + 20;
                    // Prevent shrinking below initial height
                    descriptionArea.setPrefHeight(Math.max(newHeight, initialHeight));
                });

                taskContent.setSpacing(8);
                taskCard.setContent(taskContent);

                sortTodo();
                if (status.equals(Task.Status.POSTED)){
                    taskLabel.setText("To Do");
                    sortButton.setVisible(true);
                    taskContent.getChildren().addAll(new Label("Description:"), descriptionArea);
                    descriptionArea.setOnKeyReleased(e->{
                        autoUpdateDescription(descriptionArea,task.getId());
                    });
                    datePicker.setOnAction(e -> {
                        editDate(task.getId(), datePicker.getValue(), dateButton);
                    });
                    radio.setOnAction(f -> {
                        if (radio.isSelected()){
                            task.setStatus(Task.Status.COMPLETED);
                            todoService.updateSection(task.getId(), "status", task);
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
                            task.setStatus(Task.Status.COMPLETED);
                            todoService.updateSection(task.getId(), "status", task);
                            this.completedTaskTime = LocalDateTime.now();
                            mainTaskVbox.getChildren().remove(taskCard);
                        });
                        editItem.setOnAction(f -> {
                            editTask(task);
                        });
                        deleteItem.setOnAction(f -> {
                            task.setStatus(Task.Status.DELETED);
                            todoService.updateSection(task.getId(), "status", task);
                            mainTaskVbox.getChildren().remove(taskCard);
                        });
                    });
                }

                else if (status.equals(Task.Status.DELETED)){
                    taskLabel.setText("Deleted");
                    sortButton.setVisible(false);
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
                        recoverItem.setDisable(true);
                        task.setStatus(Task.Status.DELETED);
                        todoService.updateSection(task.getId(), "status", task);
                        mainTaskVbox.getChildren().remove(taskCard);
                    });
                    taskCard.setContextMenu(rightClickMenu);
                }

                else if (status.equals(Task.Status.COMPLETED)){
                    taskLabel.setText("Completed");
                    sortButton.setVisible(false);
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
            System.err.println("JavaFX: Error occurred trying to load tasks.");
            e.printStackTrace();
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
    public void switchToKanban(ActionEvent event) {
        handler.switchScene(event, "kanbanBoard", consumer->{
            KanbanFX kanban = (KanbanFX) consumer;
        });
    }
}
