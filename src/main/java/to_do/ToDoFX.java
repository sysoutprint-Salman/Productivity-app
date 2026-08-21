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
    private record TaskUI(TitledPane card, RadioButton radio, DatePicker datePicker,
            Button dateButton, TextArea descriptionArea) {}

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

    public HBox taskCreationBar() {
        HBox bar = new HBox(8);

        TextField taskField = new TextField();

        if (datePicker == null) {
            datePicker = new DatePicker();
            datePicker.getStyleClass().add("hidden_dateP");
        }

        Button dateButton = new Button();
        ImageView calenderImg = new ImageView(
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/Images/calendar (3).png")
                ))
        );

        StackPane datePane = new StackPane(dateButton, datePicker);

        Button addTaskButton = new Button();
        ImageView plusImg = new ImageView(
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/Images/plus (6).png")
                ))
        );

        taskField.setPromptText("Create a task by typing here!");

        taskField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addTaskButton.fire();
            }
        });

        addTaskButton.setDefaultButton(true);
        addTaskButton.setOnAction(e -> createTask(taskField, datePicker));

        dateButton.setGraphic(calenderImg);
        addTaskButton.setGraphic(plusImg);

        taskField.getStyleClass().add("task_creation_field");
        datePicker.getStyleClass().add("hidden_dateP");
        dateButton.getStyleClass().add("new_task_button");
        addTaskButton.getStyleClass().add("new_task_button");
        bar.getStyleClass().add("task_creation_Hbar");

        HBox.setHgrow(taskField, Priority.ALWAYS);
        taskField.setMaxWidth(Double.MAX_VALUE);

        bar.getChildren().addAll(taskField, datePane, addTaskButton);
        return bar;
    }

    private TaskUI buildTaskUI(Task task) {
        TitledPane taskCard = new TitledPane();

        RadioButton radio = new RadioButton();
        ToggleGroup group = new ToggleGroup();
        radio.setToggleGroup(group);
        radio.setPrefWidth(30);

        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("hidden_dateP");

        Button dateButton = new Button("Due: " + task.getDate().format(dateFormatter));

        Label taskTitle = new Label(task.getTitle());

        TextArea descriptionArea = new TextArea(task.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(50);
        descriptionArea.getStyleClass().add("task_description");
        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
            double initialHeight = 50;

            Text helper = new Text(newText);
            helper.setFont(descriptionArea.getFont());
            helper.setWrappingWidth(descriptionArea.getWidth() - 20);

            double newHeight = helper.getLayoutBounds().getHeight() + 20;

            descriptionArea.setPrefHeight(
                    Math.max(newHeight, initialHeight)
            );
        });
        VBox.setVgrow(descriptionArea, Priority.NEVER);
        StackPane datePane = new StackPane(dateButton, datePicker);

        HBox taskHbox = new HBox(
                10,
                radio,
                taskTitle,
                datePane
        );

        VBox taskContent = new VBox(8);

        taskHbox.setAlignment(Pos.CENTER);
        taskHbox.setFillHeight(true);
        taskHbox.setMaxWidth(Double.MAX_VALUE);
        taskHbox.getStyleClass().add("task_hbox");

        HBox.setHgrow(taskTitle, Priority.ALWAYS);
        taskTitle.setMaxWidth(Double.MAX_VALUE);

        taskContent.getChildren().addAll(new Label("Description:"), descriptionArea);
        taskCard.setMaxWidth(Double.MAX_VALUE);

        taskCard.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            taskHbox.setPrefWidth(newWidth.doubleValue() - 65);
        });
        taskCard.setExpanded(false);
        taskCard.setGraphic(taskHbox);
        taskCard.setContent(taskContent);
        mainTaskVbox.setFillWidth(true);
        taskCard.setMaxWidth(Double.MAX_VALUE);
        taskCard.setUserData(task.getId());
        taskCard.getStyleClass().add("task");

        return new TaskUI(taskCard, radio, datePicker, dateButton, descriptionArea);
    }

    public void todo(Task.Status status) {
        try {
            mainBorderPane.setBottom(null);
            mainTaskVbox.getChildren().clear();

            List<Task> tasks = todoService.getByUserIdAndStatus(
                    User.getUserId(),
                    status
            );

            tasks = sort(tasks, currentSortOption);

            if (status.equals(Task.Status.POSTED)) {
                mainBorderPane.setBottom(taskCreationBar());
            }

            taskLabel.setText(
                    status.equals(Task.Status.POSTED) ? LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d")) :
                            status.equals(Task.Status.COMPLETED) ? "Completed" :
                                    "Deleted"
            );

            sortButton.setVisible(status.equals(Task.Status.POSTED));

            if (tasks.isEmpty()) {
                Label emptyLabel = new Label(
                        status.equals(Task.Status.POSTED)
                                ? "Your todo list looks empty. You can add some tasks by pressing the \"New Task\" button."
                                : status.equals(Task.Status.DELETED)
                                ? "Deleted tasks can be recovered here. After 30 days, they will be permanently deleted."
                                : "This is where completed tasks are, hooray!"
                );

                emptyLabel.setWrapText(true);
                emptyLabel.getStyleClass().add("emptyLabel");
                mainTaskVbox.getChildren().add(emptyLabel);
                return;
            }

            for (Task task : tasks) {
                TaskUI ui = buildTaskUI(task);

                if (status.equals(Task.Status.POSTED)) {
                    ui.descriptionArea.setOnKeyReleased(e ->
                            autoUpdateDescription(ui.descriptionArea, task.getId())
                    );

                    ui.datePicker.setOnAction(e ->
                            editDate(
                                    task.getId(),
                                    ui.datePicker.getValue(),
                                    ui.dateButton
                            )
                    );

                    ui.radio.setOnAction(e -> {
                        if (ui.radio.isSelected()) {
                            task.setStatus(Task.Status.COMPLETED);
                            todoService.updateSection(
                                    task.getId(),
                                    "status",
                                    task
                            );

                            mainTaskVbox.getChildren().remove(ui.card);
                        }
                    });

                    ContextMenu rightClickMenu = new ContextMenu();

                    MenuItem completeItem = new MenuItem("Complete");
                    MenuItem editItem = new MenuItem("Edit");
                    MenuItem deleteItem = new MenuItem("Delete");

                    rightClickMenu.getItems().addAll(
                            completeItem,
                            editItem,
                            deleteItem
                    );

                    completeItem.setOnAction(e -> {
                        task.setStatus(Task.Status.COMPLETED);
                        todoService.updateSection(
                                task.getId(),
                                "status",
                                task
                        );

                        completedTaskTime = LocalDateTime.now();
                        mainTaskVbox.getChildren().remove(ui.card);
                    });

                    editItem.setOnAction(e ->
                            editTask(task)
                    );

                    deleteItem.setOnAction(e -> {
                        task.setStatus(Task.Status.DELETED);
                        todoService.updateSection(
                                task.getId(),
                                "status",
                                task
                        );

                        mainTaskVbox.getChildren().remove(ui.card);
                    });

                    ui.card.setContextMenu(rightClickMenu);
                }

                else if (status.equals(Task.Status.DELETED)) {
                    ui.card.setGraphic(
                            ((HBox) ui.card.getGraphic())
                    );

                    ui.radio.setVisible(false);
                    ui.radio.setManaged(false);

                    ui.datePicker.setDisable(true);
                    ui.descriptionArea.setDisable(true);

                    ui.card.setContextMenu(null);

                    ContextMenu rightClickMenu = new ContextMenu();
                    MenuItem recoverItem = new MenuItem("Recover");

                    rightClickMenu.getItems().add(recoverItem);

                    recoverItem.setOnAction(e -> {
                        recoverItem.setDisable(true);

                        // Keep your existing behavior here.
                        task.setStatus(Task.Status.DELETED);

                        todoService.updateSection(
                                task.getId(),
                                "status",
                                task
                        );

                        mainTaskVbox.getChildren().remove(ui.card);
                    });

                    ui.card.setContextMenu(rightClickMenu);
                }

                else if (status.equals(Task.Status.COMPLETED)) {
                    ui.radio.setVisible(false);
                    ui.radio.setManaged(false);

                    ui.dateButton.setText("Completed");

                    ui.datePicker.setDisable(true);
                    ui.descriptionArea.setDisable(true);
                }

                mainTaskVbox.getChildren().add(ui.card);
            }

            sortTodo();

        } catch (Exception e) {
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
