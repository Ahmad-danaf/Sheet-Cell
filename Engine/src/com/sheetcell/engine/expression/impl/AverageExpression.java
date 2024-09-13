package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.sheet.api.SheetReadActions;

import com.sheetcell.engine.expression.api.RangeExpression;

import java.util.Set;

public class AverageExpression implements RangeExpression {
    private final String rangeName;

    public AverageExpression(String rangeName) {
        this.rangeName = rangeName;
    }

    @Override
    public String getRange() {
        return rangeName;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Retrieve the coordinates of cells in the specified range
        Set<Coordinate> coordinates = sheet.getRangeCoordinates(rangeName);

        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("Error: The specified range '" + rangeName + "' does not exist.");
        }

        double sum = 0;
        int count = 0;
        for (Coordinate coordinate : coordinates) {
            Cell cell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());
            if (cell != null && cell.getEffectiveValue() != null) {
                Double numericValue = cell.getEffectiveValue().castValueTo(Double.class);
                if (numericValue != null) {
                    sum += numericValue;
                    count++;
                }
            }
        }

        if (count == 0) {
            throw new IllegalArgumentException("Error: The specified range '" + rangeName + "' does not contain any numeric cells.");
        }

        double average = sum / count;
        return new EffectiveValue(CellType.NUMERIC, average);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
