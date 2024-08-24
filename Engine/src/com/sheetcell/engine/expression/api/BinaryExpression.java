package com.sheetcell.engine.expression.api;

public interface BinaryExpression extends Expression {
    Expression getLeft();
    Expression getRight();
}
