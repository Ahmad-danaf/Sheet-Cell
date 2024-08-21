package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class RefExpression implements Expression {

    private final Coordinate coordinate;

    public RefExpression(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        // Retrieve the cell from the sheet
        Cell cell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());

        // Check if the cell is null (i.e., not found) or has no effective value
        if (cell == null || cell.getEffectiveValue() == null) {
            throw new IllegalArgumentException("Error: The cell at " + coordinate + " does not exist or is empty. Please check your reference.");
        }

        // Return the effective value of the cell
        return cell.getEffectiveValue();
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.UNKNOWN;
    }
}
