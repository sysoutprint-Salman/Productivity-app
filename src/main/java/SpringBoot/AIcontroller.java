package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/gptresponses")
@RequiredArgsConstructor
public class AIcontroller {
    private final AIrepository aIrepository;
    @GetMapping
    protected List<AI> getAllGPTresponses() {
        return aIrepository.getAllGPTresponses();
    }

    @PostMapping
    protected void createResponse(@RequestBody AI response) {
        System.out.println(response);
        aIrepository.createResponse(response);
    }
    @GetMapping("filter")
    protected List<AI> findByUserId(@RequestParam Long userId){
        return aIrepository.findByUserId(userId);
    }
}

@Repository
class AIrepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final AIRowMapper aiRowMapper = new AIRowMapper();

    protected List<AI> getAllGPTresponses (){
        return jdbc.query("SELECT * from gpt_responses",aiRowMapper);
    }
    protected void createResponse(AI ai){
        jdbc.update("INSERT INTO gpt_responses (response, timestamp, prompt, user_id) VALUES (?, ?, ?, ?)",
                ai.getResponse(),
                ai.getTimestamp(),
                ai.getPrompt(),
                ai.getUserId());
    }
    protected List<AI> findByUserId(Long user_id){
        return jdbc.query("SELECT * FROM gpt_responses WHERE user_id = ?",
                aiRowMapper, user_id);
    }
}


class AIRowMapper implements RowMapper<AI>{
    @Override
    public AI mapRow(ResultSet rs, int rowNum) throws SQLException {
        AI ai = new AI();
        ai.setId(rs.getLong("id"));
        ai.setUserId(rs.getLong("user_id"));
        ai.setPrompt(rs.getString("prompt"));
        ai.setResponse(rs.getString("response"));
        ai.setTimestamp(rs.getObject("timestamp", LocalDateTime.class));
        return ai;
    }
}