package JavaFX;

import SpringBoot.AI;
import SpringBoot.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AI_AssistantFX {
    public MenuItem gptMenuItem;
    public VBox chatBoxVbox;
    public Button sendButton;
    public TextField userTextField;
    private final DateTimeFormatter dateAndTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a");
    protected final SwitchScenes handler = new SwitchScenes();
    private final LocalDateTime timestampNow = LocalDateTime.now();
    public ScrollPane messageScrollPane;
    private final HTTPHandler httpHandler = new HTTPHandler();
    private final ObjectMapper mapper = new ObjectMapper();
    public MenuItem mainTasks;
    public MenuItem viewNotebook;
    private TaskFX taskFX;
    private NotebookFX notebooks;
    private UserPrefs userPrefs = new UserPrefs();
    private User user = userPrefs.getSavedUser();
    public VBox placeholderVbox;
    public Label emptyLogsMessage;
    public AI_AssistantFX(){}

    public void initialize(){
        userTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
        sendButton.setDefaultButton(true);
    }

    public String speakToGPT(@NotNull String userPrompt) {
        String gptKey = System.getenv("gptKey");
        String content = String.format(
                "{\"role\":\"assistant\",\"content\":\"%s\"}",
                userPrompt.concat(" Make your response a maximum word count of 125. " +
                        "Structure long responses into paragraphs.")
        );
        String body = String.format("""
                 {
                 "model": "gpt-4o-mini",
                  "messages": [%s]
                }""", content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        //HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body());
            String json = response.body();
            JSONObject gptJson = new JSONObject(json);
            JSONObject innerJson = gptJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
            return innerJson.getString("content");

        } catch (InterruptedException | IOException ex) {
            System.err.println("Request was interrupted: " + ex.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupted status
        }
        return "Error";
    }
    public void onEnterPressed(){
        userTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !userTextField.getText().isEmpty()) {
                sendButton.fire();
            }
            sendButton.setDefaultButton(true);
        });
    }
    public void onSendMessage() {
        userTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !userTextField.getText().isEmpty()) {
                sendButton.fire();
            }
        });

        String prompt = userTextField.getText();
        if (!prompt.isEmpty()) {
            chatBoxVbox.getChildren().remove(emptyLogsMessage);
            Label userPrompt = new Label(prompt);
            userTextField.clear();
            userPrompt.setWrapText(true);
            userPrompt.wrapTextProperty();
            userPrompt.getStyleClass().add("prompt");
            chatBoxVbox.setAlignment(Pos.CENTER_RIGHT);
            chatBoxVbox.getChildren().add(userPrompt);

            //Automatically scrolls to the bottom on each response.
            chatBoxVbox.heightProperty().addListener((obs, oldVal, newVal) -> {
                messageScrollPane.setVvalue(1.0);
            });

            //Prompting will be done in a background thread thanks to javafx.concurrent
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return speakToGPT(prompt);
                }
            };
            task.setOnSucceeded(e -> {
                String contentString = task.getValue();
                Label gptResponseLabel = new Label(timestampNow.format(dateAndTimeFormatter) + "\n" + contentString + " - AI Assistant");

                gptResponseLabel.setWrapText(true);
                gptResponseLabel.wrapTextProperty();
                gptResponseLabel.setPadding(new Insets(10));
                gptResponseLabel.getStyleClass().add("response");
                HBox responseContainer = new HBox(gptResponseLabel);
                responseContainer.setAlignment(Pos.CENTER_LEFT);
                chatBoxVbox.getChildren().add(responseContainer);

                try {
                    Map <String, Object> jsonPayload = new HashMap<>();
                    jsonPayload.put("prompt",prompt);
                    jsonPayload.put("response",contentString);
                    jsonPayload.put("timestamp",timestampNow.toString());
                    jsonPayload.put("userId",user.getUserId());
                    String refinedJson = mapper.writeValueAsString(jsonPayload);
                    httpHandler.POST("gptresponses",refinedJson);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
            });
            task.setOnFailed(e -> {
                //No internet connection message goes here.
            });
            new Thread(task).start();
        }
    }
    public void GETChatlogs(){
        List<AI> chatLogs = httpHandler.GET("gptresponses/filter?userId=" + user.getUserId(), AI.class);
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
            taskFX = (TaskFX) consumer;
            taskFX.getByPosted();
        });
    }

    public void switchToNotebook(ActionEvent event) {
        handler.switchScene(event, "notebook", consumer->{
            notebooks = (NotebookFX) consumer;
            notebooks.GETNotebooks();
        });
    }
}
