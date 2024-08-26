package com.sheetcell.engine.utils;

import com.sheetcell.engine.sheet.Sheet;

public class SheetUpdateResult {
    private final Sheet sheet;
    private final String errorMessage;

    public SheetUpdateResult(Sheet sheet, String errorMessage) {
        this.sheet = sheet;
        this.errorMessage = errorMessage;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}

