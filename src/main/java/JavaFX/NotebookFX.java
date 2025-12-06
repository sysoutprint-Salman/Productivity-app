package JavaFX;

import SpringBoot.Notebook;
import SpringBoot.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;

public class NotebookFX{
    public VBox tabsVbox;
    public ScrollPane tabsScrollPane, notebookScrollPane;
    public TextArea notepadArea = new TextArea();
    public MenuBar tabsMenuBar;
    private Timer timer = new Timer();
    private boolean isTaskScheduled = false;
    private final int DELAY = 100;
    private final ObjectMapper mapper = new ObjectMapper();
    protected final SwitchScenes handler = new SwitchScenes();
    private AI_AssistantFX ai;
    private ToDoFX toDoFX;
    private UserPrefs userPrefs = new UserPrefs();
    private User user = userPrefs.getSavedUser();
    private Stage popupStage;
    private Scene scene;
    public MenuItem gptMenuItem;
    public MenuItem mainTasks;

    public NotebookFX(){}

    public void initialize(){
        //this.notebooks = AppState.getNotebooks();
    }

    public void createNewTab(){
        popupStage = new Stage();
        TextField newTabTitle = new TextField();
        ColorPicker colorPicker = new ColorPicker();
        Button createTabButton = new Button("Create Tab");
        Label noColorLabel = new Label("No Color");
        CheckBox noColorCB = new CheckBox();
        HBox colorHbox = new HBox(colorPicker, noColorLabel, noColorCB);

        popupStage.setTitle("Create Tab");
        newTabTitle.setPromptText("Tab title");
        newTabTitle.getStyleClass().add("text_field");
        colorPicker.getStyleClass().add("color-picker");
        createTabButton.getStyleClass().add("submit_button");
        noColorLabel.getStyleClass().add("no_colorLabel");
        noColorCB.getStyleClass().add("no_color_CB");
        colorHbox.getStyleClass().add("color_hbox");

        colorPicker.setDisable(noColorCB.isSelected());
        noColorCB.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            colorPicker.setDisable(isSelected);
        });

        createTabButton.setOnAction(e ->{
            String title = newTabTitle.getText();
            String hexColor = colorPicker.isDisabled() ? null : hexColorFormatter(colorPicker.getValue());
            if (!title.isEmpty()){
                try {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("tabTitle",title);
                    objectNode.put("userId", User.getUserId());
                    objectNode.put("hexColor", hexColor);
                    String notebookJson = mapper.writeValueAsString(objectNode);
                    HTTPHandler.POST("notebooks", notebookJson);
                    popupStage.close();
                    tabsVbox.getChildren().clear();
                    GETNotebooks();
                } catch (Exception ex) {
                    System.out.println("Notebook tab creation failed.");
                    ex.printStackTrace();
                }
            }
        });

        VBox newTabVbox = new VBox(10, newTabTitle, colorHbox, createTabButton);
        newTabVbox.setPadding(new Insets(20));

        scene = new Scene(newTabVbox, 300, 153);
        scene.getStylesheets().add("CSS/Notebook.css");
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public void editNewTab(String oldText, Long id, Notebook notebook, ToggleButton tabButton){
        popupStage = new Stage();
        TextField editTabTitle = new TextField();
        ColorPicker editColorPicker = new ColorPicker();
        Button editTabButton = new Button("Edit Tab");
        Label noColorLabel = new Label("No Color");
        CheckBox noColorCB = new CheckBox();
        HBox colorHbox = new HBox(editColorPicker, noColorLabel, noColorCB);

        popupStage.setTitle("Edit Tab");
        editTabTitle.setText(oldText);
        editTabTitle.setPromptText("Tab title");

        if (notebook.getHexColor() != null){
            editColorPicker.setValue(Color.web(notebook.getHexColor()));
        } else {
            editColorPicker.setDisable(true);
            noColorCB.setSelected(true);
        }
        editTabTitle.getStyleClass().add("text_field");
        editColorPicker.getStyleClass().add("color-picker");
        editTabButton.getStyleClass().add("submit_button");
        noColorLabel.getStyleClass().add("no_colorLabel");
        noColorCB.getStyleClass().add("no_color_CB");
        colorHbox.getStyleClass().add("color_hbox");

        editColorPicker.setDisable(noColorCB.isSelected());
        noColorCB.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            editColorPicker.setDisable(isSelected);
        });

        editTabButton.setOnAction(e ->{
            String title = editTabTitle.getText();
            String hexColor = editColorPicker.isDisabled() ? null : hexColorFormatter(editColorPicker.getValue());
            if (!title.isEmpty()){
                try {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("tabTitle", title);
                    objectNode.put("hexColor", hexColor);
                    String notebookJson = mapper.writeValueAsString(objectNode);
                    HTTPHandler.UPDATE(notebookJson, "notebooks/" + id + "/tab");
                    notebook.setTabTitle(title);
                    notebook.setHexColor(hexColor);
                    String cssStyle = hexColor != null ? hexColor + ";" : "transparent;";
                    tabButton.setStyle("-fx-border-color: transparent transparent #cfcfcf " + cssStyle);
                    tabButton.setText(notebook.getTabTitle());
                    popupStage.close();
                } catch (Exception ex) {
                    System.out.println("Notebook tab creation failed.");
                    ex.printStackTrace();
                }
            }
        });

        VBox editTabVbox = new VBox(10, editTabTitle, colorHbox, editTabButton);
        editTabVbox.setPadding(new Insets(20));

        scene = new Scene(editTabVbox, 300, 153);
        scene.getStylesheets().add("CSS/Notebook.css");
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public void GETNotebooks(){
        try {
            List<Notebook> notebooks = HTTPHandler.GET("notebooks/filter?userId=" + User.getUserId(),Notebook.class);
            notebookScrollPane.setContent(notepadArea);
            notepadArea.setVisible(false);
            notepadArea.setWrapText(true);
            notepadArea.setPromptText("Type anything you want.");
            notepadArea.getStyleClass().add("notepad");
            ToggleGroup tabsGroup = new ToggleGroup();

            if (notebooks.isEmpty()){
                Label userIndicator = new Label("Your notebook looks empty, try making some tabs by pressing the \"+\" button.");
                userIndicator.setWrapText(true);
                userIndicator.getStyleClass().add("emptyLabel");
                tabsVbox.getChildren().add(userIndicator);
            } else {
            notebooks.forEach((notebook ->{
                ToggleButton tabButton = new ToggleButton();
                tabButton.setText(notebook.getTabTitle());
                tabButton.setMaxWidth(Double.MAX_VALUE);
                tabButton.getStyleClass().add("tab");
                tabButton.setToggleGroup(tabsGroup);
                tabButton.setWrapText(true);
                tabButton.setStyle("-fx-border-color: transparent transparent #cfcfcf " +
                        (notebook.getHexColor() != null ? notebook.getHexColor() + ";" : "transparent;"));
                tabButton.setOnMouseClicked(e ->{
                    notepadArea.setText(notebook.getNotebookText());
                    notepadArea.setVisible(true);
                    autoUpdateNotebookText(notebook.getId(), notebook);
                    //tabButton.setSelected(true);
                });
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editTab = new MenuItem("Edit Tab");
                MenuItem deleteTab = new MenuItem("Delete Tab");
                contextMenu.getItems().addAll(editTab, deleteTab);
                editTab.setOnAction(event -> {
                    editNewTab(notebook.getTabTitle(), notebook.getId(), notebook, tabButton);});
                deleteTab.setOnAction(event -> {
                    HTTPHandler.DELETE(notebook.getId(), "notebooks", "false");
                    tabsVbox.getChildren().remove(tabButton);
                    notepadArea.setVisible(false);});
                tabButton.setContextMenu(contextMenu);
                tabsVbox.getChildren().add(tabButton);
            }));}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoUpdateNotebookText(Long notebookId, Notebook notebook) {
            Timeline debouncer = new Timeline(
                    new KeyFrame(Duration.millis(DELAY), e -> {
                        try {
                            String updatedText = notepadArea.getText();
                            Map<String, String> updateMap = new HashMap<>();
                            updateMap.put("notebookText", updatedText);
                            String updatedJson = mapper.writeValueAsString(updateMap);
                            HTTPHandler.UPDATE(updatedJson, "notebooks/" + notebookId + "/text");
                            //notepadArea.setVisible(true);
                            notebook.setNotebookText(updatedText);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
            );
            debouncer.setCycleCount(1); //Performs the above task once each time it fires
        notepadArea.setOnKeyTyped(e -> {
            debouncer.stop(); //Stops any previous running timelines
            debouncer.playFromStart(); //Restarts
        });
    }

    public MenuButton colorOptions(List<Color> colors, Consumer<Color> onColorSelected){
        //Will likely use in the future, but not important now.
        MenuButton button = new MenuButton();
        button.setPrefWidth(40);
        button.setPrefHeight(30);

        // default visual
        Rectangle preview = new Rectangle(20, 20, colors.get(0));
        preview.setStroke(Color.GRAY);
        button.setGraphic(preview);

        // create swatches
        for (Color color : colors) {
            CustomMenuItem item = new CustomMenuItem();

            Rectangle swatch = new Rectangle(25, 25, color);
            swatch.setStroke(Color.BLACK);

            item.setContent(swatch);
            item.setHideOnClick(true);

            item.setOnAction(e -> {
                preview.setFill(color);   // update selected color
                onColorSelected.accept(color);
            });

            button.getItems().add(item);
        }

        return button;
    }

    public String hexColorFormatter(Color color){
        return String.format("#%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
    }

    public void switchToTasks(ActionEvent event) {
        handler.switchScene(event, "tasks", consumer->{
            toDoFX = (ToDoFX) consumer;
            toDoFX.getByPosted();
        });
    }

    public void switchToGPT(ActionEvent event) {
        handler.switchScene(event, "AI", consumer->{
            ai = (AI_AssistantFX) consumer;
            ai.GETChatlogs();
        });
    }
}
