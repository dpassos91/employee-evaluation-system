package aor.projetofinal.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * DTO representing a user's photograph encoded as a Base64 string.
 * Used for transferring image data between frontend and backend.
 */
@XmlRootElement
public class PhotographDto {
    private String photograph;

    public PhotographDto() {
    }

    @XmlElement
    public String getPhotograph() {
        return photograph;
    }

    public void setPhotograph(String photograph) {
        this.photograph = photograph;
    }



}
