package com.sheetcell.engine.utils;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RangeFactory {
    // Map of range name to the set of coordinates it covers
    private static final Map<String, Set<Coordinate>> ranges = new HashMap<>();

    /**
     * Adds a new range to the factory.
     *
     * @param rangeName The name of the range.
     * @param fromCoord The starting coordinate of the range.
     * @param toCoord   The ending coordinate of the range.
     * @throws IllegalArgumentException if the range name already exists.
     */
    public static void addRange(String rangeName, Coordinate fromCoord, Coordinate toCoord) {
        if (ranges.containsKey(rangeName)) {
            throw new IllegalArgumentException("Range name '" + rangeName + "' already exists.");
        }

        Set<Coordinate> rangeCells = new HashSet<>();

        // Iterate from the "from" coordinate to the "to" coordinate
        for (int row = fromCoord.getRow(); row <= toCoord.getRow(); row++) {
            for (int col = fromCoord.getColumn(); col <= toCoord.getColumn(); col++) {
                rangeCells.add(CoordinateFactory.createCoordinate(row, col));
            }
        }

        ranges.put(rangeName, rangeCells);
    }



    /**
     * Retrieves the set of coordinates covered by a range.
     *
     * @param rangeName The name of the range.
     * @return The set of coordinates covered by the range, or an empty set if the range does not exist.
     */
    public static Set<Coordinate> getRange(String rangeName) {
        return ranges.getOrDefault(rangeName, new HashSet<>());
    }

    /**
     * Updates the range with a new cell coordinate.
     *
     * @param rangeName The name of the range.
     * @param coord     The coordinate to update or add to the range.
     */
    public static void updateRange(String rangeName, Coordinate coord) {
        ranges.computeIfAbsent(rangeName, k -> new HashSet<>()).add(coord);
    }

    /**
     * Checks if a range exists.
     *
     * @param rangeName The name of the range.
     * @return True if the range exists, false otherwise.
     */
    public static boolean rangeExists(String rangeName) {
        return ranges.containsKey(rangeName);
    }

    /**
     * Removes a coordinate from a range.
     *
     * @param rangeName The name of the range.
     * @param coord     The coordinate to remove from the range.
     */
    public static void removeCoordinateFromRange(String rangeName, Coordinate coord) {
        Set<Coordinate> rangeCells = ranges.get(rangeName);
        if (rangeCells != null) {
            rangeCells.remove(coord);
        }
    }

    /**
     * Deletes a range from the factory.
     *
     * @param rangeName The name of the range to be deleted.
     */
    public static void deleteRange(String rangeName) {
        ranges.remove(rangeName);
    }

    public static Set<String> getAllRangeNames() {
        return ranges.keySet();
    }
}
