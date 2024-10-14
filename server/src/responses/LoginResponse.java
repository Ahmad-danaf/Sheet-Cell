package responses;

public class LoginResponse {
    private String message;
    private String username;
    private String error;

    public LoginResponse(String message, String username, String error) {
        this.message = message;
        this.username = username;
        this.error = error;
    }

    // Getters for Gson to serialize the fields
    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getError() {
        return error;
    }
}
