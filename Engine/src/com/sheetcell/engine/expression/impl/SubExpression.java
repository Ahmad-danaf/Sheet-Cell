package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class SubExpression implements Expression {
    private final Expression source;
    private final Expression startIndex;
    private final Expression endIndex;

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        this.source = source;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        // Evaluate the expressions
        EffectiveValue sourceValue = source.eval(sheet);
        EffectiveValue startIndexValue = startIndex.eval(sheet);
        EffectiveValue endIndexValue = endIndex.eval(sheet);

        // Convert the values to appropriate types
        String sourceStr = sourceValue.castValueTo(String.class);
        Double startDouble = startIndexValue.castValueTo(Double.class);
        Double endDouble = endIndexValue.castValueTo(Double.class);

        // Check for type conversion issues
        if (sourceStr == null) {
            throw new IllegalArgumentException("Error: The source provided to the SUB function is not a valid string. " +
                    "Please ensure that the source argument is a valid string value.");
        }
        if (startDouble == null || endDouble == null) {
            throw new IllegalArgumentException("Error: The start or end index provided to the SUB function is not numeric. " +
                    "Please ensure that both indices are valid numeric values.");
        }

        // Convert indices to int
        int start = startDouble.intValue();
        int end = endDouble.intValue();

        // Check if indices are within bounds
        if (start < 0 || end >= sourceStr.length() || start > end) {
            return new EffectiveValue(CellType.STRING, "!UNDEFINED!"); // Return UNDEFINED if indices are invalid
        }

        // Extract the substring
        String result = sourceStr.substring(start, end + 1);

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.STRING, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}

