package ai_chat;


import database.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AIRepo {

    private final AIRowMapper aiRowMapper = new AIRowMapper();

    public List<AI> getAllResponses() {
        String sql = """
                SELECT id, user_id, prompt, response, timestamp
                FROM gpt_responses
                ORDER BY id
                """;

        List<AI> responses = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                responses.add(aiRowMapper.mapRow(rs));
            }

            return responses;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve GPT responses", e);
        }
    }

    public AI getResponseById(long id) {
        String sql = """
                SELECT id, user_id, prompt, response, timestamp
                FROM gpt_responses
                WHERE id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return aiRowMapper.mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve GPT response", e);
        }
    }

    public List<AI> findByUserId(long userId) {
        String sql = """
                SELECT response_id, user_id, prompt, response, timestamp
                FROM gpt_responses
                WHERE user_id = ?
                ORDER BY response_id
                """;

        List<AI> responses = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    responses.add(aiRowMapper.mapRow(rs));
                }
            }

            return responses;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve GPT responses for user", e);
        }
    }

    public AI createResponse(AI ai) {
        String sql = """
                INSERT INTO gpt_responses
                    (response, timestamp, prompt, user_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ai.getResponse());

            if (ai.getTimestamp() == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, ai.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            stmt.setString(3, ai.getPrompt());
            stmt.setLong(4, ai.getUserId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    ai.setResponseId(keys.getLong(1));
                }
            }

            return ai;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create GPT response", e);
        }
    }

    public boolean updateResponse(AI ai) {
        String sql = """
                UPDATE gpt_responses
                SET response = ?,
                    timestamp = ?,
                    prompt = ?,
                    user_id = ?
                WHERE response_id = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ai.getResponse());

            if (ai.getTimestamp() == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, ai.getTimestamp().toString());
            }

            stmt.setString(3, ai.getPrompt());
            stmt.setLong(4, ai.getUserId());
            stmt.setLong(5, ai.getResponseId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update GPT response", e);
        }
    }

    public boolean deleteResponse(long id) {
        String sql = "DELETE FROM gpt_responses WHERE id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete GPT response", e);
        }
    }
}




class AIRowMapper {

    public AI mapRow(ResultSet rs) throws SQLException {
        AI ai = new AI();

        ai.setResponseId(rs.getLong("response_id"));
        ai.setUserId(rs.getLong("user_id"));
        ai.setPrompt(rs.getString("prompt"));
        ai.setResponse(rs.getString("response"));

        String timestamp = rs.getString("timestamp");

        if (timestamp != null) {
            ai.setTimestamp(LocalDateTime.parse(timestamp));
        } else {
            ai.setTimestamp(null);
        }

        return ai;
    }
}