package aor.projetofinal.dto;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;



/**
 * DTO used for receiving login requests containing user credentials.
 * This object is typically populated from JSON or XML payloads.
 */
@XmlRootElement
public class LoginUserDto {
    private String email;
    private String password;

    public LoginUserDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginUserDto() {
    }

    @XmlElement
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}