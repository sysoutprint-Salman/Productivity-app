package kanban;


import JavaFX.SwitchScenes;
import ai_chat.AI_AssistantFX;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.*;
import kanban.boards.BoardService;
import kanban.card.Card;
import kanban.card.CardService;
import kanban.column.Column;
import kanban.column.ColumnService;
import kanban.reminder.Reminder;
import kanban.reminder.ReminderService;
import notebook.NotebookFX;
import to_do.ToDoFX;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KanbanFX {
    @FXML
    private HBox boardHBox;


    @FXML private StackPane card;
    @FXML private TextArea cardTextA;
    @FXML private MenuButton cardOptions;
    @FXML private VBox menu;
    @FXML private VBox cards;
    @FXML private VBox archive;
    @FXML private VBox boards;
    @FXML private VBox deleted;
    @FXML private VBox reminders;
    //@FXML private VBox themes;

    @FXML private Label menuLabel;
    @FXML private Label cardsLabel;
    @FXML private Label filterLabel;
    @FXML private Label archiveLabel;
    @FXML private Label boardsLabel;
    @FXML private VBox sidebar;
    @FXML private VBox sidebarExpansionVbox;
    @FXML private Button addListButton;
    @FXML private ScrollPane boardScrollPane;
    @FXML private HBox boardHeadherHbox;
    @FXML private Label boardTitle;


    private Stage stage;
    private Scene scene;
    private VBox layoutContainer;

    private enum Sidebar{ CARDS, ARCHIVE, BOARDS, DELETED, REMINDERS, THEMES }
    public enum Mode { ADD, LOAD }
    private final VBox sidebarContentVbox = new VBox();
    private final HBox sidebarHeader = new HBox();
    private final ScrollPane sidebarScrollPane = new ScrollPane();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");
    private java.util.List<Column> columnData;
    private java.util.List<Card> cardData;
    private Long boardID = 1l;
    private String json;

    private double dragOffsetX;
    private boolean sidebarOpen = false;
    private VBox previousSidebarButton;
    private VBox draggedList = null;
    private Pane ghostList = null;
    private Rectangle placeholder = null;
    private double dragOffsetY;
    private SwitchScenes handler = new SwitchScenes();
    private static final double DRAG_VISUAL_OFFSET = 5;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ColumnService columnService = new ColumnService();
    private final CardService cardService = new CardService();
    private final BoardService boardService = new BoardService();
    private final ReminderService reminderService = new ReminderService();

    public KanbanFX(){}

    @FXML
    private void initialize() {
        addOrLoadColumns(Mode.LOAD);
        cards.setOnMousePressed(this::slidingSidebar);
        reminders.setOnMousePressed(this::slidingSidebar);
        //themes.setOnMousePressed(this::slidingSidebar);
        archive.setOnMousePressed(this::slidingSidebar);
        boards.setOnMousePressed(this::slidingSidebar);
        deleted.setOnMousePressed(this::slidingSidebar);

        sidebarContentVbox.getStyleClass().add("sidebar_content");
        sidebarContentVbox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        sidebarContentVbox.setPrefWidth(Region.USE_COMPUTED_SIZE);

        sidebarHeader.setPrefHeight(Region.USE_COMPUTED_SIZE);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        sidebarHeader.getStyleClass().add("sidebar_header");

        sidebarScrollPane.setFitToWidth(true);
        sidebarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane.getStyleClass().add("sidebar_scroll");
        sidebarScrollPane.setContent(sidebarContentVbox);
        sidebarScrollPane.setFocusTraversable(false);
        sidebarExpansionVbox.setFocusTraversable(false);
        boardHBox.setFocusTraversable(true);
        boardScrollPane.setFocusTraversable(false);
        boardScrollPane.getStyleClass().add("board_scrollpane");

        boardTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(boardTitle,Priority.ALWAYS);


        VBox.setVgrow(sidebarScrollPane,Priority.ALWAYS);
        VBox.setVgrow(sidebarContentVbox, Priority.ALWAYS);
    }

    private void remindersPopup(Boolean isEdit, Reminder reminderToEdit) {
        stage = new Stage();
        stage.setTitle(isEdit ? "Edit reminder" : "Create reminder");

        layoutContainer = new VBox();
        layoutContainer.getStyleClass().add("pu_vbox");

        TextField titleField = new TextField();
        titleField.setPromptText("Reminder title");
        titleField.getStyleClass().add("pu_text_fields");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setWrapText(true);
        descriptionArea.getStyleClass().add("pu_text_area");

        HBox timeDateRow = new HBox();
        timeDateRow.getStyleClass().add("pu_hbox");

        Label timeLabel = new Label("Time:");

        ComboBox<String> timeCombo = new ComboBox<>();
        times(timeCombo);
        timeCombo.getStyleClass().add("pu_choicebox");
        timeCombo.setPrefWidth(95);

        Label dateLabel = new Label("Date:");

        DatePicker datePicker = new DatePicker();
        datePicker.setEditable(false);
        datePicker.setPromptText("Ex: 1/1/2026");
        datePicker.getStyleClass().add("date_picker");
        datePicker.setPrefWidth(125);

        timeDateRow.getChildren().addAll(
                timeLabel,
                timeCombo,
                dateLabel,
                datePicker
        );

        HBox priorityColorRow = new HBox();
        priorityColorRow.getStyleClass().add("pu_hbox");

        Label priorityLabel = new Label("Priority:");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.setVisibleRowCount(4);
        priorityCombo.getStyleClass().add("pu_choicebox");
        priorityCombo.getItems().addAll("None", "Low", "Medium", "High");

        priorityColorRow.getChildren().addAll(priorityLabel, priorityCombo);

        if (isEdit && reminderToEdit != null) {
            titleField.setText(reminderToEdit.getReminderTitle());
            descriptionArea.setText(reminderToEdit.getDescription());

            if (reminderToEdit.getPriority() != null) {
                String priorityText = reminderToEdit.getPriority().name().substring(0, 1)
                        + reminderToEdit.getPriority().name().substring(1).toLowerCase();

                priorityCombo.setValue(priorityText);
            }

            if (reminderToEdit.getDueDate() != null) {
                datePicker.setValue(reminderToEdit.getDueDate().toLocalDate());
                timeCombo.setValue(reminderToEdit.getDueDate().toLocalTime().format(formatter));
            }
        }

        Button saveButton = new Button(isEdit ? "Save" : "Create");
        saveButton.getStyleClass().add("pu_buttons");

        saveButton.setOnAction(e -> {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            LocalDate date = datePicker.getValue();

            if (title == null || title.isBlank()) return;
            if (date == null) return;
            if (timeCombo.getValue() == null) return;
            if (priorityCombo.getValue() == null) return;

            LocalTime time = LocalTime.parse(timeCombo.getValue(), formatter);

            Reminder.Priority priority = Reminder.Priority.valueOf(
                    priorityCombo.getValue().toUpperCase()
            );

            Reminder reminder = new Reminder(
                    isEdit && reminderToEdit != null ? reminderToEdit.getReminderId() : null,
                    boardID,
                    title,
                    description,
                    priority,
                    LocalDateTime.of(date, time)
            );

            addOrLoadReminders(isEdit ? Mode.LOAD : Mode.ADD, reminder);

            stage.close();
        });

        layoutContainer.getChildren().addAll(
                titleField,
                descriptionArea,
                timeDateRow,
                priorityColorRow,
                saveButton
        );

        scene = new Scene(layoutContainer, 350, 300);
        scene.getStylesheets().add("CSS/Kanban.css");

        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void createReminder(String titleText, String descriptionText, LocalDate date, LocalTime time, Reminder.Priority priority) {

        TitledPane reminderPane = new TitledPane();
        reminderPane.getStyleClass().add("pu_titledpane");
        reminderPane.setCollapsible(true);
        reminderPane.setMaxWidth(Double.MAX_VALUE);
        reminderPane.prefWidthProperty().bind(sidebarContentVbox.widthProperty());
        VBox.setVgrow(reminderPane, Priority.NEVER);

        ContextMenu reminderMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        MenuItem completeItem = new MenuItem("Complete");
        MenuItem deleteItem = new MenuItem("Delete");

        reminderMenu.getItems().addAll(
                editItem,
                completeItem,
                deleteItem
        );

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 5, 0, 5));
        header.setPrefHeight(40);
        header.maxWidthProperty().bind(reminderPane.widthProperty());

        HBox priorityBox = new HBox(2);
        priorityBox.setAlignment(Pos.CENTER_LEFT);


        int priorityCount = switch (priority) {
            case NONE -> 0;
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };


        for (int i = 0; i < priorityCount; i++) {
            ImageView icon = new ImageView(
                    new Image(getClass().getResource("/Images/exclamation.png").toExternalForm())
            );
            icon.setFitWidth(12);
            icon.setFitHeight(12);
            icon.setPreserveRatio(true);
            priorityBox.getChildren().add(icon);
        }

        Label title = new Label(titleText);
        title.setFont(Font.font(14));
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);


        header.getChildren().addAll(priorityBox, title);
        reminderPane.setGraphic(header);

        AnchorPane contentPane = new AnchorPane();
        contentPane.setPrefHeight(112);

        TextArea description = new TextArea(descriptionText);
        description.getStyleClass().add("pu_text_area");
        description.setWrapText(true);
        description.setEditable(false);
        description.setMouseTransparent(false);

        description.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !description.isEditable()) {
                description.setEditable(true);
                description.requestFocus();
                description.selectAll();
            }
        });

        description.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && description.isEditable()) {
                description.setEditable(false);
            }
        });


        AnchorPane.setTopAnchor(description, -10.0);
        AnchorPane.setBottomAnchor(description, 15.0);
        AnchorPane.setLeftAnchor(description, -10.0);
        AnchorPane.setRightAnchor(description, -10.0);

        HBox timeBox = new HBox();
        timeBox.setAlignment(Pos.CENTER_RIGHT);

        String timestampText = "";
        if (date != null && time != null) {
            timestampText =
                    time.format(DateTimeFormatter.ofPattern("h:mma"))
                            + " "
                            + date.format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        }

        Label timestamp = new Label(timestampText);
        timestamp.setPadding(new Insets(5));
        timeBox.getChildren().add(timestamp);

        AnchorPane.setTopAnchor(timeBox, 100.0);
        AnchorPane.setBottomAnchor(timeBox, -10.0);
        AnchorPane.setLeftAnchor(timeBox, -10.0);
        AnchorPane.setRightAnchor(timeBox, -10.0);

        contentPane.getChildren().addAll(description, timeBox);
        reminderPane.setContent(contentPane);

        sidebarContentVbox.getChildren().add(reminderPane);
    }

    private void times(ComboBox<String> comboBox) {

        for (int hour24 = 0; hour24 < 24; hour24++) {
            for (int minute = 0; minute <= 45; minute += 15) {

                int hour12 = hour24 % 12;
                if (hour12 == 0) hour12 = 12;

                String amPm = (hour24 < 12) ? "AM" : "PM";

                String time = String.format("%d:%02d%s", hour12, minute, amPm);
                comboBox.getItems().add(time);
            }
        }
    }

    private void slidingSidebar(MouseEvent event) {
        try {
            VBox source = (VBox) event.getSource();
            boolean clickedAgain = source == previousSidebarButton;
            previousSidebarButton = source;

            Sidebar section = Sidebar.valueOf(source.getId().toUpperCase());

            //Close
            if (sidebarOpen && clickedAgain) {

                Timeline fadeHeader = fadeSidebar(sidebarHeader, 1, 0, Duration.ZERO);
                fadeSidebar(sidebarScrollPane, 1, 0, Duration.ZERO);

                fadeHeader.setOnFinished(e -> {
                    sidebarExpansionVbox.getChildren().clear();
                    Timeline collapse = animateSidebar(sidebarExpansionVbox.getWidth(), 0);
                    collapse.setOnFinished(ev -> sidebarOpen = false);
                });
                return;
            }

            //Content
            if (sidebarOpen && !clickedAgain) {

                Timeline fadeOut = fadeSidebar(sidebarHeader, 1, 0, Duration.ZERO);
                fadeSidebar(sidebarScrollPane, 1, 0, Duration.ZERO);

                fadeOut.setOnFinished(e -> {
                    sidebarContent(section);

                    sidebarHeader.setOpacity(0);
                    sidebarScrollPane.setOpacity(0);

                    fadeSidebar(sidebarHeader, 0, 1, Duration.millis(40));
                    fadeSidebar(sidebarScrollPane, 0, 1, Duration.millis(40));
                });
                return;
            }

            //Open
            if (!sidebarOpen) {

                sidebarExpansionVbox.toFront();

                Timeline expand = animateSidebar(sidebarExpansionVbox.getWidth(), 275);

                expand.setOnFinished(e -> {
                    sidebarExpansionVbox.getChildren().setAll(sidebarHeader, sidebarScrollPane);
                    sidebarContent(section);

                    sidebarHeader.setOpacity(0);
                    sidebarScrollPane.setOpacity(0);

                    fadeSidebar(sidebarHeader, 0, 1, Duration.millis(40));
                    fadeSidebar(sidebarScrollPane, 0, 1, Duration.millis(40));
                });

                sidebarOpen = true;
            }

        } catch (Exception ignored) {}
    }

    private void sidebarContent(Sidebar section) {

        sidebarHeader.getChildren().clear();
        sidebarContentVbox.getChildren().clear();

        Label title = new Label();
        title.getStyleClass().add("content_label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button();
        addButton.setVisible(false);
        addButton.getStyleClass().add("add_button");

        sidebarHeader.getChildren().addAll(title, spacer, addButton);

        switch (section) {
            case CARDS:
                title.setText("Cards");
                addButton.setText("Create card");
                addButton.setVisible(true);
                addButton.setOnAction(e -> addOrLoadCards(sidebarContentVbox, Mode.ADD, Enums.Section.INBOX));
                addOrLoadCards(sidebarContentVbox, Mode.LOAD, Enums.Section.INBOX);
                break;

            case ARCHIVE:
                title.setText("Archive");
                break;

            /*case BOARDS:
                title.setText("Boards");
                addButton.setText("Create board");
                addButton.setVisible(true);
                addButton.setOnAction(e -> createBoardPopup());
                break;*/

            case DELETED:
                title.setText("Deleted");
                sidebarContentVbox.getChildren().add(new Label("Deleted section"));
                break;

            case REMINDERS:
                title.setText("Reminders");
                addButton.setText("Create reminder");
                addButton.setVisible(true);
                addButton.setOnAction(e -> remindersPopup(false, null));
                addOrLoadReminders(Mode.LOAD, null);
                break;

            case THEMES:
                title.setText("Themes");
                sidebarContentVbox.getChildren().add(new Label("Themes section"));
                break;
        }
    }

    private Timeline animateSidebar(double fromWidth, double toWidth) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebarExpansionVbox.prefWidthProperty(), fromWidth)
                ),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(sidebarExpansionVbox.prefWidthProperty(), toWidth)
                )
        );
        timeline.play();
        return timeline;
    }

    private Timeline fadeSidebar(Node node, double fromOpacity, double toOpacity, Duration delay) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), fromOpacity)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.opacityProperty(), toOpacity))
        );
        timeline.setDelay(delay);
        timeline.play();
        return timeline;
    }

    private void reorderIndices(Pane pane) {
        Long position = 0L;

        if (pane instanceof VBox) {
            List<Card> cardsToUpdate = new ArrayList<>();
            for (Node cardNode : pane.getChildren()) {
                if (!(cardNode instanceof StackPane)) continue;
                Object cardId = cardNode.getUserData();

                if (!(cardId instanceof Long)) continue;
                Card card = Card.builder().cardId((Long) cardId)
                        .cardPosition(position).build();
                cardsToUpdate.add(card);
                position++;
            }

            if (!cardsToUpdate.isEmpty()) {
                cardService.updateCardSectionBatch(cardsToUpdate, Enums.Section.POSITION);
            }
        }

        else if (pane instanceof HBox) {
            List<Column> columnsToUpdate = new ArrayList<>();
            for (Node columnNode : pane.getChildren()) {
                if (!(columnNode instanceof VBox columnContainer)) continue;
                if (!columnContainer.getStyleClass().contains("list_column")) {
                    continue;
                }
                Object columnId = columnNode.getUserData();

                if (!(columnId instanceof Long)) continue;

                Column column = new Column();
                column.setColumnId((Long) columnId);
                column.setColumnPosition(position);

                columnsToUpdate.add(column);
                position++;
            }

            if (!columnsToUpdate.isEmpty()) {
                columnService.updateColumnPositions(columnsToUpdate);
            }
        }
    }

    private void addOrLoadColumns(Mode mode) {
        boolean isLoad = mode == Mode.LOAD;
        if (isLoad) {
            columnData = columnService.findByStatus(boardID, Enums.LS.ACTIVE);
            columnData.sort(Comparator.comparing(Column::getColumnPosition));

        } else {
            columnData = new ArrayList<>();
            columnData.add(new Column());
        }


        columnData.forEach(column -> {
            VBox listContainer = new VBox();
            listContainer.setUserData(column.getColumnId());
            listContainer.getStyleClass().add("list_column");
            listContainer.setAlignment(Pos.TOP_CENTER);
            listContainer.setPrefWidth(275);
            listContainer.setMinWidth(Region.USE_PREF_SIZE);
            listContainer.setMaxWidth(Region.USE_PREF_SIZE);
            listContainer.maxHeightProperty().bind(boardHBox.heightProperty());

            HBox headerSection = new HBox();
            headerSection.getStyleClass().add("header_section");


            TextField headerTitle = new TextField(column.getTitle());
            headerTitle.getStyleClass().add("header_TF");
            HBox.setHgrow(headerTitle,Priority.ALWAYS);
            if (isLoad) {
                headerTitle.setDisable(true);
                headerTitle.setEditable(false);
                headerTitle.deselect();
            }

            headerTitle.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && !headerTitle.isDisabled()) {
                    headerTitle.setEditable(false);
                    headerTitle.setDisable(true);
                    String title = headerTitle.getText() == null || headerTitle.getText().isEmpty() ? "Untitled" : headerTitle.getText();
                    if(!title.equals(column.getTitle())){
                        column.setTitle(title);
                        headerTitle.setText(title);
                        columnService.updateColumnSection(column.getColumnId(),Enums.Section.TITLE, title);
                    }
                }
            });
            headerTitle.setOnAction(e -> {
                headerTitle.setEditable(false);
                headerTitle.setDisable(true);
                headerTitle.deselect();
                String title = headerTitle.getText() == null || headerTitle.getText().isEmpty() ? "Untitled" : headerTitle.getText();
                if(!title.equals(column.getTitle())){
                    if (!isLoad){
                        columnService.create(
                                Column.builder().boardId(boardID)
                                        .columnPosition((long) boardHBox.getChildren().size()-2).title(title)
                                        .hexColor("#FFFFFF").status(Enums.LS.ACTIVE).build());
                        headerTitle.setText(title);
                        boardHBox.getChildren().clear();
                        Platform.runLater(() ->{
                            addOrLoadColumns(Mode.LOAD);});
                        //Lazy solution: the issue of duplicates being saved when reediting a new list is still there.
                    } else {
                        column.setTitle(title);
                        headerTitle.setText(title);
                        columnService.updateColumnSection(column.getColumnId(), Enums.Section.TITLE,title);
                    }

                }
            });

            MenuButton listOptionsBtn = new MenuButton();
            listOptionsBtn.getStyleClass().add("list_options");
            listOptionsBtn.setGraphic(new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream("/Images/dots.png")))
            ));

            MenuItem addCard = new MenuItem("Add Card");
            MenuItem edit = new MenuItem("Edit");
            MenuItem archive = new MenuItem("Archive");
            MenuItem delete = new MenuItem("Delete");


            ColorPicker colorPicker = new ColorPicker(Color.web(
                    column.getHexColor() == null ? "#FFFFFF" : column.getHexColor()
            ));

            colorPicker.setMaxWidth(Double.MAX_VALUE);
            CustomMenuItem color = new CustomMenuItem(colorPicker);
            color.setHideOnClick(false);

            listOptionsBtn.getItems().addAll(addCard, edit, archive, delete);


            edit.setOnAction(e->{
                headerTitle.setDisable(false);
                headerTitle.setEditable(true);
                headerTitle.requestFocus();
                headerTitle.selectAll();
            });
            archive.setOnAction(e-> {
                boardHBox.getChildren().remove(listContainer);
                columnService.updateColumnSection(column.getColumnId(), Enums.Section.STATUS, Enums.LS.ARCHIVED);
                cardService.updateCardsToParentArchived(column.getColumnId());
            });
            delete.setOnAction(e -> {
                boardHBox.getChildren().remove(listContainer);
                columnService.updateColumnSection(column.getColumnId(), Enums.Section.STATUS, Enums.LS.DELETED);
                cardService.updateCardsToParentDeleted(column.getColumnId());
                reorderIndices(boardHBox);
            });

            headerSection.getChildren().addAll(headerTitle, listOptionsBtn);
            VBox visibleList = new VBox();
            visibleList.getStyleClass().add("visible_list");
            visibleList.setUserData(column.getColumnId());

            if (isLoad) {
                addOrLoadCards(visibleList, Mode.LOAD, Enums.Section.LIST);
            }
            addCard.setOnAction(event -> {
                addOrLoadCards(visibleList, Mode.ADD, null);
            });



            ScrollPane listScrollPane = new ScrollPane(visibleList);
            listScrollPane.getStyleClass().add("list_scrollP");
            listScrollPane.setFitToWidth(true);
            listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            listScrollPane.setMinHeight(0);

            HBox addCardSection = new HBox();
            addCardSection.getStyleClass().add("addCardSection");
            addCardSection.setAlignment(Pos.CENTER);

            Button addCardBtn = new Button("Add card");
            addCardBtn.getStyleClass().add("add_button");
            addCardBtn.setPrefWidth(275);
            addCardBtn.setOnAction(e ->
                    addOrLoadCards(visibleList, Mode.ADD, Enums.Section.LIST)
            );

            addCardSection.getChildren().add(addCardBtn);

            colorPicker.setOnAction(e -> {
                Color selectedColor = colorPicker.getValue();

                String hexColor = String.format(
                        "#%02X%02X%02X",
                        (int) (selectedColor.getRed() * 255),
                        (int) (selectedColor.getGreen() * 255),
                        (int) (selectedColor.getBlue() * 255)
                );

                column.setHexColor(hexColor);

                headerSection.setStyle("-fx-background-color: " + hexColor + ";");
                visibleList.setStyle("-fx-background-color: " + hexColor + ";");
                addCardSection.setStyle("-fx-background-color: " + hexColor + ";");

                columnService.updateColumnSection(column.getColumnId(), Enums.Section.COLOR,hexColor);
                colorPicker.hide();
                listOptionsBtn.hide();
            });

            headerSection.setStyle("-fx-background-color: " + column.getHexColor() + ";");
            visibleList.setStyle("-fx-background-color: " + column.getHexColor() + ";");
            addCardSection.setStyle("-fx-background-color: " + column.getHexColor() + ";");

            listContainer.getChildren().addAll(
                    headerSection,
                    listScrollPane,
                    addCardSection
            );

            makeListDraggable(column, listContainer, headerSection, listScrollPane, addCardSection);

            if (isLoad) {
                boardHBox.getChildren().add(listContainer);
            } else {
                int insertIndex = Math.max(0, boardHBox.getChildren().size() - 1);
                boardHBox.getChildren().add(insertIndex, listContainer);
            }
        });


        if (boardHBox.lookup("#addListButton") == null) {

            Button addListButton = new Button("Add a list");
            addListButton.setId("addListButton");
            addListButton.getStyleClass().add("add_button");
            addListButton.setMinSize(120, 30);

            ImageView plusIcon = new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream("/Images/plusButton.png")))
            );
            plusIcon.setFitWidth(12);
            plusIcon.setFitHeight(12);
            plusIcon.setPreserveRatio(true);

            addListButton.setGraphic(plusIcon);
            addListButton.setOnAction(e ->
                    addOrLoadColumns(Mode.ADD)
            );

            boardHBox.getChildren().add(addListButton);
            boardScrollPane.setHvalue(0);
        }

    }

    private void addOrLoadReminders(Mode mode, Reminder reminderInfo) {
        if (mode == Mode.ADD) {
            reminderService.create(reminderInfo);
            addOrLoadReminders(Mode.LOAD, null);
            return;
        }

        if (reminderInfo != null && reminderInfo.getReminderId() != null) {
            Map<Enums.Section, Object> updates = new HashMap<>();
            updates.put(Enums.Section.TITLE, reminderInfo.getReminderTitle());
            updates.put(Enums.Section.DESCRIPTION, reminderInfo.getDescription());
            updates.put(Enums.Section.PRIORITY, reminderInfo.getPriority());
            updates.put(Enums.Section.DUE_DATE, reminderInfo.getDueDate());

            reminderService.updateReminderSections(
                    reminderInfo.getReminderId(),
                    updates
            );

            addOrLoadReminders(Mode.LOAD, null);
            return;
        }

        sidebarContentVbox.getChildren().clear();

        List<Reminder> reminderData = reminderService.findByBoardId(boardID);
        if (reminderData == null || reminderData.isEmpty()) return;

        reminderData.sort(
                Comparator.comparing(
                        Reminder::getReminderId,
                        Comparator.nullsLast(Long::compareTo)
                ).reversed()
        );

        for (Reminder reminder : reminderData) {
            TitledPane reminderPane = new TitledPane();
            reminderPane.getStyleClass().add("pu_titledpane");
            reminderPane.setCollapsible(true);
            reminderPane.setExpanded(false);
            reminderPane.setMaxWidth(Double.MAX_VALUE);
            reminderPane.setMinWidth(0);
            reminderPane.prefWidthProperty().bind(
                    sidebarContentVbox.widthProperty().subtract(8)
            );

            HBox header = new HBox(6);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(10));
            header.setMinHeight(70);
            header.setPrefHeight(Region.USE_COMPUTED_SIZE);
            header.setMaxHeight(Region.USE_PREF_SIZE);
            header.setMinWidth(0);
            header.prefWidthProperty().bind(
                    sidebarContentVbox.widthProperty().subtract(35)
            );

            HBox priorityBox = new HBox(2);
            priorityBox.setAlignment(Pos.CENTER_LEFT);
            priorityBox.setMinWidth(Region.USE_PREF_SIZE);
            priorityBox.setMaxWidth(Region.USE_PREF_SIZE);

            int priorityCount = switch (reminder.getPriority()) {
                case NONE -> 0;
                case LOW -> 1;
                case MEDIUM -> 2;
                case HIGH -> 3;
            };

            for (int i = 0; i < priorityCount; i++) {
                ImageView icon = new ImageView(
                        new Image(getClass()
                                .getResource("/Images/exclamation.png")
                                .toExternalForm())
                );
                icon.setFitWidth(12);
                icon.setFitHeight(12);
                icon.setPreserveRatio(true);
                priorityBox.getChildren().add(icon);
            }

            Label title = new Label(reminder.getReminderTitle());
            title.setFont(Font.font(14));
            title.setWrapText(true);
            title.setMinWidth(0);
            title.setMaxWidth(Double.MAX_VALUE);
            title.prefWidthProperty().bind(
                    header.widthProperty()
                            .subtract(priorityBox.widthProperty())
                            .subtract(12)
            );
            HBox.setHgrow(title, Priority.ALWAYS);

            header.getChildren().addAll(priorityBox, title);
            reminderPane.setGraphic(header);

            ContextMenu reminderContextMenu = new ContextMenu();
            MenuItem edit = new MenuItem("Edit");
            MenuItem delete = new MenuItem("Delete");
            reminderContextMenu.getItems().addAll(edit, delete);

            reminderPane.setOnContextMenuRequested(e -> {
                reminderContextMenu.show(
                        reminderPane,
                        e.getScreenX(),
                        e.getScreenY()
                );
                e.consume();
            });

            VBox contentBox = new VBox(6);
            contentBox.setPadding(new Insets(8));
            contentBox.setMaxWidth(Double.MAX_VALUE);
            contentBox.setMinWidth(0);

            Label description = new Label(reminder.getDescription());
            description.setWrapText(true);
            description.setMinWidth(0);
            description.setMaxWidth(Double.MAX_VALUE);
            description.prefWidthProperty().bind(
                    sidebarContentVbox.widthProperty().subtract(25)
            );

            Label timestamp = new Label();

            if (reminder.getDueDate() != null)
                timestamp.setText(
                        reminder.getDueDate().toLocalTime().format(formatter)
                                + " "
                                + reminder.getDueDate().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("M/d/yyyy"))
                );

            timestamp.setWrapText(true);
            timestamp.setMinWidth(0);
            timestamp.setMaxWidth(Double.MAX_VALUE);

            contentBox.getChildren().addAll(description, timestamp);
            reminderPane.setContent(contentBox);

            edit.setOnAction(e -> remindersPopup(true, reminder));

            delete.setOnAction(e -> {
                reminderService.delete(reminder.getReminderId());
                sidebarContentVbox.getChildren().remove(reminderPane);
            });

            sidebarContentVbox.getChildren().add(reminderPane);
        }
    }

    private void addOrLoadCards(VBox visibleList, Mode mode, Enums.Section section) {
        /*
        TODO: New cards should be saved on ENTER not patched.
        * */
        boolean isAdd = mode == Mode.ADD;
        if (!isAdd) {

            if (section == Enums.Section.LIST) {
                cardData = cardService.findAllCardsByColumnId((Long) visibleList.getUserData());

            }
            else if (section == Enums.Section.INBOX) {
                cardData = cardService.findByStatus(boardID, Enums.CS.INBOXED);
            }

            if (cardData == null || cardData.isEmpty()) return;

            cardData.forEach(cardData -> {
                StackPane card = buildCardUI(
                        visibleList,
                        cardData,
                        false
                );
                visibleList.getChildren().add(card);
            });

            return;
        }

        Card newCard = new Card();
        newCard.setDescription("Untitled");

        StackPane card = buildCardUI(visibleList, newCard, true);

        visibleList.getChildren().add(0, card);
    }

    private StackPane buildCardUI(VBox visibleList, Card cardData, boolean startEditing) {
        StackPane card = new StackPane();
        card.getStyleClass().add("card");
        card.setPadding(new Insets(6));
        card.setUserData(cardData.getCardId());
        VBox.setVgrow(card, Priority.NEVER);

        TextArea text = new TextArea(cardData.getDescription());
        text.getStyleClass().add("card_textA");
        text.setWrapText(true);
        text.setPrefRowCount(1);
        text.setMinHeight(Region.USE_PREF_SIZE);
        text.setPrefHeight(Region.USE_COMPUTED_SIZE);
        text.setMaxHeight(Double.MAX_VALUE);
        text.setEditable(startEditing);
        text.setMouseTransparent(!startEditing);

        text.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.ENTER && !ke.isControlDown()) {
                text.setEditable(false);
                text.setMouseTransparent(true);
                text.deselect();
                String description = text.getText().isEmpty() ? "Untitled" : text.getText();
                if (cardData.getCardId() == null) {
                    boolean creatingInboxCard = visibleList == sidebarContentVbox;
                    Card transferCard = Card.builder()
                            .columnId(creatingInboxCard ? null : (Long) visibleList.getUserData())
                            .boardId(boardID).cardPosition(creatingInboxCard ? null : 0L)
                            .description(description)
                            .hexColor("#FFFFFF")
                            .status(creatingInboxCard ? Enums.CS.INBOXED : Enums.CS.ACTIVE)
                            .build();
                    Card createdCard = cardService.createCard(transferCard);
                    cardData.setCardId(createdCard.getCardId());
                    card.setUserData(createdCard.getCardId());
                    if (visibleList != sidebarContentVbox) {
                        reorderIndices(visibleList);
                    }
                    card.requestFocus();
                } else {
                cardService.updateCardSection(cardData.getCardId(), text.getText(), Enums.Section.DESCRIPTION);
            }
                ke.consume();
            }
        });

        StackPane.setAlignment(text, Pos.TOP_LEFT);

        MenuButton cardOptions = new MenuButton();
        cardOptions.getStyleClass().add("card_options");
        StackPane.setAlignment(cardOptions, Pos.TOP_RIGHT);

        MenuItem edit = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");
        MenuItem color = new MenuItem("Color");
        MenuItem inbox = new MenuItem("Inbox");
        edit.setOnAction(e -> {
            text.setEditable(true);
            text.setMouseTransparent(false);
            text.requestFocus();
            Platform.runLater(text::selectAll);
        });
        delete.setOnAction(e ->{
            if (card.getParent() instanceof VBox currentParent) {
                currentParent.getChildren().remove(card);

                if (currentParent != sidebarContentVbox) {
                    reorderIndices(currentParent);
                }
            }
            cardService.deleteCard(cardData.getCardId());
        });
        inbox.setOnAction(e->{
            if (card.getParent() instanceof VBox currentParent) {
                currentParent.getChildren().remove(card);

                if (currentParent != sidebarContentVbox) {
                    reorderIndices(currentParent);
                }
            }
            cardService.updateCardToInboxed(cardData.getCardId());

        });

        cardOptions.getItems().addAll(edit, delete, inbox, color);
        card.getChildren().addAll(text, cardOptions);

        if (startEditing) {
            Platform.runLater(() -> {
                text.requestFocus();
                text.selectAll();
            });
        }

        makeCardDraggable(visibleList, card, text);

        return card;
    }

    private TextArea createArchiveCard(Card card) {
        TextArea area = new TextArea(card.getDescription());
        area.setPrefWidth(230);
        area.setPrefHeight(45);
        area.setEditable(false);
        area.setWrapText(true);
        area.getStyleClass().add("archive_card");
        ContextMenu contextMenu = new ContextMenu();
        MenuItem recover = new MenuItem("Recover card");
        contextMenu.getItems().add(recover);
        area.setOnContextMenuRequested(e -> {
            contextMenu.show(area, e.getScreenX(),e.getScreenY());
            recover.setOnAction(f -> sidebarContentVbox.getChildren().remove(area));
        });

        /*if (card.getHexColor() != null) {
            area.setStyle("-fx-background-color: " + card.getHexColor() + ";");
        }*/

        return area;
    }

    public void addOrLoadArchiveContent(ArrayList<Column> archivedColumns, ArrayList<Card> archivedCards, Mode mode) {

        if (mode != Mode.LOAD) {
            return;
        }

        sidebarContentVbox.getChildren().clear();

        Map<Long, ArrayList<Card>> cardsByListId = new HashMap<>();
        ArrayList<Card> standaloneArchivedCards = new ArrayList<>();

        for (Card card : archivedCards) {

            if (card.getStatus() == Enums.CS.PARENT_ARCHIVED) {
                cardsByListId
                        .computeIfAbsent(card.getColumnId(), k -> new ArrayList<>())
                        .add(card);
            }
            else if (card.getStatus() == Enums.CS.ARCHIVED) {
                standaloneArchivedCards.add(card);
            }
        }

        if (!archivedColumns.isEmpty()) {

            for (Column column : archivedColumns) {

                TitledPane archivePane = new TitledPane();
                archivePane.setText(column.getTitle());
                archivePane.getStyleClass().add("archive_list");

                ContextMenu contextMenu = new ContextMenu();
                MenuItem recover = new MenuItem("Recover");
                contextMenu.getItems().add(recover);
                archivePane.setOnContextMenuRequested(e -> {
                    contextMenu.show(archivePane, e.getScreenX(),e.getScreenY());
                    recover.setOnAction(f -> sidebarContentVbox.getChildren().remove(archivePane));
                });

                AnchorPane contentPane = new AnchorPane();
                contentPane.setMinHeight(0);
                contentPane.setMinWidth(0);

                double topOffset = 0;

                ArrayList<Card> listCards =
                        cardsByListId.getOrDefault(column.getColumnId(), new ArrayList<>());

                for (Card card : listCards) {

                    TextArea cardNode = createArchiveCard(card);

                    AnchorPane.setTopAnchor(cardNode, topOffset);
                    AnchorPane.setLeftAnchor(cardNode, 0.0);
                    AnchorPane.setRightAnchor(cardNode, 0.0);

                    contentPane.getChildren().add(cardNode);
                    topOffset += 50;
                }

                archivePane.setContent(contentPane);
                sidebarContentVbox.getChildren().add(archivePane);
            }
        }

        for (Card card : standaloneArchivedCards) {
            TextArea cardNode = createArchiveCard(card);
            sidebarContentVbox.getChildren().add(cardNode);
        }
    }

    private void makeListDraggable(Column column, VBox listContainer, HBox headerSection, ScrollPane listScrollPane, HBox addCardSection) {

        listContainer.setOnMousePressed(e -> {
            draggedList = listContainer;

            // Create ghost snapshot
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            ImageView ghostImg = new ImageView(listContainer.snapshot(params, null));
            ghostImg.setOpacity(0.30);

            ghostList = new Pane(ghostImg);
            ghostList.setMouseTransparent(true);

            // BEFORE ADDING TO ROOT → place it where it should appear
            Bounds b = listContainer.localToScene(listContainer.getBoundsInLocal());

            double startX = b.getMinX() + DRAG_VISUAL_OFFSET;
            double startY = b.getMinY() + DRAG_VISUAL_OFFSET;

            ghostList.setLayoutX(startX);
            ghostList.setLayoutY(startY);

            dragOffsetX = e.getSceneX() - startX;
            dragOffsetY = e.getSceneY() - startY;

            double visibleListHeight = Math.min(listScrollPane.getHeight(), listContainer.getHeight());
            //Noticeable shifting of components happen if using original height and widths,
            //Subtracting by 0.25 fixes it.

            double placeholderHeight = headerSection.getHeight() + visibleListHeight-0.25
                    + addCardSection.getHeight();

            placeholder = new Rectangle(listContainer.getWidth()-0.25, placeholderHeight);
            placeholder.setArcWidth(10);
            placeholder.setArcHeight(10);
            placeholder.setFill(Color.rgb(0, 0, 0, 0.18));

            // Insert placeholder where the list originally was
            int index = boardHBox.getChildren().indexOf(listContainer);
            boardHBox.getChildren().add(index, placeholder);
            boardHBox.getChildren().remove(listContainer);


            Pane rootPane = (Pane) boardHBox.getScene().getRoot();
            rootPane.getChildren().add(ghostList);
        });


        listContainer.setOnMouseDragged(e -> {
            if (draggedList == null) return;

            // Move ghost freely
            ghostList.setLayoutX(e.getSceneX() - dragOffsetX);
            ghostList.setLayoutY(e.getSceneY() - dragOffsetY);

            int newIndex = getDropIndex(e.getSceneX());
            movePlaceholder(newIndex);
        });


        listContainer.setOnMouseReleased(e -> {
            if (draggedList == null) return;

            int dropIndex = getDropIndex(e.getSceneX());

            // Place list back into the layout
            boardHBox.getChildren().remove(placeholder);
            boardHBox.getChildren().add(dropIndex, draggedList);
            reorderIndices(boardHBox);

            // Clean up ghost
            Pane rootPane = (Pane) boardHBox.getScene().getRoot();
            rootPane.getChildren().remove(ghostList);

            ghostList = null;
            draggedList.setOpacity(1);
            draggedList = null;
            placeholder = null;
        });
    }

    private void makeCardDraggable(VBox visibleList, StackPane card, TextArea text) {
        final StackPane[] ghostCard = {null};
        final Rectangle[] placeholder = {null};
        final Group[] overlayRef = {null};
        final VBox[] sourceListRef = {null};

        card.setOnMousePressed(e -> {
            if (text.isEditable() || e.getTarget() instanceof MenuButton) return;

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            ImageView ghostImg = new ImageView(card.snapshot(params, null));
            ghostImg.setOpacity(0.36);

            ghostCard[0] = new StackPane(ghostImg);
            ghostCard[0].setManaged(false);
            ghostCard[0].setMouseTransparent(true);

            Parent root = boardHBox.getScene().getRoot();

            if (root instanceof Pane pane) {
                overlayRef[0] = new Group();
                overlayRef[0].getProperties().put("kanbanOverlay", true);
                pane.getChildren().add(overlayRef[0]);
            }

            Point2D start = overlayRef[0].sceneToLocal(e.getSceneX(), e.getSceneY());
            ghostCard[0].relocate(start.getX(), start.getY());
            overlayRef[0].getChildren().add(ghostCard[0]);

            placeholder[0] = new Rectangle(card.getWidth() - 0.25, card.getHeight() - 0.25);
            placeholder[0].setFill(Color.rgb(0, 0, 0, 0.18));
            placeholder[0].setArcWidth(10);
            placeholder[0].setArcHeight(10);

            VBox parent = (VBox) card.getParent();
            int index = parent.getChildren().indexOf(card);
            parent.getChildren().add(index, placeholder[0]);
            sourceListRef[0] = (VBox) card.getParent();
            parent.getChildren().remove(card);
        });

        card.setOnMouseDragged(e -> {
            if (ghostCard[0] == null || overlayRef[0] == null) return;

            Point2D dragPoint = overlayRef[0].sceneToLocal(e.getSceneX(), e.getSceneY());
            ghostCard[0].relocate(dragPoint.getX(), dragPoint.getY());

            VBox targetList = findCardDropTarget(e);

            if (targetList == null) return;

            if (placeholder[0].getParent() != null) {
                ((VBox) placeholder[0].getParent()).getChildren().remove(placeholder[0]);
            }

            Bounds ghostBounds = ghostCard[0].localToScene(ghostCard[0].getBoundsInLocal());
            double centerY = ghostBounds.getCenterY();

            int insertIndex = 0;
            for (Node n : targetList.getChildren()) {
                Bounds nb = n.localToScene(n.getBoundsInLocal());
                if (centerY > nb.getMinY() + nb.getHeight() / 2) {
                    insertIndex++;
                }
            }

            targetList.getChildren().add(insertIndex, placeholder[0]);
        });

        card.setOnMouseReleased(e -> {
            if (ghostCard[0] == null) return;

            if (overlayRef[0] != null) {
                overlayRef[0].getChildren().remove(ghostCard[0]);

                if (overlayRef[0].getParent() instanceof Pane pane) {
                    pane.getChildren().remove(overlayRef[0]);
                }
            }

            VBox sourceList = sourceListRef[0];

            if (placeholder[0] == null || placeholder[0].getParent() == null) {
                sourceList.getChildren().add(card);
            } else {
                VBox targetList = (VBox) placeholder[0].getParent();
                int index = targetList.getChildren().indexOf(placeholder[0]);

                targetList.getChildren().remove(placeholder[0]);
                targetList.getChildren().add(index, card);

                Object cardIdObject = card.getUserData();

                if (cardIdObject instanceof Long cardId) {

                    boolean movedFromInboxToList =
                            sourceList == sidebarContentVbox && targetList != sidebarContentVbox;

                    boolean movedFromListToInbox =
                            sourceList != sidebarContentVbox && targetList == sidebarContentVbox;

                    boolean movedListToList =
                            sourceList != sidebarContentVbox && targetList != sidebarContentVbox;

                    if (sourceList != sidebarContentVbox) {
                        reorderIndices(sourceList);
                    }

                    if (targetList != sidebarContentVbox) {
                        reorderIndices(targetList);
                    }

                    if (movedFromInboxToList) {
                        Object targetListIdObject = targetList.getUserData();

                        if (targetListIdObject instanceof Long newListId) {
                            cardService.updateCardSections(cardId,Map.of(
                                    Enums.Section.ID, newListId,
                                    Enums.Section.STATUS, Enums.CS.ACTIVE));
                        }
                    }

                    if (movedFromListToInbox) {
                        cardService.updateCardToInboxed(cardId);
                    }

                    if (movedListToList && sourceList != targetList) {
                        Object targetListIdObject = targetList.getUserData();

                        if (targetListIdObject instanceof Long newListId) {
                            cardService.updateCardSection(cardId, newListId, Enums.Section.ID);
                        }
                    }
                }
            }

            text.setEditable(false);
            text.setMouseTransparent(true);
            card.requestFocus();

            ghostCard[0] = null;
            placeholder[0] = null;
            sourceListRef[0] = null;
        });
    }

    private VBox findCardDropTarget(MouseEvent e) {
        if (sidebarOpen && previousSidebarButton == cards) {
            Bounds sidebarBounds = sidebarContentVbox.localToScene(sidebarContentVbox.getBoundsInLocal());

            if (e.getSceneX() > sidebarBounds.getMinX()
                    && e.getSceneX() < sidebarBounds.getMaxX()
                    && e.getSceneY() > sidebarBounds.getMinY()
                    && e.getSceneY() < sidebarBounds.getMaxY()) {
                return sidebarContentVbox;
            }
        }

        for (Node listContainer : boardHBox.getChildren()) {
            if (listContainer instanceof VBox outer
                    && outer.getChildren().size() > 1
                    && outer.getChildren().get(1) instanceof ScrollPane sp
                    && sp.getContent() instanceof VBox list) {

                Bounds lb = list.localToScene(list.getBoundsInLocal());

                if (e.getSceneX() > lb.getMinX()
                        && e.getSceneX() < lb.getMaxX()
                        && e.getSceneY() > lb.getMinY()
                        && e.getSceneY() < lb.getMaxY()) {
                    return list;
                }
            }
        }

        return null;
    }

    private int getDropIndex(double sceneX) {
        int index = 0;

        for (Node n : boardHBox.getChildren()) {

            // Skip the Rectangle placeholder
            if (n == placeholder) continue;
            if (n instanceof Button) continue;

            // Only compare real lists
            Bounds bounds = n.localToScene(n.getBoundsInLocal());
            double listCenter = bounds.getMinX() + bounds.getWidth() / 2;

            // If dragging past the center of this list, increase index
            if (sceneX > listCenter) {
                index++;
            }
        }

        return index;
    }

    private void movePlaceholder(int newIndex) {
        boardHBox.getChildren().remove(placeholder);
        boardHBox.getChildren().add(newIndex, placeholder);
    }

    public void switchToGPT(ActionEvent event) {
        handler.switchScene(event, "AI", consumer->{
            AI_AssistantFX ai = (AI_AssistantFX) consumer;
            ai.GETChatlogs();
        });
    }

    public void switchToNotebook(ActionEvent event) {
        handler.switchScene(event, "notebook", consumer->{
            NotebookFX notebooks = (NotebookFX) consumer;
            notebooks.GETNotebooks();
        });
    }

    public void switchToTasks(ActionEvent event) {
        handler.switchScene(event, "tasks", consumer->{
            ToDoFX toDoFX = (ToDoFX) consumer;
            toDoFX.getByPosted();
        });
    }
}