package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "profiles")
public class ProfileEntity implements Serializable  {

    @Id
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
    private UserEntity user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false, updatable = false)
    private LocalDateTime birthDate;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "photograph")
    private String photograph;

    @Column (name="bio", length = 65535, columnDefinition = "TEXT")
    private String bio;

    @Column(name = "usual_work_place")
    private String usualWorkplace;

    public ProfileEntity() {
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

    public LocalDateTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDate) {
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
}
