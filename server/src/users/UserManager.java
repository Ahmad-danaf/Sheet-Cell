package users;

import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private final Set<String> usersSet;

    public UserManager() {
        usersSet = new HashSet<>();
    }

    public synchronized void addUser(String username) {
        usersSet.add(username);
    }

    public synchronized void removeUser(String username) {
        usersSet.remove(username);
    }

    public boolean isUserExists(String username) {
        return usersSet.contains(username);
    }

    public synchronized Set<String> getUsers() {
        return new HashSet<>(usersSet);
    }
}
