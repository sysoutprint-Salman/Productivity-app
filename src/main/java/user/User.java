package user;


import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Getter
    @Setter
    //private static Long globalId;
    private static Long userId;
    private String username;
    private String email;
}
