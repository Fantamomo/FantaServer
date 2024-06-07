package at.leisner.server.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FantaPermissionManager implements PermissionManager, Serializable {
    private Map<String, FantaUser> users;

    public FantaPermissionManager() {
        this.users = new HashMap<>();
    }
    @Override
    public void addUser(String username, String password) {
        users.put(username, new FantaUser(username, password));
    }
    @Override
    public void assignPermissionToUser(String username, String permissionName, boolean value) {
        User user = users.get(username);
        if (user != null) {
            user.addPermission(new FantaPermission(permissionName, value));
        }
    }
    @Override

    public void removePermissionFromUser(String username, String permissionName) {
        User user = users.get(username);
        if (user != null) {
            user.removePermission(permissionName);
        }
    }
    @Override

    public boolean userHasPermission(String username, String permissionName) {
        User user = users.get(username);
        return user != null && user.hasPermission(permissionName);
    }
    public boolean authenticate(String username, String password) {
        return users.get(username).authenticate(password);
    }
    public User getUser(String username) {
        return users.get(username);
    }
}
