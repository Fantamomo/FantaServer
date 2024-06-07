package at.leisner.server.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FantaUser implements User, Serializable {
    private String username;
    private String password;
    private Map<String, Permission> permissions;

    public FantaUser(String username, String password) {
        this.password = password;
        this.username = username;
        this.permissions = new HashMap<>();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void addPermission(Permission permission) {
        permissions.put(permission.getName(), permission);
    }

    @Override
    public void removePermission(String permissionName) {
        permissions.remove(permissionName);
    }

    @Override
    public boolean hasPermission(String permissionName) {
        String[] parts = permissionName.split("\\.");
        StringBuilder currentPermission = new StringBuilder();
        Boolean result = null;

        for (String part : parts) {
            if (currentPermission.length() > 0) {
                currentPermission.append(".");
            }
            currentPermission.append(part);

            if (permissions.containsKey(currentPermission.toString())) {
                result = permissions.get(currentPermission.toString()).getValue();
            }
            if (permissions.containsKey(currentPermission.toString() + ".*")) {
                result = permissions.get(currentPermission.toString() + ".*").getValue();
            }
        }

        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}

