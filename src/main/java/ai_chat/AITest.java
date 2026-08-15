package ai_chat;

import org.junit.*;
import org.testng.Assert;

import java.time.LocalDateTime;
import java.util.List;

public class AITest {

    private final AIService aiService = new AIService();

    @Test
    public void createResponse() {
        AI chat = new AI();

        chat.setUserId(2L);
        chat.setPrompt("What is Java?");
        chat.setResponse("Java is a programming language.");
        chat.setTimestamp(LocalDateTime.now());

        Assert.assertNotNull(aiService.createResponse(chat).getResponseId());
        System.out.println(chat);

    }

    @Test
    public void findResponsesByUserId() {
        AI ai = new AI();

        ai.setUserId(2L);
        ai.setPrompt("What is SQLite?");
        ai.setResponse("SQLite is a lightweight database.");
        ai.setTimestamp(LocalDateTime.now());

        aiService.createResponse(ai);
        List<AI> responses = aiService.findByUserId(2L);
        System.out.println(responses);
        Assert.assertNotNull(responses);

    }
}