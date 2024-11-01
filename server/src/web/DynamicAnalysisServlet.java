package web;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.utils.DynamicAnalysisResult;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.SheetDataUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/performDynamicAnalysis")
public class DynamicAnalysisServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        String cellAddress = request.getParameter("cellAddress");
        double minValue;
        double maxValue;
        double stepSize;

        try {
            minValue = Double.parseDouble(request.getParameter("minValue"));
            maxValue = Double.parseDouble(request.getParameter("maxValue"));
            stepSize = Double.parseDouble(request.getParameter("stepSize"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid numeric parameters.")));
            return;
        }

        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized.")));
            return;
        }

        Engine engine = engineManager.getSheetEngine(sheetName);
        if (engine == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
            return;
        }

        try {
            int row= CoordinateFactory.getRowIndex(cellAddress);
            int col= CoordinateFactory.getColumnIndex(cellAddress);
            DynamicAnalysisResult analysisResult = engine.performDynamicAnalysis(row, col, minValue, maxValue, stepSize);
            Map<Double, Map<String, Map<String, String>>> analysisData = SheetDataUtils.getDynamicAnalysisData(analysisResult);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(analysisData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Failed to perform dynamic analysis: " + e.getMessage())));
        }
    }
}
