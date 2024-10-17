package data;

public enum PermissionType {
    OWNER,   // Only the owner can modify permissions
    READER,  // Can only view the sheet
    WRITER,  // Can edit the sheet
    NONE     // Default, no permissions
}

