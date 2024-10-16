package data;

public class PermissionUserData {
    private String username;
    private String permissionType;
    private String status;

    public PermissionUserData(String username, String permissionType, String status) {
        this.username = username;
        this.permissionType = permissionType;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public String getStatus() {
        return status;
    }
}
