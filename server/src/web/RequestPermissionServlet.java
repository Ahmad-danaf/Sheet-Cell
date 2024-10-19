package web;

import com.google.gson.Gson;
import data.PermissionStatus;
import data.PermissionType;
import data.PermissionUserData;
import data.SheetUserData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/requestPermission")
public class RequestPermissionServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            // Retrieve the UserManager from the servlet context
            UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
            if (userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.put("error", "Server configuration error: UserManager is not initialized.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Extract request parameters
            String sheetName = request.getParameter("sheetName");
            String requester = request.getParameter("requester");
            String requestedPermission = request.getParameter("requestedPermission");

            // Validate that all required parameters are present
            if (sheetName == null || requester == null || requestedPermission == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "Missing required parameters.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Fetch the sheet by name
            SheetUserData sheetData = userManager.getSheetByName(sheetName);
            if (sheetData == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.put("error", "Sheet not found.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Add the permission request to the sheet
            // By default, the permission is marked as not acknowledged (pending approval by the owner)
            PermissionType permissionType = PermissionType.valueOf(requestedPermission);
            PermissionUserData permissionRequest = new PermissionUserData(requester, permissionType,
                    PermissionStatus.PENDING);
            sheetData.addPermissionForUser(requester, permissionType, PermissionStatus.PENDING);

            // Send success response
            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.put("message", "Permission request sent successfully.");
            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (IllegalArgumentException e) {
            // Handle invalid permission type
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("error", "Invalid permission type: " + e.getMessage());
            response.getWriter().write(gson.toJson(jsonResponse));
        } catch (Exception e) {
            // Handle unexpected errors
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }
}
