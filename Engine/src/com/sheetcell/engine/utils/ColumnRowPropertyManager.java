package com.sheetcell.engine.utils;

import java.util.HashMap;
import java.util.Map;

public class ColumnRowPropertyManager {

    private Map<Integer, ColumnProperties> columnPropertiesMap = new HashMap<>();
    private Map<Integer, RowProperties> rowPropertiesMap = new HashMap<>();

    public void setColumnProperties(Integer column, String alignment, int width) {
        ColumnProperties properties = new ColumnProperties(alignment, width);
        columnPropertiesMap.put(column, properties);
    }

    public ColumnProperties getColumnProperties(Integer column) {
        return columnPropertiesMap.get(column);
    }

    public void setColumnAlignment(Integer column, String alignment) {
        ColumnProperties properties = columnPropertiesMap.get(column);
        if (properties == null) {
            return;
        }
        properties.setAlignment(alignment);
    }

    public void setColumnWidth(Integer column, int width) {
        ColumnProperties properties = columnPropertiesMap.get(column);
        if (properties == null) {
            return;
        }
        properties.setWidth(width);
    }

    //init column properties-for every column same properties
    public void initColumnProperties(Integer maxColumn,int height,int width){
        clearColumnProperties();
        for(int i=0;i<maxColumn;i++){
            setColumnProperties(i, "Center", width);
        }
    }

    public void clearColumnProperties() {
        columnPropertiesMap.clear();
    }

    public void setRowProperties(Integer row, int height) {
        RowProperties properties = new RowProperties(height);
        rowPropertiesMap.put(row, properties);
    }
    public RowProperties getRowProperties(Integer row) {
        return rowPropertiesMap.get(row);
    }
    public void setRowHeight(Integer row, int height) {
        RowProperties properties = rowPropertiesMap.get(row);
        if (properties == null) {
            return;
        }
        properties.setHeight(height);
    }
    public void clearRowProperties() {
        rowPropertiesMap.clear();
    }
    public void initRowProperties(Integer maxRow,int height){
        clearRowProperties();
        for(int i=0;i<maxRow;i++){
            setRowProperties(i, height);
        }
    }

    public void clearAllProperties() {
        clearColumnProperties();
        clearRowProperties();
    }

    public void initAllProperties(Integer maxRow,Integer maxColumn,int height,int width){
        initColumnProperties(maxColumn, height, width);
        initRowProperties(maxRow, height);
    }
}
