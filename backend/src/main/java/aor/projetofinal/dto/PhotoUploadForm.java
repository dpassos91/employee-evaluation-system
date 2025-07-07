package aor.projetofinal.dto;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import jakarta.ws.rs.FormParam;

/**
 * DTO for uploading a photo with a filename.
 * Used in multipart/form-data requests.
 */
public class PhotoUploadForm {
    @FormParam("photo")
    @PartType("application/octet-stream")
    private byte[] photo;

    @FormParam("fileName")
    @PartType("text/plain")
    private String fileName;

    // Getters and setters
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}

