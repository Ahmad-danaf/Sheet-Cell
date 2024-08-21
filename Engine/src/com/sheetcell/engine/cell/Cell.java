package com.sheetcell.engine.cell;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.api.SheetReadActions;

import java.util.HashSet;
import java.util.Set;

public class Cell {
    private Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    Set<Cell> dependencies;
    Set<Cell> influencedCells;
    private final SheetReadActions sheet;
    //    private boolean isEvaluated;
    //    private boolean isDirty;
    //    private int row;


    public Cell(int row, int column, String originalValue, int version, SheetReadActions sheet)  {
        this.sheet = sheet;
        this.coordinate = CoordinateFactory.createCoordinate(row, column);
        this.originalValue = originalValue;
        this.version = version;
        this.dependencies = new HashSet<>();
        this.influencedCells = new HashSet<>();
    }

    // Getters and Setters
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public EffectiveValue getEffectiveValue() { return effectiveValue; }

    public int getVersion() {
        return version;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    private void incrementVersion() {
        this.version++;
    }

    public boolean calculateEffectiveValue() {
        Expression expression = FunctionParser.parseExpression(originalValue);

        EffectiveValue newEffectiveValue = expression.eval(sheet);

        if (newEffectiveValue.equals(effectiveValue)) {
            return false;
        } else {
            effectiveValue = newEffectiveValue;
            return true;
        }
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
                ", effectiveValue='" + effectiveValue.getValue() + '\'' +
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
        return effectiveValue.hashCode();
    }
}

