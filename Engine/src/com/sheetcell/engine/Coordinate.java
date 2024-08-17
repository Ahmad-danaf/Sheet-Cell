package com.sheetcell.engine;

public class Coordinate {
    private int row;
    private char column;

    // Constructor
    public Coordinate(int row, char column) {
        this.row = row;
        this.column = column;
    }

    // Getters and Setters
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public char getColumn() {
        return column;
    }

    public void setColumn(char column) {
        this.column = column;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Coordinate that = (Coordinate) obj;

        if (row != that.row) return false;
        return column == that.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + (int) column;
        return result;
    }

    @Override
    public String toString() {
        return "" + column + row;
    }
}
