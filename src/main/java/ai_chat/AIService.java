package ai_chat;


import java.util.List;

public class AIService {

    private final AIRepo aiRepo;

    public AIService() {
        this.aiRepo = new AIRepo();
    }

    public List<AI> getAllResponses() {
        return aiRepo.getAllResponses();
    }

    public AI getResponseById(long id) {
        return aiRepo.getResponseById(id);
    }

    public List<AI> findByUserId(long userId) {
        return aiRepo.findByUserId(userId);
    }

    public AI createResponse(AI ai) {
        return aiRepo.createResponse(ai);
    }

    public boolean updateResponse(AI ai) {
        return aiRepo.updateResponse(ai);
    }

    public boolean deleteResponse(long id) {
        return aiRepo.deleteResponse(id);
    }
}
