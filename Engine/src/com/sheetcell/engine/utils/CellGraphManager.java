package com.sheetcell.engine.utils;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.cell.Cell;

import java.util.*;

public class CellGraphManager {

    public static List<Cell> topologicalSort(Map<Coordinate, Cell> activeCells) {
        List<Cell> sortedCells = new ArrayList<>();
        Map<Cell, Integer> inDegree = new HashMap<>();
        Queue<Cell> zeroInDegreeQueue = new LinkedList<>();

        // Initialize in-degrees
        for (Cell cell : activeCells.values()) {
            inDegree.put(cell, cell.getDependencies().size());
            if (cell.getDependencies().isEmpty()) {
                zeroInDegreeQueue.add(cell);
            }
        }

        // Process cells with zero in-degree
        while (!zeroInDegreeQueue.isEmpty()) {
            Cell currentCell = zeroInDegreeQueue.poll();
            sortedCells.add(currentCell);

            for (Cell influencedCell : currentCell.getInfluencedCells()) {
                int updatedInDegree = inDegree.get(influencedCell) - 1;
                inDegree.put(influencedCell, updatedInDegree);
                if (updatedInDegree == 0) {
                    zeroInDegreeQueue.add(influencedCell);
                }
            }
        }

        // If there are still cells with non-zero in-degree, there's a cycle
        if (sortedCells.size() != activeCells.size()) {
            throw new IllegalStateException("Cycle detected in cell dependencies!");
        }

        return sortedCells;
    }

    // Method 2: Detect Cycles in the Graph
    public  static boolean hasCycle(Map<Coordinate, Cell> activeCells) {
        try {
            topologicalSort(activeCells);
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

}
