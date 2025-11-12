package JavaFX;

import SpringBoot.Notebook;
import SpringBoot.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class NotebookFX{
    public VBox tabsVbox;
    public ScrollPane tabsScrollPane, notebookScrollPane;
    public TextArea notepadArea = new TextArea();
    public MenuBar tabsMenuBar;
    private Timer timer = new Timer();
    private boolean isTaskScheduled = false;
    private final int DELAY = 100;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HTTPHandler httpHandler = new HTTPHandler();
    protected final SwitchScenes handler = new SwitchScenes();
    private AI_AssistantFX ai;
    private TaskFX taskFX;
    private UserPrefs userPrefs = new UserPrefs();
    private User user = userPrefs.getSavedUser();

    public MenuItem gptMenuItem;
    public MenuItem mainTasks;
    public Pane savedPane;

    public NotebookFX(){}

    public void createNewTab(){
        Stage newTabStage = new Stage();
        newTabStage.setTitle("Create Tab");
        TextField newTabTitle = new TextField();
        Button createTabButton = new Button("Create Tab");
        createTabButton.setOnAction(e ->{
            String title = newTabTitle.getText();
            String notebookJson = String.format(
                    "{\"tabTitle\":\"%s\", \"userId\":\"%s\"}", title, user.getUserId());
            httpHandler.POST("notebooks", notebookJson);
            newTabStage.close();
            tabsVbox.getChildren().clear();
            GETNotebooks();
        });

        VBox newTabVbox = new VBox(10, newTabTitle, createTabButton);
        newTabVbox.setPadding(new Insets(20));

        Scene newTabScene = new Scene(newTabVbox, 320, 150);
        newTabStage.setScene(newTabScene);
        newTabStage.show();
    }
    public void editNewTab(String oldText, Long id, Notebook notebook, ToggleButton tabButton){
        Stage editTabStage = new Stage();
        editTabStage.setTitle("Edit Tab");
        TextField editTabTitle = new TextField(); editTabTitle.setPromptText(oldText);
        Button editTabButton = new Button("Edit Tab");
        editTabButton.setOnAction(e ->{
            String title = editTabTitle.getText();
            String notebookJson = String.format(
                    "{\"tabTitle\":\"%s\"}", title);
            httpHandler.UPDATE(notebookJson, "notebooks/" + id + "/tab");
            notebook.setTabTitle(title);
            tabButton.setText(notebook.getTabTitle());
            /*tabsVbox.getChildren().clear();
            Platform.runLater(this::GETNotebooks);*/
            editTabStage.close();
        });
        VBox editTabVbox = new VBox(10, editTabTitle, editTabButton);
        editTabVbox.setPadding(new Insets(20));
        Scene editTabScene = new Scene(editTabVbox, 320, 150);
        editTabStage.setScene(editTabScene);
        editTabStage.show();

    }
    public void GETNotebooks(){
        try {
            List<Notebook> notebooks = httpHandler.GET("notebooks/filter?userId=" + user.getUserId(),Notebook.class);
            notebookScrollPane.setContent(notepadArea);
            notepadArea.setVisible(false);
            notepadArea.setWrapText(true);
            notepadArea.setPromptText("Type anything you want.");
            ToggleGroup tabsGroup = new ToggleGroup();

            notebooks.forEach((notebook ->{
                ToggleButton tabButton = new ToggleButton();
                tabButton.setText(notebook.getTabTitle());
                tabButton.setMaxWidth(Double.MAX_VALUE);
                tabButton.getStyleClass().add("tab");
                tabButton.setToggleGroup(tabsGroup);
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
                    httpHandler.DELETE(notebook.getId(), "notebooks", "false");
                    tabsVbox.getChildren().remove(tabButton);
                    notepadArea.setVisible(false);});
                tabButton.setContextMenu(contextMenu);
                tabsVbox.getChildren().add(tabButton);
            }));
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
                            httpHandler.UPDATE(updatedJson, "notebooks/" + notebookId + "/text");
                            savedPane.setVisible(true);
                            //notepadArea.setVisible(true);
                            notebook.setNotebookText(updatedText);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
            );
            debouncer.setCycleCount(1); //Performs the above task once each time it fires
        notepadArea.setOnKeyTyped(e -> {
            savedPane.setVisible(false);
            debouncer.stop(); //Stops any previous running timelines
            debouncer.playFromStart(); //Restarts
        });
    }

    public TitledPane coloredTabs(TitledPane tab, Label newTabTitle){
        ColorPicker colorPicker = new ColorPicker();

        Color color = colorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        String textColor = luminance < 0.5 ? "white" : "black";

        Label styledText = new Label(newTabTitle.getText());
        styledText.setStyle("-fx-text-fill: " + textColor + ";");

        tab.setGraphic(styledText);
        tab.setCollapsible(false);
        tab.setStyle("-fx-background-color: " + hex + ";");


        Platform.runLater(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            Node titleNode = tab.lookup(".title");
            if (titleNode != null) {
                titleNode.setStyle("-fx-background-color: " + hex + ";");
            } else {
                System.out.println("Title node not found.");
            }
        });
        return new TitledPane();
    } //TODO, work on having tabs be colored


    public void switchToTasks(ActionEvent event) {
        handler.switchScene(event, "tasks", consumer->{
            taskFX = (TaskFX) consumer;
            taskFX.getByPosted();
        });
    }
    public void switchToGPT(ActionEvent event) {
        handler.switchScene(event, "AI", consumer->{
            ai = (AI_AssistantFX) consumer;
            ai.GETChatlogs();
        });
    }
}
