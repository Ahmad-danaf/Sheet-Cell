package com.sheetcell.engine;

public class EffectiveValue {

    private CellType cellType;
    private Object value;

    public EffectiveValue(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
    }

    public CellType getCellType() {
        return cellType;
    }

    public Object getValue() {
        return value;
    }

    public <T> T extractValueWithExpectation(Class<T> type) {
        if (cellType.isAssignableFrom(type)) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EffectiveValue that = (EffectiveValue) obj;

        if (cellType != that.cellType) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = cellType != null ? cellType.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
