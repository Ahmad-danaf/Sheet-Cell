package com.sheetcell.engine.expression.api;

import com.sheetcell.engine.coordinate.Coordinate;

public interface ReferenceExpression extends Expression {
    Coordinate getCoordinate();
}

