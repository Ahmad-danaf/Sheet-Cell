package desktop;

public class CellRange {
    public final int startRow;
    public final int startCol;
    public final int endRow;
    public final int endCol;

    public CellRange(int startRow, int startCol, int endRow, int endCol) {
        // Ensure startRow <= endRow and startCol <= endCol
        this.startRow = Math.min(startRow, endRow);
        this.startCol = Math.min(startCol, endCol);
        this.endRow = Math.max(startRow, endRow);
        this.endCol = Math.max(startCol, endCol);
    }
}

