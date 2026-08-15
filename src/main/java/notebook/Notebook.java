package notebook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true) //Jackson Annotation
//Ignores properties that don't match fields in Java
public class Notebook {
    private Long notebookId;
    private Long userId;
    private String tabTitle;
    private String hexColor;
    private String notebookText;
}
