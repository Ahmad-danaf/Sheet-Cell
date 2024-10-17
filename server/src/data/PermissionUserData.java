package data;

import java.util.Objects;

public class PermissionUserData {

    private String username;
    private PermissionType permissionType;
    private PermissionStatus status;

    public PermissionUserData(String username, PermissionType permissionType, PermissionStatus status) {
        this.username = username;
        this.permissionType = permissionType;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public PermissionStatus getStatus() {
        return status;
    }

    public void setStatus(PermissionStatus status) {
        this.status = status;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionUserData that = (PermissionUserData) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
