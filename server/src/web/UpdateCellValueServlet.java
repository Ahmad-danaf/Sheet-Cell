package web;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.SheetUpdateResult;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.SheetDataUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/updateCellValue")
public class UpdateCellValueServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        String cellAddress = request.getParameter("cellAddress");
        String newValue = request.getParameter("newValue");
        String userId = request.getParameter("username");

        if(sheetName == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing sheetName parameter.")));
            return;
        }
        if(cellAddress == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing cellAddress parameter.")));
            return;
        }
        if(newValue == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing newValue parameter.")));
            return;
        }
        if(userId == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing userId parameter.")));
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
            // Validate and update cell value
            SheetUpdateResult result = engine.setCellValue(cellAddress, newValue,userId);

            if (result.isNoActionNeeded()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(Map.of("message", "No action needed: " + result.getErrorMessage())));
                return;
            } else if (result.hasError()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of("error", "Update failed: " + result.getErrorMessage())));
                return;
            }

            // Gather updated sheet data to return
            Map<String, Object> updatedSheetData = SheetDataUtils.getSheetData(engine.getReadOnlySheet(),engine,engine.getReadOnlySheet().getVersion());
            //add the username
            updatedSheetData.put("username", userId);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updatedSheetData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error","Update failed: " +  e.getMessage())));
        }
    }
}
