package com.sheetcell.engine;

import java.util.HashSet;
import java.util.Set;

public class Cell {
    private Coordinate coordinate;
    private String originalValue;
    private Object effectiveValue;
    private int version;
    Set<Cell> dependencies;
    Set<Cell> influencedCells;
    //    private boolean isEvaluated;
    //    private boolean isDirty;
    //    private int row;

    // Constructor with Coordinate
    public Cell(Coordinate coordinate, String originalValue) {
        this.coordinate = coordinate;
        this.originalValue = originalValue;
        this.effectiveValue  = evaluateEffectiveValue(originalValue);
        this.version = 1;
        this.dependencies = new HashSet<>();
        this.influencedCells = new HashSet<>();
    }

    // Default Constructor
    public Cell() {
        //this.coordinate = new Coordinate(1, 'A'); // Default to A1
        this.originalValue = "";
        this.effectiveValue  = "";
        this.version = 1;
        this.dependencies = new HashSet<>();
        this.influencedCells = new HashSet<>();
    }

    // Getters and Setters
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
        this.effectiveValue = evaluateEffectiveValue(originalValue);
        incrementVersion();
    }

    public Object  getEvaluatedValue() {
        return effectiveValue;
    }

    public int getVersion() {
        return version;
    }

    private void incrementVersion() {
        this.version++;
    }

    public Object evaluateEffectiveValue(Object value) {
        // evaluate the effective value
        // set the evaluatedValue
        // For now, return the original value
        return value;
    }

    // Compare (equals) based on original values
    public boolean equalsOriginal(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Cell cell = (Cell) obj;

        return originalValue.equals(cell.originalValue);
    }

    // Add a cell to the dependencies list
    public void addDependency(Cell dependency) {
        dependencies.add(dependency);
    }

    // Remove a cell from the dependencies list
    public void removeDependency(Cell dependency) {
        dependencies.remove(dependency);
    }

    // Add a cell to the influencedCells list
    public void addInfluencedCell(Cell influencedCell) {
        influencedCells.add(influencedCell);
    }

    // Remove a cell from the influencedCells list
    public void removeInfluencedCell(Cell influencedCell) {
        influencedCells.remove(influencedCell);
    }

    // Getters for dependencies and influencedCells
    public Set<Cell> getDependencies() {
        return dependencies;
    }

    public Set<Cell> getInfluencedCells() {
        return influencedCells;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "coordinate=" + coordinate +
                ", originalValue='" + originalValue + '\'' +
                ", evaluatedValue='" + effectiveValue + '\'' +
                ", version=" + version +
                ", dependencies=" + dependencies.size() +
                ", influencedCells=" + influencedCells.size() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Cell cell = (Cell) obj;
        return effectiveValue .equals(cell.effectiveValue );
    }

    @Override
    public int hashCode() {
        return effectiveValue .hashCode();
    }
}

