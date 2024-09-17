package com.sheetcell.engine.expression.impl.numeric;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.expression.api.RangeExpression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

import java.util.Set;

public class SumExpression implements RangeExpression {
    private final String rangeName;

    public SumExpression(String rangeName) {
        this.rangeName = rangeName;
    }

    @Override
    public String getRange() {
        return rangeName;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        Set<Coordinate> coordinates = sheet.getRangeCoordinates(rangeName);

        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("Error: The specified range '" + rangeName + "' does not exist.");
        }

        double sum = 0;
        for (Coordinate coordinate : coordinates) {
            Cell cell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());
            if (cell != null && cell.getEffectiveValue() != null) {
                Double numericValue = cell.getEffectiveValue().castValueTo(Double.class);
                if (numericValue != null) {
                    sum += numericValue;
                }
            }
        }
        sheet.markRangeAsUsed(rangeName);
        return new EffectiveValue(CellType.NUMERIC, sum);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
