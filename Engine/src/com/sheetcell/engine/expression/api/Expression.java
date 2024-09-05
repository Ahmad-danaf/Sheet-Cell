package com.sheetcell.engine.expression.api;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public interface Expression {
    EffectiveValue eval(SheetReadActions sheet, Cell callingCell);
    CellType getFunctionResultType();



    public static boolean isInvalidNumeric(EffectiveValue value) {
        // Check for null and type mismatch
        if (value == null || value.getCellType() != CellType.NUMERIC || value.getValue() == null) {
            return true;
        }
        // Check for invalid numeric values
        Double numericValue = value.castValueTo(Double.class);
        return numericValue == null || Double.isNaN(numericValue) ||
                value.getValue().toString().equals("!UNDEFINED!") ||
                value.getValue().toString().equals("UNKNOWN");
    }

    public static boolean isInvalidString(EffectiveValue value) {
        // Check for null and type mismatch
        if (value == null || value.getCellType() != CellType.STRING || value.getValue() == null) {
            return true;
        }
        // Check for invalid string values
        return value.getValue().toString().equals("!UNDEFINED!") ||
                value.getValue().toString().equals("UNKNOWN");
    }

    public static boolean isInvalidBoolean(EffectiveValue value) {
        // Check for null and type mismatch
        if (value == null || value.getCellType() != CellType.BOOLEAN || value.getValue() == null) {
            return true;
        }
        // Check for invalid boolean values
        return value.getValue().toString().equals("!UNDEFINED!") ||
                value.getValue().toString().equals("UNKNOWN");
    }
}
