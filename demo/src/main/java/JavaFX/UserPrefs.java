package JavaFX;

import SpringBoot.User;
import lombok.Data;

import java.awt.*;
import java.util.prefs.Preferences;

@Data
public class UserPrefs {
    private final Preferences userPreferences = Preferences.userNodeForPackage(UserPrefs.class);
    private final HTTPHandler httpHandler = new HTTPHandler();
    private String username;
    private String email;
    private final User user = new User();
    private User savedUser;


    public UserPrefs(){}

    public void saveUser(){
        this.user.setUsername(username);
        this.user.setEmail(email);
        String json = String.format( "{\"username\":\"%s\", \"email\":\"%s\"}", username, email);
        httpHandler.POST("users", json);
        userPreferences.put("username", username);
        userPreferences.put("email", email);
        this.savedUser = getSavedUser();
    }

    public User getSavedUser() {
        String storedUsername = getStoredUsername();
        String storedEmail = getStoredEmail();
        var users = httpHandler.GET("users/login?username=" + storedUsername + "&email=" + storedEmail, User.class);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    /*public void registerUser(){
        if (!username.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")){
            System.err.println("Username needs to be 6 characters long and contain atleast 1 number.");
        } else if (!email.matches("^(?=[A-Za-z0-9._%+-]*[A-Za-z])[A-Za-z0-9._%+-]+@(gmail|hotmail|outlook)\\.com$")) {
            System.err.println("A valid email is required.");
        } else {
            saveUser();
        }
    }*/


    public void saveToPref(String credential){
        if (credential.matches(".*@(gmail|hotmail|outlook)\\.com$")){
            userPreferences.put("email",credential);
        }else {
            userPreferences.put("username",credential);
        }
    }

    public void saveSortOption(TaskFX.Sort sortOption){
        userPreferences.put("sortOption",sortOption.toString());
    }
    public String getSortOption(){
        return userPreferences.get("sortOption",null);
    }
    public String getStoredUsername() {
        return userPreferences.get("username",null);
    }
    public String getStoredEmail() {
        return userPreferences.get("email",null);
    }

}
