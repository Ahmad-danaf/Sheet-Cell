package com.sheetcell.engine.utils;

import com.sheetcell.engine.sheet.Sheet;

public class SheetUpdateResult {
    private final Sheet sheet;
    private final String errorMessage;
    private final boolean noActionNeeded;

    public SheetUpdateResult(Sheet sheet, String errorMessage) {
        this.sheet = sheet;
        this.errorMessage = errorMessage;
        this.noActionNeeded = false;
    }

    public SheetUpdateResult(Sheet sheet, String errorMessage, boolean noActionNeeded) {
        this.sheet = sheet;
        this.errorMessage = errorMessage;
        this.noActionNeeded = noActionNeeded;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public boolean isNoActionNeeded() {
        return noActionNeeded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}

