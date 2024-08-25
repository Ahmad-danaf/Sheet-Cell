package com.sheetcell.engine.coordinate;

import java.io.Serializable;

public class Coordinate implements Serializable {
    private static final long serialVersionUID = 1L;


    private final int row;
    private final int column;

    // Constructor
    public Coordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }

    // Getters and Setters
    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
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
        return 31 * row + column;
    }

    @Override
    public String toString() {
        return CoordinateFactory.convertIndexToCellCord(row,column) + " ";
    }
}
