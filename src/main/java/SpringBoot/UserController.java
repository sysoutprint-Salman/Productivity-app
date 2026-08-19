package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    protected void saveUser(@RequestBody User user) {
        userRepository.saveUser(user);
    }

    @GetMapping
    protected List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @GetMapping("/{id}")
    protected User getUserById(@PathVariable Long id) {
        return userRepository.getUserById(id);
    }

    @GetMapping("/existing")
    protected boolean isUserExisting(@RequestParam String username, @RequestParam String email) {
        return userRepository.isUserExisting(username, email);
    }

    @GetMapping("/login")
    protected ResponseEntity<User> getUserByUsernameOrEmail(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        User user = userRepository.findByUsernameOrEmail(username, email);
        return user != null
                ? ResponseEntity.ok(user)
                : ResponseEntity.notFound().build();
    }
}
@Repository
@RequiredArgsConstructor
class UserRepository {

    private final JdbcTemplate jdbc;
    private final UserMapper mapper = new UserMapper();

    protected void saveUser(User user) {
        jdbc.update(
                "INSERT INTO users (username, email) VALUES (?, ?)",
                user.getUsername(),
                user.getEmail()
        );
    }

    protected List<User> getAllUsers() {
        return jdbc.query(
                "SELECT * FROM users ORDER BY user_id",
                mapper
        );
    }

    protected User getUserById(Long id) {
        return jdbc.queryForObject(
                "SELECT * FROM users WHERE user_id = ?",
                mapper,
                id
        );
    }

    protected boolean isUserExisting(String username, String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?",
                Integer.class,
                username,
                email
        );
        return count != null && count > 0;
    }

    protected User findByUsernameOrEmail(String username, String email) {
        List<User> users = jdbc.query(
                "SELECT * FROM users WHERE username = ? OR email = ? LIMIT 1",
                mapper,
                username,
                email
        );
        return users.isEmpty() ? null : users.get(0);
    }
}

class UserMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        return user;
    }


}
