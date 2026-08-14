package database;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    /* This class is responsible for initializing the database/schema when necessary, along with
    updates/migration logic in the future.
    * */

    public static void main(String[] args) {
        initialize();
    }

    public static void initialize() {
        try (Connection connection = DatabaseManager.connect();
             InputStream inputStream = DatabaseInitializer.class
                     .getResourceAsStream("/schema.sql")) {

            if (inputStream == null) {
                throw new RuntimeException("schema.sql could not be found.");
            }

            String schema = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            try (Statement statement = connection.createStatement()) {
                for (String sql : schema.split(";")) {
                    if (!sql.trim().isEmpty()) {
                        statement.execute(sql);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
