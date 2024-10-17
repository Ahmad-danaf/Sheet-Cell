package data;

import java.util.*;

public class SheetUserData {
    private String sheetName;
    private String owner;
    private String size;
    private Set<PermissionUserData> userPermissions; // Set of user permissions

    public SheetUserData(String sheetName, String owner, String size, PermissionType ownerPermission) {
        this.sheetName = sheetName;
        this.owner = owner;
        this.size = size;
        this.userPermissions = new HashSet<>();
        // Owner is initialized with acknowledged OWNER permission
        this.userPermissions.add(new PermissionUserData(owner, ownerPermission, PermissionStatus.ACKNOWLEDGED));
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getOwner() {
        return owner;
    }

    public String getSize() {
        return size;
    }

    public Set<PermissionUserData> getUserPermissions() {
        return userPermissions;
    }

    // Retrieve permission for a specific user
    public PermissionUserData getPermissionForUser(String userId) {
        return userPermissions.stream()
                .filter(permission -> permission.getUsername().equals(userId))
                .findFirst()
                .orElse(null);
    }

    // Add a new permission request for a user
    public void addPermissionRequest(PermissionUserData permissionRequest) {
        userPermissions.add(permissionRequest);
    }


    // Add or update a permission for a user, including status (ACKNOWLEDGED, PENDING, etc.)
    public void addPermissionForUser(String userId, PermissionType permissionType, PermissionStatus status) {
        PermissionUserData permissionData = getPermissionForUser(userId);
        if (permissionData == null) {
            // If permission doesn't exist, create a new one
            permissionData = new PermissionUserData(userId, permissionType, status);
            userPermissions.add(permissionData);
        } else {
            // If permission already exists, update it
            permissionData.setPermissionType(permissionType);
            permissionData.setStatus(status);
        }
    }

    // Acknowledge the permission request for a user (set status to ACKNOWLEDGED)
    public void acknowledgePermissionForUser(String userId) {
        userPermissions.stream()
                .filter(permission -> permission.getUsername().equals(userId))
                .forEach(permission -> permission.setStatus(PermissionStatus.ACKNOWLEDGED)); // Mark as acknowledged
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SheetUserData that = (SheetUserData) o;
        return Objects.equals(sheetName, that.sheetName) && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetName, owner);
    }
}
