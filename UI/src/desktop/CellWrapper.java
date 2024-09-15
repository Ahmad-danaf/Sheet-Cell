package desktop;

import com.sheetcell.engine.cell.Cell;

public class CellWrapper {
    private Cell cell;
    private int originalRow;
    private int column;
    private String style = "";
    private String highlightStyle = "";


    public CellWrapper(Cell cell, int row, int column) {
        this.cell = cell;
        this.originalRow  = row;
        this.column = column;
    }

    public String getHighlightStyle() {
        return highlightStyle;
    }

    public Cell getCell() {
        return cell;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setHighlightStyle(String highlightStyle) {
        this.highlightStyle = highlightStyle;
    }

    // Getters for row and column indices if needed
    public int getOriginalRow() {
        return originalRow;
    }

    public int getColumn() {
        return column;
    }
}