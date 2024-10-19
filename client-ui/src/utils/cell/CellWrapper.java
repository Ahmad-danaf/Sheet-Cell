package utils.cell;

import com.sheetcell.engine.cell.Cell;

public class CellWrapper {
    private Cell cell;
    private int originalRow;
    private int column;
    private String style = "";
    private String highlightStyle = "";
    private int height;


    public CellWrapper(Cell cell, int row, int column) {
        this.cell = cell;
        this.originalRow  = row;
        this.column = column;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
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

    @Override
    public String toString() {
        String value = cell != null && cell.getEffectiveValue()!=null ? cell.getEffectiveValue().toString() : "" ;
        if (value.isEmpty()){
            return "";
        }
        if (value.equalsIgnoreCase("true")) {
            return "TRUE";
        } else if (value.equalsIgnoreCase("false")) {
            return "FALSE";
        }
        return value;
    }
}
