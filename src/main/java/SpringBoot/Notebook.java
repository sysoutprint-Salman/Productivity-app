package SpringBoot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson Annotation
//Ignores properties that don't match fields in Java
public class Notebook {
    private Long id;
    private Long userId;
    private String tabTitle;
    private String hexColor;
    private String notebookText;
}
