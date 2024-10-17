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

@WebServlet("/acknowledgePermission")
public class AcknowledgePermissionServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            // Get UserManager from the servlet context
            UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
            if (userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.put("error", "Server configuration error: UserManager is not initialized.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Retrieve necessary request parameters
            String sheetName = request.getParameter("sheetName");
            String targetUser = request.getParameter("targetUser");
            String permission = request.getParameter("permission");
            String status = request.getParameter("status");  // New parameter to handle denial
            String ownerId = (String) request.getSession().getAttribute("username");

            // Ensure that all parameters are present
            if (sheetName == null || targetUser == null || permission == null || status == null || ownerId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "Missing required parameters.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Get the sheet by name
            SheetUserData sheetData = userManager.getSheetByName(sheetName);
            if (sheetData == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.put("error", "Sheet not found.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Check if the current user is the owner of the sheet
            if (!sheetData.getOwner().equals(ownerId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.put("error", "You are not authorized to manage permissions for this sheet.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Find the permission for the target user and update it
            PermissionUserData permissionData = sheetData.getPermissionForUser(targetUser);
            if (permissionData == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.put("error", "Permission request for the target user not found.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Handle acknowledgment or denial based on the 'status' parameter
            if ("ACKNOWLEDGED".equals(status)) {
                // Update the permission type and mark it as acknowledged
                permissionData.setPermissionType(PermissionType.valueOf(permission));
                permissionData.setStatus(PermissionStatus.ACKNOWLEDGED);
                jsonResponse.put("message", "Permission updated and acknowledged successfully.");
            } else if ("DENIED".equals(status)) {
                // Mark the request as denied
                permissionData.setStatus(PermissionStatus.DENIED);
                jsonResponse.put("message", "Permission request denied.");
            } else {
                // Invalid status value
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "Invalid status value: " + status);
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Send success response
            response.setStatus(HttpServletResponse.SC_OK);
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
