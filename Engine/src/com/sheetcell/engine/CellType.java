package com.sheetcell.engine;

public enum CellType {
    STRING(String.class),
    NUMBER(Double.class),
    BOOLEAN(Boolean.class);

    private final Class<?> type;

    CellType(Class<?> type) {
        this.type = type;
    }

    public boolean isAssignableFrom(Class<?> type) {
        return this.type.isAssignableFrom(type);
    }
}
