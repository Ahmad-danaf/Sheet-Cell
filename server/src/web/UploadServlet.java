package web;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import responses.UploadFileResponse;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

@WebServlet("/upload") // This defines the URL endpoint
@MultipartConfig // This tells the servlet to handle file uploads
public class UploadServlet extends HttpServlet {

    // Handles POST requests (file uploads in this case)
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtain the uploaded file from the request (form field "file")
        Part filePart = request.getPart("file");
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        // Create a response object
        Gson gson = new Gson();
        UploadFileResponse uploadFileResponse;

        // Check if the file is XML
        if (fileName.endsWith(".xml")) {
            // Read the file content
            InputStream fileContent = filePart.getInputStream();
            String fileData = new Scanner(fileContent, StandardCharsets.UTF_8).useDelimiter("\\A").next();

            // Create success response with file content
            uploadFileResponse = new UploadFileResponse("File uploaded successfully", fileName, fileData);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Create error response without file content
            uploadFileResponse = new UploadFileResponse("Invalid file format. Only XML files are allowed.", null, null);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        // Convert the response to JSON and send it
        String jsonResponse = gson.toJson(uploadFileResponse);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().write("Hello from the UploadServlet!");
    }
}
