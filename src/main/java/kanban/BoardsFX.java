package kanban;

import JavaFX.HTTPHandler;
import JavaFX.SwitchScenes;
import SpringBoot.User;
import ai_chat.AI_AssistantFX;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import notebook.NotebookFX;
import to_do.ToDoFX;

import java.time.LocalDateTime;
import java.util.*;

public class BoardsFX {
    @FXML private Button createBoardButton;
    @FXML private FlowPane boardGrid;

    private SwitchScenes handler = new SwitchScenes();
    private Stage stage;
    private Scene scene;
    private String json;
    private Board currentBoard;

    @FXML
    private void initialize(){}
    @FXML
    private void createBoardPopup(ActionEvent event) {
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
            createButton.setOnAction(r->{
                List<Map<String, Object>> lists = new ArrayList<>();

                if (!boardTitle.getText().isEmpty()) {
                    String title = boardTitle.getText();

                    /*for (Node node : listEntriesBox.getChildren()) {
                        HBox entry = (HBox) node;
                        Label titleLabel = (Label) entry.getChildren().get(0);
                        Spinner<Integer> spinner = (Spinner<Integer>) entry.getChildren().get(3);
                        Map<String, Object> list = new HashMap<>();
                        list.put("title", titleLabel.getText());
                        list.put("cardCount", spinner.getValue());
                        lists.add(list);
                    }*/

                    currentBoard = new Board(null, User.getUserId(), title, LocalDateTime.now());
                    currentBoard.setBoardId(HTTPHandler.POST("boards", currentBoard, Long.class));
                    addBoardCards(currentBoard.getBoardTitle());

                    stage.close();
                }
            });
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

    private void addBoardCards(String boardTitle){
            VBox card = new VBox();
            card.setPrefSize(250,180);
            card.getStyleClass().add("board_card");

            StackPane preview = new StackPane();
            preview.setPrefHeight(130);
            preview.getStyleClass().add("board_preview");

            HBox footer = new HBox();
            footer.getStyleClass().add("board_footer");

            VBox info = new VBox();
            info.getStyleClass().add("board_info");
            HBox.setHgrow(info, Priority.ALWAYS);

            Label title = new Label(boardTitle);
            title.getStyleClass().add("board_title");

            Label modified = new Label(
                    "Last Modified: Yesterday" /*+ board.getLastModified()*/
            );
            modified.getStyleClass().add("board_last_modified");

            info.getChildren().addAll(title, modified);

            MenuButton menu = new MenuButton();
            menu.getStyleClass().add("board_menu");
            MenuItem rename = new MenuItem("Rename");
            MenuItem duplicate = new MenuItem("Duplicate");
            MenuItem archive = new MenuItem("Archive");
            MenuItem delete = new MenuItem("Delete");

            menu.getItems().addAll(
                    rename,
                    duplicate,
                    archive,
                    delete
            );

            footer.getChildren().addAll(info, menu);

            card.getChildren().addAll(preview, footer);

            boardGrid.getChildren().add(card);

    }

    private void addOrLoadBoardCards(String boardTitle){
        List<Board> boardDate;
        if (boardTitle == null){
            boardDate = HTTPHandler.GET("user/" + User.getUserId(),Board.class);
            System.out.println(boardDate.toString());
        }
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
