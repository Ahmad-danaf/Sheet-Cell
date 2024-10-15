package dashboard;

public class SheetUserData {
    private String sheetName;
    private String owner;
    private String size;

    public SheetUserData(String sheetName, String owner, String size) {
        this.sheetName = sheetName;
        this.owner = owner;
        this.size = size;
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
}
