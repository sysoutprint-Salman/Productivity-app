package ai_chat;

import JavaFX.AbstractFX;
import JavaFX.Enums;
import JavaFX.NavigationFX;
import javafx.fxml.FXML;
import notebook.NotebookFX;
import JavaFX.SwitchScenes;
import user.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import to_do.ToDoFX;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AI_AssistantFX extends AbstractFX {
    public VBox chatBoxVbox;
    public Button sendButton;
    public TextField userTextField;
    private final DateTimeFormatter dateAndTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a");
    protected final SwitchScenes handler = new SwitchScenes();
    public ScrollPane messageScrollPane;
    public MenuItem mainTasks;
    public MenuItem viewNotebook;
    public ImageView uploadIcon;
    private ToDoFX toDoFX;
    private NotebookFX notebooks;
    public Label emptyLogsMessage;
    private final AIService aiService = new AIService();



    public AI_AssistantFX(){}

    public void initialize(){
        userTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
        sendButton.setDefaultButton(true);

        userTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                uploadIcon.setStyle("-fx-opacity: 1;");
            } else {
                uploadIcon.setStyle("-fx-opacity: 0.2;");
            }
        });

        GETChatlogs();
    }

    public void streamGPT(String prompt, Consumer<String> onToken, Runnable onComplete, Consumer<Exception> onError) {

        String gptKey = System.getenv("gptKey");

        String reqJson = String.format("""
                {
                    "model": "gpt-4o-mini",
                    "stream": true,
                    "messages": [
                        {"role":"user","content":"%s"}
                    ]
                }
                """, prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptKey)
                .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<InputStream> response =
                        client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));

                String line;
                while ((line = reader.readLine()) != null) {

                    if (!line.startsWith("data:")) continue;

                    String json = line.substring(5).trim();

                    if (json.equals("[DONE]")) {
                        onComplete.run();
                        break;
                    }

                    JSONObject obj = new JSONObject(json);
                    JSONObject delta = obj.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("delta");

                    if (delta.has("content")) {
                        onToken.accept(delta.getString("content"));
                    }
                }

            } catch (Exception ex) {
                onError.accept(ex);
            }
        });
    }

    public void onSendMessage() {
        userTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !userTextField.getText().isEmpty()) {
                sendButton.fire();
            }
        });

        String prompt = userTextField.getText();
        if (prompt.isEmpty()) return;

        uploadIcon.getStyleClass().add("upload_icon");
        chatBoxVbox.getChildren().remove(emptyLogsMessage);

        Label userLabel = new Label(prompt);
        userLabel.getStyleClass().add("prompt");
        userLabel.setWrapText(true);
        chatBoxVbox.getChildren().add(userLabel);
        userTextField.clear();

        chatBoxVbox.heightProperty().addListener((obs, o, n) -> {
            messageScrollPane.setVvalue(1.0);
        });
        Label gptLabel = new Label(LocalDateTime.now().format(dateAndTimeFormatter) + "\n");
        gptLabel.getStyleClass().add("response");
        gptLabel.setWrapText(true);
        gptLabel.setPadding(new Insets(10));

        HBox responseContainer = new HBox(gptLabel);
        responseContainer.setAlignment(Pos.CENTER_LEFT);
        chatBoxVbox.getChildren().add(responseContainer);

        StringBuilder fullResponse = new StringBuilder();

        streamGPT(
                prompt,
                token -> {
                    fullResponse.append(token);
                    Platform.runLater(() ->
                            gptLabel.setText(LocalDateTime.now().format(dateAndTimeFormatter) + "\n" + fullResponse)
                    );
                },

                () -> {
                    try {
                        AI newChat = new AI();
                        newChat.setResponse(fullResponse.toString());
                        newChat.setTimestamp(LocalDateTime.now());
                        newChat.setPrompt(prompt);
                        newChat.setUserId(User.getUserId());
                        aiService.createResponse(newChat);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                },
                error -> Platform.runLater(() -> {
                    gptLabel.setText("Error");
                    error.printStackTrace();
                })
        );
    }

    public void GETChatlogs(){
        List<AI> chatLogs =aiService.findByUserId(User.getUserId());

        chatBoxVbox.getChildren().removeAll();
        if (chatLogs.isEmpty()){
            emptyLogsMessage = new Label("This is your personal AI assistant, ask it whatever you need!");
            emptyLogsMessage.getStyleClass().add("emptyLabel");
            emptyLogsMessage.setPrefSize(550,450);
            emptyLogsMessage.setAlignment(Pos.CENTER);
            chatBoxVbox.getChildren().add(emptyLogsMessage);
        }
        else {
            chatLogs.forEach(chat ->{
            String prompt = chat.getPrompt();
            Label promptLabel = new Label(prompt);
            promptLabel.setWrapText(true);
            promptLabel.wrapTextProperty();
            promptLabel.getStyleClass().add("prompt");
            chatBoxVbox.setAlignment(Pos.CENTER_RIGHT);
            chatBoxVbox.getChildren().add(promptLabel);

            String response = chat.getResponse();
            Label responseLabel = new Label(chat.getTimestamp().format(dateAndTimeFormatter) + "\n" + response + " - AI Assistant");
            responseLabel.setWrapText(true);
            responseLabel.wrapTextProperty();
            responseLabel.setPadding(new Insets(10));
            responseLabel.getStyleClass().add("response");
            HBox responseContainer = new HBox(responseLabel);
            responseContainer.setAlignment(Pos.CENTER_LEFT);
            chatBoxVbox.getChildren().add(responseContainer);

            chatBoxVbox.heightProperty().addListener((obs, oldVal, newVal) -> {
                messageScrollPane.setVvalue(1.0);
            });
        });}
    }

    public void switchToTasks(ActionEvent event) {
        handler.switchScene(event, "tasks", consumer->{
            toDoFX = (ToDoFX) consumer;
            toDoFX.getByPosted();
        });
    }

    public void switchToNotebook(ActionEvent event) {
        handler.switchScene(event, "notebook", consumer->{
            notebooks = (NotebookFX) consumer;
            notebooks.GETNotebooks();
        });
    }

    @Override
    public void highlightNav() {
        navController.getNavigation().selectToggle(null); // deselect everything
        navController.getAiChatButton().setSelected(true);
    }
}
