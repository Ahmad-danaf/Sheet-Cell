package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class ConcatExpression implements Expression {
    private final Expression str1;
    private final Expression str2;

    public ConcatExpression(Expression str1, Expression str2) {
//        System.out.println("str1: '" + str1.eval(null).castValueTo(String.class) + "'");
//        System.out.println("str2: '" + str2.eval(null).castValueTo(String.class) + "'");
        this.str1 = str1;
        this.str2 = str2;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        // Evaluate the expressions
        EffectiveValue str1Value = str1.eval(sheet);
        EffectiveValue str2Value = str2.eval(sheet);

        // Check if the values can be cast to String
        String str1String = str1Value.castValueTo(String.class);
        String str2String = str2Value.castValueTo(String.class);

        // If either value is null, it means the cast failed (not a string type)
        if (str1String == null || str2String == null) {
            throw new IllegalArgumentException("Error: One of the arguments provided to the CONCAT function is not a string. " +
                    "Please ensure that both arguments are valid string values.");
        }

        String result = str1String + str2String;

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.STRING, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}

