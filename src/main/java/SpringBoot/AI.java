package SpringBoot;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AI {

    private Long id;
    //@JsonAlias({"userId", "user_id"}) //
    private Long userId;
    private String prompt;
    //@Column(columnDefinition = "TEXT")
    private String response;
    private LocalDateTime timestamp;
}
