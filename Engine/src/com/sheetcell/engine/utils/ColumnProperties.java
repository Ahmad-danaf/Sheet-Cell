package com.sheetcell.engine.utils;

public class ColumnProperties {
    private String alignment;
    private int width;

    public ColumnProperties(String alignment, int width) {
        this.alignment = alignment;
        this.width = width;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}

