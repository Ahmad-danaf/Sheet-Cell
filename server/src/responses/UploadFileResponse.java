package responses;

public class UploadFileResponse {
    private String status;
    private String fileName;
    private String fileContent;

    // Constructor
    public UploadFileResponse(String status, String fileName, String fileContent) {
        this.status = status;
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}
