package utils.cell;


public class CellWrapper {
    private String originalValue;
    private String effectiveValue;
    private int originalRow;
    private int column;
    private String style = "";
    private String highlightStyle = "";
    private int height;
    private int version;


    public CellWrapper(String originalValue, String effectiveValue,int version ,int row, int column) {
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.originalRow  = row;
        this.column = column;
        this.version = version;
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

    public String getOriginalValue() {
        return originalValue;
    }

    public String getEffectiveValue() {
        return effectiveValue;
    }

    public int getVersion() {
        return version;
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
        String value = effectiveValue != null ? effectiveValue : "" ;
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
