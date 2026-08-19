package user;

import java.util.List;

public class UserService {

    private final UserRepo userRepo;

    public UserService() {
        userRepo = new UserRepo();
    }

    public void saveUser(User user) {
        userRepo.saveUser(user);
    }

    public List<User> getAllUsers() {
        return userRepo.getAllUsers();
    }

    public User getUserById(Long id) {
        return userRepo.getUserById(id);
    }

    public boolean isUserExisting(String username, String email) {
        return userRepo.isUserExisting(username, email);
    }

    public User findByUsernameOrEmail(String username, String email) {
        return userRepo.findByUsernameOrEmail(username, email);
    }
}
