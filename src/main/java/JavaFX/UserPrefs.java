package JavaFX;

import lombok.Data;
import to_do.ToDoFX;
import user.User;
import user.UserService;

import java.util.prefs.Preferences;

@Data
public class UserPrefs {
    private static final Preferences userPreferences =
            Preferences.userNodeForPackage(UserPrefs.class);

    private final UserService userService = new UserService();
    private String username;
    private String email;
    private final User user = new User();
    private User savedUser;

    public UserPrefs() {}

    public void saveUser() {
        user.setUsername(username);
        user.setEmail(email);

        userService.saveUser(user);

        userPreferences.put("username", username);
        userPreferences.put("email", email);

        savedUser = getSavedUser();
    }

    public User getSavedUser() {
        String storedUsername = getStoredUsername();
        String storedEmail = getStoredEmail();

        return userService.findByUsernameOrEmail(
                storedUsername,
                storedEmail
        );
    }

    public static Long getSavedUserId() {
        String storedUsername = getStoredUsernameS();
        String storedEmail = getStoredEmailS();

        UserService userService = new UserService();
        User user = userService.findByUsernameOrEmail(
                storedUsername,
                storedEmail
        );

        return user == null ? null : user.getUserId();
    }

    public void saveToPref(String credential) {
        if (credential.matches(".*@(gmail|hotmail|outlook)\\.com$"))
            userPreferences.put("email", credential);
        else
            userPreferences.put("username", credential);
    }

    public void saveSortOption(ToDoFX.Sort sortOption) {
        userPreferences.put("sortOption", sortOption.toString());
    }

    public String getSortOption() {
        return userPreferences.get("sortOption", null);
    }

    public String getStoredUsername() {
        return userPreferences.get("username", null);
    }

    public String getStoredEmail() {
        return userPreferences.get("email", null);
    }

    public static String getStoredUsernameS() {
        return userPreferences.get("username", null);
    }

    public static String getStoredEmailS() {
        return userPreferences.get("email", null);
    }
}