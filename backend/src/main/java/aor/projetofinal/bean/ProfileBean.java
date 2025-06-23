package aor.projetofinal.bean;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;

import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dao.ProfileDao;

import java.io.Serializable;

@Stateless
public class ProfileBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(ProfileBean.class);

    @Inject
    private UserDao userDao;

    @Inject
    private ProfileDao profileDao;

    public boolean updateInfo(ProfileDto profileDto, String email) {

    UserEntity user = userDao.findByEmail(email);

    if (user == null) {
        logger.warn("User: {} | IP: {} | Email: {} - Could not find user to update.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return false;
    }

    ProfileEntity profileToUpdate = user.getProfile();

    if (profileToUpdate == null) {
        profileToUpdate = new ProfileEntity();
        profileToUpdate.setUser(user);
        user.setProfile(profileToUpdate);
        logger.info("User: {} | IP: {} | Email: {} - Created new profile for user.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
    }

    // Required fields
    profileToUpdate.setFirstName(profileDto.getFirstName());
    profileToUpdate.setLastName(profileDto.getLastName());
    profileToUpdate.setBirthDate(profileDto.getBirthDate());
    profileToUpdate.setAddress(profileDto.getAddress());
    profileToUpdate.setPhone(profileDto.getPhone());

    // Optional fields
    profileToUpdate.setPhotograph(profileDto.getPhotograph());
    profileToUpdate.setBio(profileDto.getBio());

    // Conversion and validation for usualWorkplace (Enum)
    String usualWorkplaceString = profileDto.getUsualWorkplace();
    if (usualWorkplaceString != null && !usualWorkplaceString.isBlank()) {
        try {
            UsualWorkPlaceType type = UsualWorkPlaceType.valueOf(usualWorkplaceString.toUpperCase());
            profileToUpdate.setUsualWorkplace(type);
        } catch (IllegalArgumentException e) {
            logger.warn("User: {} | IP: {} | Email: {} | Value: {} - Invalid usualWorkplace value received.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email, usualWorkplaceString);
            return false;
        }
    } else {
        logger.warn("User: {} | IP: {} | Email: {} - Missing required usualWorkplace field.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return false;
    }

    profileDao.save(profileToUpdate);

    logger.info("User: {} | IP: {} | Email: {} - Successfully updated profile for user.",
            RequestContext.getAuthor(), RequestContext.getIp(), email);

    return true;
}





}
