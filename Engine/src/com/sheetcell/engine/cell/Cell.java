package com.sheetcell.engine.cell;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.api.SheetReadActions;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;


    private Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    List<Cell> dependencies; // Cells that this cell depends on
    List<Cell> influencedCells; // Cells that depend on this cell
    private final SheetReadActions sheet;
    private String user = "";
    //    private boolean isEvaluated;
    //    private boolean isDirty;
    //    private int row;


    public Cell(int row, int column, String originalValue, int version, SheetReadActions sheet) {
        this.sheet = sheet;
        this.coordinate = CoordinateFactory.createCoordinate(row, column);
        this.originalValue = originalValue;
        this.version = version;
        this.dependencies = new LinkedList<>();
        this.influencedCells = new LinkedList<>();
    }

    public Cell(int row, int column, String originalValue, int version, SheetReadActions sheet, String user) {
        this.sheet = sheet;
        this.coordinate = CoordinateFactory.createCoordinate(row, column);
        this.originalValue = originalValue;
        this.version = version;
        this.dependencies = new LinkedList<>();
        this.influencedCells = new LinkedList<>();
        this.user = user;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    private void incrementVersion() {
        this.version++;
    }

    public boolean calculateEffectiveValue() {
        Expression expression = FunctionParser.parseExpression(originalValue);

        EffectiveValue newEffectiveValue = expression.eval(sheet, this);

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

    public void addDependency(Cell dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    public void addInfluencedCell(Cell influencedCell) {
        if (!influencedCells.contains(influencedCell)) {
            influencedCells.add(influencedCell);
        }
    }


    // Remove a cell from the dependencies list
    public void removeDependency(Cell dependency) {
        dependencies.remove(dependency);
    }

    // Remove a cell from the influencedCells list
    public void removeInfluencedCell(Cell influencedCell) {
        influencedCells.remove(influencedCell);
    }

    // Getters for dependencies and influencedCells
    public List<Cell> getDependencies() {
        return dependencies;
    }

    public List<Cell> getInfluencedCells() {
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

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//
//        Cell cell = (Cell) obj;
//        return effectiveValue .equals(cell.effectiveValue );
//    }
//
//    @Override
//    public int hashCode() {
//        return effectiveValue.hashCode();
//    }
}

