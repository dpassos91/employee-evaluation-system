package aor.projetofinal.dto;


import aor.projetofinal.entity.UserEntity;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;


public class ProfileDto {
    private UserEntity user;
    private String firstName;
    private String lastName;
    
    @JsonFormat(pattern = "yyyy-MM-dd") // Ensures the date is formatted correctly when serialized
    private LocalDate birthDate;
    
    private String address;
    private String phone;
    private String photograph;
    private String bio;
    private String usualWorkplace;
    private boolean profileComplete;
    private List<String> missingFields;

    public ProfileDto() {
    }


    public UserEntity getUser() {
        return user;
    }


    public void setUser(UserEntity user) {
        this.user = user;
    }

    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
   
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
  
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPhotograph() {
        return photograph;
    }

    public void setPhotograph(String photograph) {
        this.photograph = photograph;
    }
    
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getUsualWorkplace() {
        return usualWorkplace;
    }

    public void setUsualWorkplace(String usualWorkplace) {
        this.usualWorkplace = usualWorkplace;
    }

    public boolean isProfileComplete() { return profileComplete; }
public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }
public List<String> getMissingFields() { return missingFields; }
public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
}
