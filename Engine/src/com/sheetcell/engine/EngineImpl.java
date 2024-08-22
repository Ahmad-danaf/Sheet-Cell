package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.expression.parser.FunctionParser;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.schema.generatedFiles.STLCell;
import jaxb.schema.generatedFiles.STLCells;
import jaxb.schema.generatedFiles.STLLayout;
import jaxb.schema.generatedFiles.STLSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EngineImpl implements Engine {
    private Sheet currentSheet;

    @Override
    public void loadSheet(String filePath) {
    }

    private void updateCurrentSheet(Sheet tempSheet) {
        this.currentSheet = tempSheet;
    }

    @Override
    public void saveSheet(String filePath) {
        // Save the current sheet to a file
    }

    @Override
    public void setCellValue(String cellId, String value) {
        // Sets the value of a cell
    }

    @Override
    public void displaySheet() {
        // Displays the current sheet
    }

    @Override
    public Cell getCell(String cellId) {
        // Retrieves a cell by its ID
        return null;
    }
}
