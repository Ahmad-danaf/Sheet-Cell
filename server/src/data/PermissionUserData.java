package data;

import java.util.Objects;

public class PermissionUserData {

    private String username;
    private PermissionType permissionType;
    private PermissionStatus status;
    private PermissionType prevAcknowledgedPermission;

    public PermissionUserData(String username, PermissionType permissionType, PermissionStatus status) {
        this.username = username;
        this.permissionType = permissionType;
        this.status = status;
        prevAcknowledgedPermission=PermissionType.NONE;
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

    public PermissionType getPrevAcknowledgedPermission() {
        return prevAcknowledgedPermission;
    }

    public void setStatus(PermissionStatus status) {
        this.status = status;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public void setPrevAcknowledgedPermission(PermissionType prevAcknowledgedPermission) {
        this.prevAcknowledgedPermission = prevAcknowledgedPermission;
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
