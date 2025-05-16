package aor.projetofinal.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
    private String path;

    // Anotação para formatar o timestamp como string legível
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String ip;
    private String author;

    private Map<String, String> validationErrors;

    public ErrorResponseDto(int status, String error, String message, String path, String ip, String author) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.ip = ip;
        this.author = author;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIp() {return ip; }

    public void setIp(String ip) { this.ip = ip; }

    public String getAuthor() {return author; }

    public void setAuthor(String author) { this.author = author; }

    public Map<String, String> getValidationErrors() { return validationErrors; }

    public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
}
