package data;

public enum PermissionStatus {
    PENDING,    // Default state when a permission is requested
    ACKNOWLEDGED,  // When the owner has granted the permission
    DENIED     // When the owner has denied the permission
}

