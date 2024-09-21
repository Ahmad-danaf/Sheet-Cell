package com.sheetcell.engine.utils;

import java.util.HashMap;
import java.util.Map;

public class ColumnPropertyManager {

    private Map<Integer, ColumnProperties> columnPropertiesMap = new HashMap<>();

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
}
