package web;

import com.google.gson.Gson;
import data.PermissionType;
import data.SheetUserData;
import engine.EngineManager;
import engine.UserSheetEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import users.UserManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
            UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");

            if (engineManager == null || userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.put("error", "Server configuration error: EngineManager or UserManager is not initialized.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            // Retrieve file parts from the request
            Part filePart = request.getPart("file"); // "file" should match the name attribute in Postman
            Part fileNamePart = request.getPart("fileName");
            Part userNamePart = request.getPart("username");

            if (filePart == null || fileNamePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "File or file name is missing.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            if (userNamePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "Username is missing.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Read the file name
            String fileName = new Scanner(fileNamePart.getInputStream()).nextLine();

            // Read the file content
            String fileContent = new Scanner(filePart.getInputStream()).useDelimiter("\\A").next();

            String username = new Scanner(userNamePart.getInputStream()).nextLine();
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.put("error", "User is not authenticated.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Get the user's sheet engine
            UserSheetEngine userEngine = engineManager.getUserEngine(username);
            // check if it xml file
            if (!fileName.endsWith(".xml")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "The uploaded file is not an XML file.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Check if a sheet with the same name already exists
            if (userManager.isSheetExists(fileName)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                jsonResponse.put("error", "A sheet with this name already exists.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Attempt to load the sheet using the user's engine
            try {
                userEngine.loadSheetFromContentXML(fileContent, fileName,username); // If this method does not throw an exception, the sheet is valid
                response.setStatus(HttpServletResponse.SC_OK);
                //add too the json the max col and row
                int[] maxColRow = userEngine.getMaxColRow(fileName);
                String maxColumns = String.valueOf(maxColRow[0]);
                String maxRows = String.valueOf(maxColRow[1]);
                jsonResponse.put("maxColumns", maxColumns);
                jsonResponse.put("maxRows", maxRows);
                // size is nXm where n is the number of columns and m is the number of rows
                String size = maxColumns + "x" + maxRows;
                SheetUserData sheetUserData = new SheetUserData(fileName, username, size, PermissionType.OWNER);
                userManager.addSheetToUser(username, sheetUserData);
                jsonResponse.put("sheets", userManager.getAllSheets());
                jsonResponse.put("message", "File uploaded and processed successfully.");
                response.getWriter().write(gson.toJson(jsonResponse));
            } catch (Exception e) {
                // If an exception is thrown, the sheet is invalid
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("error", "The uploaded sheet is invalid: " + e.getMessage());
                response.getWriter().write(gson.toJson(jsonResponse));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().write("Hello from the UploadServlet!");
    }
}
