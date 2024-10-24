package utils.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.UIHelper;

import java.io.IOException;
import java.util.Map;

import static utils.UIHelper.showError;

public class RequestUtils {

    private static final Gson gson = new Gson();

    // Send a request to delete the selected range
    public static void deleteRange(String SheetName, String rangeName) {
        Request deleteRequest = new Request.Builder()
                .url("http://localhost:8080/webapp/deleteRange")
                .post(new FormBody.Builder()
                        .add("sheetName", SheetName) // Pass the sheet name
                        .add("rangeName", rangeName) // Pass the range to delete
                        .build())
                .build();

        HttpClientUtil.runAsync(deleteRequest, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to delete range: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> UIHelper.showAlert("Success", "Range deleted successfully."));
                } else {
                    String responseBody = null;
                    if (response.body() != null) {
                        // Read the response body once
                        responseBody = response.body().string();
                    }
                    String errorMessage = "Failed to delete range";
                    if (responseBody != null && !responseBody.isEmpty()) {
                        try {
                            // Try to parse the response body as JSON and extract the error message
                            Map<String, String> errorResponse = gson.fromJson(responseBody, new TypeToken<Map<String, String>>(){}.getType());
                            if (errorResponse.containsKey("error")) {
                                errorMessage += ": " + errorResponse.get("error");
                            }
                        } catch (Exception e) {
                            // Fallback if JSON parsing fails
                            errorMessage += ": " + responseBody;
                        }
                    }

                    String finalErrorMessage = errorMessage;
                    Platform.runLater(() -> showError(finalErrorMessage));
                }
                response.close(); // Ensure response is closed to avoid leaks
            }
        });
    }

    // Send range data to the server
    public static void addRange(String sheetName, String rangeName, String rangeDefinition) {
        Request request = new Request.Builder()
                .url("http://localhost:8080/webapp/addRange")
                .post(new FormBody.Builder()
                        .add("sheetName", sheetName) // The sheet name
                        .add("rangeName", rangeName) // The new range name
                        .add("rangeDefinition", rangeDefinition) // The range definition
                        .build())
                .build();

        // Run the HTTP request asynchronously
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to add range: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() ->UIHelper.showAlert("Success", "Range added successfully."));
                } else {
                    String responseBody = null;
                    if (response.body() != null) {
                        // Read the response body once
                        responseBody = response.body().string();
                    }
                    String errorMessage = "Failed to add range";
                    if (responseBody != null && !responseBody.isEmpty()) {
                        try {
                            // Try to parse the response body as JSON and extract the error message
                            Map<String, String> errorResponse = gson.fromJson(responseBody, new TypeToken<Map<String, String>>(){}.getType());
                            if (errorResponse.containsKey("error")) {
                                errorMessage += ": " + errorResponse.get("error");
                            }
                        } catch (Exception e) {
                            // Fallback if JSON parsing fails
                            errorMessage += ": " + responseBody;
                        }
                    }

                    String finalErrorMessage = errorMessage;
                    Platform.runLater(() -> showError(finalErrorMessage));
                }
                response.close(); // Ensure the response is closed to prevent leaks
            }
        });
    }
}
