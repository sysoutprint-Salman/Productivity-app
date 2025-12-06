package SpringBoot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*@Entity //I.E table in a database
@Table(name = "tasks")*/
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
/*    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)*/

    private Long id;
    private Long userId;
    private String title;
    private LocalDate date;
    private String description;
    //@Enumerated(EnumType.STRING) //Stores status's as strings in db
    private Status status;
    private LocalDateTime creationDate;
    //Add color column which holds hexadecimal color code
    public enum Status {POSTED,DELETED,COMPLETED }
}
