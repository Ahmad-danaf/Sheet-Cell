package com.sheetcell.engine.cell;

public enum CellType {
    NUMERIC(Double.class),
    STRING(String.class),
    BOOLEAN(Boolean.class),
    UNKNOWN(Void.class);


    private final Class<?> type;

    CellType(Class<?> type) {
        this.type = type;
    }

    public boolean isAssignableFrom(Class<?> type) {
        return this.type.isAssignableFrom(type);
    }
}
