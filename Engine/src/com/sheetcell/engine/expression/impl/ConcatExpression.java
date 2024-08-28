package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.BinaryExpression;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class ConcatExpression implements BinaryExpression {
    private final Expression str1;
    private final Expression str2;

    public ConcatExpression(Expression str1, Expression str2) {
//        System.out.println("str1: '" + str1.eval(null).castValueTo(String.class) + "'");
//        System.out.println("str2: '" + str2.eval(null).castValueTo(String.class) + "'");
        this.str1 = str1;
        this.str2 = str2;
    }

    @Override
    public Expression getLeft() {
        return str1;
    }

    @Override
    public Expression getRight() {
        return str2;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Evaluate the expressions
        EffectiveValue str1Value = str1.eval(sheet, callingCell);
        EffectiveValue str2Value = str2.eval(sheet, callingCell);

        // Check if the values can be cast to String
        String str1String = str1Value.castValueTo(String.class);
        String str2String = str2Value.castValueTo(String.class);

        // If either value is null, it means the cast failed (not a string type)
        if (str1String == null || str2String == null) {
            String cellCoordinates = (callingCell != null && callingCell.getCoordinate() != null)
                    ? callingCell.getCoordinate().toString()
                    : "unknown";
            if (cellCoordinates.equals("unknown")) {
               throw new IllegalArgumentException("Error at '"+ cellCoordinates+"': One of the arguments provided to the CONCAT function is not a string.\n" +
                        "Please ensure that both arguments are valid string values.");
            }
            else{
                throw new IllegalArgumentException("Error: One of the arguments provided to the CONCAT function is not a string.\n" +
                        "Please ensure that both arguments are valid string values.");
            }
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

