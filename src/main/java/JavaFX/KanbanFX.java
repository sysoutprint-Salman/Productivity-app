package JavaFX;

import SpringBoot.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.WritableValue;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
    @FXML private VBox themes;

    @FXML private Label menuLabel;
    @FXML private Label cardsLabel;
    @FXML private Label filterLabel;
    @FXML private Label archiveLabel;
    @FXML private Label boardsLabel;
    @FXML private VBox sidebar;
    @FXML private VBox sidebarExpansionVbox;

    private Stage stage;
    private Scene scene;
    private VBox layoutContainer;
    private enum Sidebar{CARDS, ARCHIVE, BOARDS, DELETED, REMINDERS, THEMES}
    private final VBox sidebarContentVbox = new VBox();
    private final HBox sidebarHeader = new HBox();
    private final Label headerTitle = new Label();
    private final ScrollPane sidebarScrollPane = new ScrollPane();
    private Label label;
    private double dragOffsetX;
    private boolean dragging = false;
    private boolean sidebarOpen = false;
    private VBox previousSidebarButton;
    private VBox draggedList = null;
    private Pane ghostList = null;
    private Rectangle placeholder = null;
    private double dragOffsetY;
    private SwitchScenes handler = new SwitchScenes();
    private static final double DRAG_VISUAL_OFFSET = 5;

    public KanbanFX(){}

    @FXML
    private void initialize() {
        createInitialLists();
        cards.setOnMousePressed(this::slidingSidebar);
        reminders.setOnMousePressed(this::slidingSidebar);
        themes.setOnMousePressed(this::slidingSidebar);
        archive.setOnMousePressed(this::slidingSidebar);
        boards.setOnMousePressed(this::slidingSidebar);
        deleted.setOnMousePressed(this::slidingSidebar);

        sidebarContentVbox.getStyleClass().add("sidebar_content");
        sidebarContentVbox.setPrefHeight(Region.USE_COMPUTED_SIZE);

        sidebarHeader.setPrefHeight(Region.USE_COMPUTED_SIZE);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        sidebarHeader.getStyleClass().add("sidebar_header");

        sidebarScrollPane.setFitToWidth(true);
        sidebarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane.getStyleClass().add("sidebar_scroll");
        sidebarScrollPane.setContent(sidebarContentVbox);

        VBox.setVgrow(sidebarScrollPane,Priority.ALWAYS);
        VBox.setVgrow(sidebarContentVbox, Priority.ALWAYS);
    }

    private void cardsPopup() {
        stage = new Stage();
        stage.setTitle("Create cards");

        // Root
        layoutContainer = new VBox();
        layoutContainer.getStyleClass().add("pu_vbox");

        // Label
        //Label contentLabel = new Label("Card content:");

        // TextArea
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.getStyleClass().add("pu_text_area");
        VBox.setVgrow(descriptionArea, Priority.ALWAYS);

        // HBox row
        HBox controlsRow = new HBox();
        controlsRow.getStyleClass().add("pu_hbox");

        Label countLabel = new Label("Count:");

        Spinner<Integer> countSpinner =
                new Spinner<>(0, Integer.MAX_VALUE, 0);
        countSpinner.setEditable(true);
        countSpinner.getStyleClass().add("pu_spinner");
        countSpinner.setPrefWidth(80);

        Label colorLabel = new Label("Color:");

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setPromptText("Task Color");
        colorPicker.getStyleClass().add("pu_color_picker");

        controlsRow.getChildren().addAll(
                countLabel,
                countSpinner,
                colorLabel,
                colorPicker
        );

        // Button
        Button createButton = new Button("Create");
        createButton.getStyleClass().add("pu_buttons");

        // Assemble layout
        layoutContainer.getChildren().addAll(
                descriptionArea,
                controlsRow,
                createButton
        );

        scene = new Scene(layoutContainer, 350, 300);
        scene.getStylesheets().add("CSS/Kanban.css");

        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void remindersPopup() {
        stage = new Stage();
        stage.setTitle("Create reminder");

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
        timeCombo.getItems().addAll();

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
        priorityCombo.setVisibleRowCount(3);
        priorityCombo.getStyleClass().add("pu_choicebox");
        priorityCombo.getItems().addAll("Low", "Medium", "High");

        Label colorLabel = new Label("Color:");

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setPromptText("Task Color");
        colorPicker.getStyleClass().add("pu_color_picker");

        priorityColorRow.getChildren().addAll(
                priorityLabel,
                priorityCombo,
                colorLabel,
                colorPicker
        );

        Button createButton = new Button("Create");
        createButton.getStyleClass().add("pu_buttons");

        layoutContainer.getChildren().addAll(
                titleField,
                descriptionArea,
                timeDateRow,
                priorityColorRow,
                createButton
        );

        scene = new Scene(layoutContainer, 350, 300);
        scene.getStylesheets().add("CSS/Kanban.css");

        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void createBoardPopup() {

        final double ENTRY_HEIGHT = 30;
        final double BASE_HEIGHT = 280;

        stage = new Stage();
        stage.setTitle("Create Board");

        AnchorPane root = new AnchorPane();
        root.setPrefWidth(350);
        root.setPrefHeight(BASE_HEIGHT);

        VBox contentVBox = new VBox(10);
        contentVBox.getStyleClass().add("pu_vbox");

        AnchorPane.setTopAnchor(contentVBox, 0.0);
        AnchorPane.setLeftAnchor(contentVBox, 0.0);
        AnchorPane.setRightAnchor(contentVBox, 0.0);
        AnchorPane.setBottomAnchor(contentVBox, 50.0);

        TextField boardTitle = new TextField();
        boardTitle.setPromptText("Board title");
        boardTitle.getStyleClass().add("pu_text_fields");

        HBox listCreationRow = new HBox(10);
        listCreationRow.getStyleClass().add("pu_hbox");

        CheckBox noListsCheckBox = new CheckBox("No Lists");
        noListsCheckBox.getStyleClass().add("pu_checkbox");

        TextField listTitlesField = new TextField();
        listTitlesField.setPromptText("List titles");
        listTitlesField.setPrefHeight(31);
        listTitlesField.getStyleClass().add("pu_text_fields");
        HBox.setHgrow(listTitlesField, Priority.ALWAYS);

        ImageView addIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/Images/plusButton.png"))
        );
        addIcon.setFitWidth(12);
        addIcon.setFitHeight(12);
        addIcon.setPreserveRatio(true);

        Button addListButton = new Button();
        addListButton.getStyleClass().add("add_button");
        addListButton.setGraphic(addIcon);

        listCreationRow.getChildren().addAll(
                noListsCheckBox,
                listTitlesField,
                addListButton
        );

        VBox listEntriesBox = new VBox(5);

        addListButton.setOnAction(e -> {
            String title = listTitlesField.getText().trim();
            if (title.isEmpty() || noListsCheckBox.isSelected()) {
                return;
            }

            HBox entry = new HBox(10);
            entry.setPrefHeight(ENTRY_HEIGHT);
            entry.setMinHeight(ENTRY_HEIGHT);
            entry.setMaxHeight(ENTRY_HEIGHT);
            entry.getStyleClass().add("pu_hbox");

            Label titleLabel = new Label(title);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label countLabel = new Label("Card count:");

            Spinner<Integer> spinner = new Spinner<>(0, 100, 0);
            spinner.setPrefWidth(60);
            spinner.setPrefHeight(27);
            spinner.getStyleClass().add("pu_spinner");

            ImageView removeIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/Images/x.png"))
            );
            removeIcon.setFitWidth(12);
            removeIcon.setFitHeight(12);
            removeIcon.setPreserveRatio(true);

            Button removeButton = new Button();
            removeButton.setPrefWidth(30);
            removeButton.getStyleClass().add("add_button");
            removeButton.setGraphic(removeIcon);

            removeButton.setOnAction(ev -> {
                listEntriesBox.getChildren().remove(entry);
                stage.setHeight(stage.getHeight() - ENTRY_HEIGHT);

            });

            entry.getChildren().addAll(
                    titleLabel,
                    spacer,
                    countLabel,
                    spinner,
                    removeButton
            );

            listEntriesBox.getChildren().add(entry);
            stage.setHeight(stage.getHeight() + ENTRY_HEIGHT);
            listTitlesField.clear();
        });

        HBox themeRow = new HBox(10);
        themeRow.getStyleClass().add("pu_hbox");

        Label themeLabel = new Label("Theme:");
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.setPrefWidth(184);
        themeCombo.setPrefHeight(27);
        themeCombo.getStyleClass().add("pu_choicebox");
        HBox.setHgrow(themeCombo, Priority.ALWAYS);

        themeRow.getChildren().addAll(themeLabel, themeCombo);

        HBox templateRow = new HBox(10);
        templateRow.getStyleClass().add("pu_hbox");

        Label templateLabel = new Label("Templates");
        ComboBox<String> templateCombo = new ComboBox<>();
        templateCombo.setPrefWidth(184);
        templateCombo.setPrefHeight(27);
        templateCombo.getStyleClass().add("pu_choicebox");
        HBox.setHgrow(templateCombo, Priority.ALWAYS);

        templateRow.getChildren().addAll(templateLabel, templateCombo);

        contentVBox.getChildren().addAll(
                boardTitle,
                listCreationRow,
                listEntriesBox,
                themeRow,
                templateRow
        );

        HBox buttonBar = new HBox();
        AnchorPane.setBottomAnchor(buttonBar, 20.0);
        AnchorPane.setLeftAnchor(buttonBar, 20.0);
        AnchorPane.setRightAnchor(buttonBar, 20.0);

        Button createButton = new Button("Create");
        createButton.getStyleClass().add("pu_buttons");

        buttonBar.getChildren().add(createButton);

        root.getChildren().addAll(contentVBox, buttonBar);

        scene = new Scene(root);
        scene.getStylesheets().add("CSS/Kanban.css");

        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void times(ComboBox<String> comboBox) {

        for (int hour24 = 0; hour24 < 24; hour24++) {
            for (int minute = 0; minute <= 45; minute += 15) {

                int hour12 = hour24 % 12;
                if (hour12 == 0) hour12 = 12;

                String amPm = (hour24 < 12) ? "am" : "pm";

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

            /* ---------- CLOSE ---------- */
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

            /* ---------- SWITCH CONTENT ---------- */
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

            /* ---------- OPEN ---------- */
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
                addButton.setOnAction(e -> cardsPopup());

                for (int i = 0; i < 6; i++) {
                    setCards(sidebarContentVbox);
                }
                break;

            case ARCHIVE:
                title.setText("Archive");
                sidebarContentVbox.getChildren().add(new Label("Archive section"));
                break;

            case BOARDS:
                title.setText("Boards");
                addButton.setText("Create board");
                addButton.setVisible(true);
                addButton.setOnAction(e -> createBoardPopup());
                sidebarContentVbox.getChildren().add(new Label("Boards section"));
                break;

            case DELETED:
                title.setText("Deleted");
                sidebarContentVbox.getChildren().add(new Label("Deleted section"));
                break;

            case REMINDERS:
                title.setText("Reminders");
                addButton.setText("Create reminder");
                addButton.setVisible(true);
                addButton.setOnAction(e -> remindersPopup());
                sidebarContentVbox.getChildren().add(new Label("Reminders section"));
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

    private void createInitialLists() {

        for (int i = 1; i <= 2; i++) {

            VBox listContainer = new VBox();
            listContainer.getStyleClass().add("list_column");
            listContainer.setAlignment(Pos.TOP_CENTER);
            listContainer.setPrefWidth(275);
            listContainer.setMinWidth(Region.USE_PREF_SIZE);
            listContainer.setMaxWidth(Region.USE_PREF_SIZE);
            listContainer.maxHeightProperty().bind(boardHBox.heightProperty());


            HBox headerSection = new HBox();
            headerSection.getStyleClass().add("header_section");

            Label headerTitle = new Label("List " + i);
            headerTitle.getStyleClass().add("header_title");

            MenuButton listOptionsBtn = new MenuButton();
            listOptionsBtn.getStyleClass().add("list_options");

            ImageView dotsImg = new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream("/Images/dots.png")))
            );
            listOptionsBtn.setGraphic(dotsImg);

            listOptionsBtn.getItems().addAll(
                    new MenuItem("Add Card"),
                    new MenuItem("Edit"),
                    new MenuItem("Color"),
                    new MenuItem("Archive"),
                    new MenuItem("Hide For Now"),
                    new MenuItem("Delete")
            );

            headerSection.getChildren().addAll(headerTitle, listOptionsBtn);

            ScrollPane listScrollPane = new ScrollPane();
            listScrollPane.getStyleClass().add("list_scrollP");
            listScrollPane.setFitToWidth(true);
            listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


            VBox visibleList = new VBox();
            visibleList.getStyleClass().add("visible_list");

            // Demo cards
            for (int j = 0; j < 1; j++) {
                setCards(visibleList);
            }

            listScrollPane.setContent(visibleList);

            // ================= FIXED FOOTER =================
            HBox addCardSection = new HBox();
            addCardSection.getStyleClass().add("addCardSection");
            addCardSection.setAlignment(Pos.CENTER);

            Button addCardBtn = new Button("Add card");
            addCardBtn.getStyleClass().add("add_button");
            addCardSection.getChildren().add(addCardBtn);
            addCardBtn.setPrefWidth(275);
            addCardBtn.setOnAction(e-> {});

            // ================= ASSEMBLY =================
            listContainer.getChildren().addAll(
                    headerSection,
                    listScrollPane,
                    addCardSection
            );

            makeListDraggable(listContainer, headerSection, listScrollPane, addCardSection);
            boardHBox.getChildren().add(listContainer);
        }
    }

    private void setCards(VBox visibleList) {

        StackPane card = new StackPane();
        card.getStyleClass().add("card");
        card.setPadding(new Insets(6));
        VBox.setVgrow(card, Priority.NEVER);

        TextArea text = new TextArea("New Card");
        text.getStyleClass().add("card_textA");
        text.setWrapText(true);
        text.setPrefRowCount(1);
        text.setMinHeight(Region.USE_PREF_SIZE);
        text.setPrefHeight(Region.USE_COMPUTED_SIZE);
        text.setMaxHeight(Double.MAX_VALUE);
        text.setEditable(false);
        text.setMouseTransparent(true);

        text.textProperty().addListener((obs, oldText, newText) -> {
            // Ensure card resizes when text changes
            text.setPrefHeight(Region.USE_COMPUTED_SIZE);
            text.layout();
            card.requestLayout();
        });

        MenuButton cardOptions = new MenuButton();
        cardOptions.getStyleClass().add("card_options");
        StackPane.setAlignment(cardOptions, Pos.TOP_RIGHT);

        card.getChildren().addAll(text, cardOptions);
        visibleList.getChildren().add(card);

        //Needs work
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !text.isEditable()) {
                text.setEditable(true);
                text.setMouseTransparent(false);
                text.requestFocus();
                Platform.runLater(() -> text.positionCaret(text.getText().length()));
            }
        });

        text.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                text.setEditable(false);
                text.setMouseTransparent(true);
                card.requestFocus();
            }
        });

        text.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.ENTER && !ke.isControlDown()) {
                card.requestFocus();
                ke.consume();
            }
        });

        // Variables for drag-and-drop
        final StackPane[] ghostCard = {null};
        final Rectangle[] placeholder = {null};
        final Group[] overlayRef = {null};

        card.setOnMousePressed(e -> {
            if (text.isEditable() || e.getTarget() instanceof MenuButton) return;

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            ImageView ghostImg = new ImageView(card.snapshot(params, null));
            ghostImg.setOpacity(0.36);

            ghostCard[0] = new StackPane(ghostImg);
            ghostCard[0].setManaged(false);
            ghostCard[0].setMouseTransparent(true);
            ghostCard[0].setPickOnBounds(false);

            Parent root = boardHBox.getScene().getRoot();

            // Create or find overlay for dragging.
            // Ensures that the ghost image can move freely without being affected by layout properties
            if (overlayRef[0] == null) {
                if (root instanceof Pane pane) {
                    for (Node n : pane.getChildren()) {
                        if (n instanceof Group g && g.getProperties().containsKey("kanbanOverlay")) {
                            overlayRef[0] = g; //Reusing group
                            break;
                        }
                    }
                    if (overlayRef[0] == null) {
                        overlayRef[0] = new Group();
                        overlayRef[0].getProperties().put("kanbanOverlay", true);
                        pane.getChildren().add(overlayRef[0]); //Creating group for future use

                    }
                } else if (root instanceof Group g) {
                    overlayRef[0] = g;
                }
            }

            // Position ghost card exactly under cursor
            Point2D start = overlayRef[0].sceneToLocal(e.getSceneX(), e.getSceneY());
            ghostCard[0].relocate(start.getX(), start.getY());
            overlayRef[0].getChildren().add(ghostCard[0]);

            // Placeholder shows original position
            placeholder[0] = new Rectangle(card.getWidth()-0.25, card.getHeight()-0.25);
            placeholder[0].setFill(Color.rgb(0, 0, 0, 0.18));
            placeholder[0].setArcWidth(10);
            placeholder[0].setArcHeight(10);

            VBox parent = (VBox) card.getParent();
            int index = parent.getChildren().indexOf(card);
            parent.getChildren().add(index, placeholder[0]);
            parent.getChildren().remove(card);
        });

        card.setOnMouseDragged(e -> {
            if (ghostCard[0] == null || overlayRef[0] == null) return;

            Point2D dragPoint = overlayRef[0].sceneToLocal(e.getSceneX(), e.getSceneY());
            ghostCard[0].relocate(dragPoint.getX(), dragPoint.getY());

            VBox targetList = null;
            for (Node listContainer : boardHBox.getChildren()) {
                if (listContainer instanceof VBox outer && outer.getChildren().size() > 1) {
                    if (outer.getChildren().get(1) instanceof ScrollPane sp
                            && sp.getContent() instanceof VBox list) {

                        Bounds lb = list.localToScene(list.getBoundsInLocal());
                        if (e.getSceneX() > lb.getMinX() && e.getSceneX() < lb.getMaxX()) {
                            targetList = list;
                            break;
                        }
                    }
                }
            }

            if (targetList == null) return;

            Bounds ghostBounds = ghostCard[0].localToScene(ghostCard[0].getBoundsInLocal());
            double centerY = ghostBounds.getCenterY();

            if (placeholder[0].getParent() != null) {
                ((VBox) placeholder[0].getParent()).getChildren().remove(placeholder[0]);
            }

            int insertIndex = 0;
            for (Node n : targetList.getChildren()) {
                Bounds nb = n.localToScene(n.getBoundsInLocal());
                if (centerY > nb.getMinY() + nb.getHeight() / 2) insertIndex++;
            }

            targetList.getChildren().add(insertIndex, placeholder[0]);
        });

        card.setOnMouseReleased(e -> {
            if (ghostCard[0] == null) return;

            // Remove ghost card
            if (overlayRef[0] != null) {
                overlayRef[0].getChildren().remove(ghostCard[0]);
                if (overlayRef[0].getChildren().isEmpty() && overlayRef[0].getParent() instanceof Pane pane) {
                    pane.getChildren().remove(overlayRef[0]);
                    overlayRef[0] = null;
                }
            }

            // Move card to placeholder's position
            if (placeholder[0] == null || placeholder[0].getParent() == null) {
                visibleList.getChildren().add(card);
            } else {
                VBox targetList = (VBox) placeholder[0].getParent();
                int index = targetList.getChildren().indexOf(placeholder[0]);
                targetList.getChildren().remove(placeholder[0]);
                targetList.getChildren().add(index, card);
            }

            text.setEditable(false);
            text.setMouseTransparent(true);
            card.requestFocus();

            ghostCard[0] = null;
            placeholder[0] = null;
        });
    }

    private void makeListDraggable(VBox listContainer, HBox headerSection, ScrollPane listScrollPane, HBox addCardSection) {

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
            // Noticable shifting of components happen if using orignal height and widths,
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

            //Ghost overlay
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

            // Clean up ghost
            Pane rootPane = (Pane) boardHBox.getScene().getRoot();
            rootPane.getChildren().remove(ghostList);

            ghostList = null;
            draggedList.setOpacity(1);
            draggedList = null;
            placeholder = null;
        });
    }

    private int getDropIndex(double sceneX) {
        int index = 0;

        for (Node n : boardHBox.getChildren()) {

            // Skip the Rectangle placeholder
            if (n == placeholder)
                continue;

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