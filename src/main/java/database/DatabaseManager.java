package database;

import java.nio.file.Files;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database.db";

    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }
}
