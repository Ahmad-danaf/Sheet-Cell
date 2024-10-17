package web;

import com.google.gson.Gson;
import data.PermissionUserData;
import data.SheetUserData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;

import java.io.IOException;
import java.util.Set;

@WebServlet("/getPermissionsForSheet")
public class GetPermissionsForSheetServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        try {
            // Get UserManager from the servlet context
            UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
            if (userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson("UserManager not found"));
                return;
            }

            // Get the sheet name from the request
            String sheetName = request.getParameter("sheetName");
            if (sheetName == null || sheetName.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson("Missing or empty sheetName parameter"));
                return;
            }

            // Get the sheet by name
            SheetUserData sheetData = userManager.getSheetByName(sheetName);
            if (sheetData == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson("Sheet not found"));
                return;
            }

            // Retrieve all PermissionUserData for this sheet
            Set<PermissionUserData> userPermissions = sheetData.getUserPermissions();

            // Send the set of permissions as JSON response
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(userPermissions));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("An unexpected error occurred: " + e.getMessage()));
        }
    }
}
