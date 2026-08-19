package user;

import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    private final UserMapper mapper = new UserMapper();

    public void saveUser(User user) {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next())
                users.add(mapper.mapRow(rs, rs.getRow()));

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get users", e);
        }
    }

    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapper.mapRow(rs, rs.getRow());
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public boolean isUserExisting(String username, String email) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE username = ? OR email = ?
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user existence", e);
        }
    }

    public User findByUsernameOrEmail(String username, String email) {
        String sql = """
                SELECT *
                FROM users
                WHERE username = ? OR email = ?
                LIMIT 1
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapper.mapRow(rs, rs.getRow());
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find user by username or email", e
            );
        }
    }
}
class UserMapper {

    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}
