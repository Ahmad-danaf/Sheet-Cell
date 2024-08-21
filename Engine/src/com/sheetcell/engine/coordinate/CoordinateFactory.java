package com.sheetcell.engine.coordinate;

import java.util.HashMap;
import java.util.Map;

public class CoordinateFactory {
    private static Map<String, Coordinate> cachedCoordinates = new HashMap<>();

    public static Coordinate createCoordinate(int row, int column) {

        String key = row + ":" + column;
        if (cachedCoordinates.containsKey(key)) {
            return cachedCoordinates.get(key);
        }

        Coordinate coordinate = new Coordinate(row, column);
        cachedCoordinates.put(key, coordinate);

        return coordinate;
    }

    public static Coordinate from(String trim) {
        try {
            String[] parts = trim.split(":");
            return createCoordinate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
