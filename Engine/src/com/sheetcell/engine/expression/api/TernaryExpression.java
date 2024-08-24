package com.sheetcell.engine.expression.api;

public interface TernaryExpression extends Expression {
    Expression getFirst();
    Expression getSecond();
    Expression getThird();
}