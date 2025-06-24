package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import aor.projetofinal.entity.enums.UsualWorkPlaceType;


@Entity
@Table(name = "profiles")
public class ProfileEntity implements Serializable  {

    private static final long serialVersionUID = 1L;

    @Id
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
    private UserEntity user;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "normalized_first_name")
    private String normalizedFirstName;


    @Column(name = "normalized_last_name")
    private String normalizedLastName;


    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "photograph")
    private String photograph;

    @Column (name="bio", length = 65535, columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "usual_work_place")
    private UsualWorkPlaceType usualWorkplace;

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

    public String getNormalizedFirstName() {
        return normalizedFirstName;
    }

    public void setNormalizedFirstName(String normalizedFirstName) {
        this.normalizedFirstName = normalizedFirstName;
    }

    public String getNormalizedLastName() {
        return normalizedLastName;
    }

    public void setNormalizedLastName(String normalizedLastName) {
        this.normalizedLastName = normalizedLastName;
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

    public UsualWorkPlaceType getUsualWorkplace() {
    return usualWorkplace;
    }

    public void setUsualWorkplace(UsualWorkPlaceType usualWorkplace) {
    this.usualWorkplace = usualWorkplace;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileEntity that = (ProfileEntity) o;
        return Objects.equals(user, that.user);
    }

    // hash
    @Override
    public int hashCode() {
        return Objects.hash(user);
    }

    // toString
    @Override
    public String toString() {
        return "ProfileEntity{" +
                "userId=" + (user != null ? user.getId() : null) +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", photograph='" + photograph + '\'' +
                ", bio='" + (bio != null ? bio.substring(0, Math.min(20, bio.length())) + "..." : "") + '\'' +
                ", usualWorkplace='" + (usualWorkplace != null ? usualWorkplace.name() : null) + '\'' +
                '}';
    }

}
