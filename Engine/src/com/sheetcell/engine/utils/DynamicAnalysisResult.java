package com.sheetcell.engine.utils;

import com.sheetcell.engine.sheet.api.SheetReadActions;

import java.util.HashMap;
import java.util.Map;

public class DynamicAnalysisResult {
    private final Map<Double, SheetReadActions> valueToSheetMap;
    private final int maxRows;
    private final int maxColumns;


    public DynamicAnalysisResult(int maxRows, int maxColumns) {
        this.valueToSheetMap = new HashMap<>();
        this.maxRows = maxRows;
        this.maxColumns = maxColumns;
    }


    public int getMaxRows() {
        return maxRows;
    }


    public int getMaxColumns() {
        return maxColumns;
    }

    public void addResult(double value, SheetReadActions sheet) {
        valueToSheetMap.put(value, sheet);
    }

    public Map<Double, SheetReadActions> getResults() {
        return valueToSheetMap;
    }
}
