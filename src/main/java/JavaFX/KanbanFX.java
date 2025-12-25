package JavaFX;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.animation.*;

import java.util.Objects;

public class KanbanFX {
    @FXML
    private HBox boardHBox;


    @FXML private StackPane card;
    @FXML private TextArea cardTextA;
    @FXML private MenuButton cardOptions;

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

    private double dragOffsetX;
    private boolean dragging = false;
    private boolean sidebarOpen = false;
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

    }

    public void slidingSidebar() {
        if (sidebarOpen) {
            collapseSidebar();
        } else {
            expandSidebar();
        }
    }

    private void expandSidebar() {
        sidebar.toFront();

        FadeTransition fade = new FadeTransition(Duration.millis(200));
        fade.setFromValue(0);
        fade.setToValue(1);

        double startWidth = sidebar.getWidth();
        double endWidth = 275;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebar.prefWidthProperty(), startWidth)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(sidebar.prefWidthProperty(), endWidth, Interpolator.EASE_BOTH))
        );
        fade.play();
        timeline.play();

        sidebarOpen = true;
    }

    private void collapseSidebar() {

        FadeTransition fade = new FadeTransition(Duration.millis(200));
        fade.setFromValue(1);
        fade.setToValue(0);

        double startWidth = sidebar.getWidth();
        double endWidth = 80;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebar.prefWidthProperty(), startWidth)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(sidebar.prefWidthProperty(), endWidth, Interpolator.EASE_BOTH))
        );
        fade.play();
        timeline.play();
        sidebarOpen = false;
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
            addCardBtn.getStyleClass().add("add_card_button");
            addCardSection.getChildren().add(addCardBtn);

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