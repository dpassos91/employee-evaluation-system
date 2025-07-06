package aor.projetofinal.bean;

import aor.projetofinal.dao.ProfileDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.FlatProfileDto;
import aor.projetofinal.dto.PaginatedProfilesDto;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.util.PasswordUtil;
import aor.projetofinal.util.ProfileValidator;
import aor.projetofinal.util.StringUtils;
import aor.projetofinal.context.RequestContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless EJB bean for profile business logic.
 * Handles profile creation, update, conversion, filtering and password changes.
 */
@Stateless
public class ProfileBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(ProfileBean.class);

    @Inject
    JavaConversionUtil javaConversionUtil;

    @Inject
    private ProfileDao profileDao;

    @Inject
    private UserDao userDao;

    /**
     * Updates the user's profile photograph.
     *
     * @param currentProfile     The UserEntity whose photograph is to be updated.
     * @param photographUpdated  The new photograph (URL, path, or identifier).
     * @return true if updated successfully, false if the user is null.
     */
    public boolean changePhotographOnProfile(UserEntity currentProfile, String photographUpdated) {
        String email = currentProfile != null ? currentProfile.getEmail() : "unknown";
        if (currentProfile == null) {
            logger.warn("User: {} | IP: {} | Email: {} - Attempted to update photograph for null user. Operation aborted.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return false;
        }
        currentProfile.getProfile().setPhotograph(photographUpdated);
        userDao.save(currentProfile);
        logger.info("User: {} | IP: {} | Email: {} - Successfully updated profile photograph.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return true;
    }

    /**
     * Converts a ProfileEntity to its corresponding ProfileDto representation.
     *
     * @param entity The ProfileEntity to convert.
     * @return A ProfileDto representing the given entity, or null if the entity is null.
     */
    public ProfileDto convertToDto(ProfileEntity entity) {
        if (entity == null) {
            logger.warn("User: {} | IP: {} - Attempted to convert a null ProfileEntity to DTO. Operation aborted.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return null;
        }
        logger.info("User: {} | IP: {} - Converting ProfileEntity to ProfileDto.",
                RequestContext.getAuthor(), RequestContext.getIp());

        ProfileDto dto = new ProfileDto();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setPhotograph(entity.getPhotograph());
        dto.setBio(entity.getBio());
        dto.setProfileComplete(ProfileValidator.isProfileComplete(entity));
        dto.setMissingFields(ProfileValidator.getMissingFields(entity));
        dto.setBirthDate(entity.getBirthDate() != null ? entity.getBirthDate() : null);
        dto.setUsualWorkplace(entity.getUsualWorkplace() != null ? entity.getUsualWorkplace().name() : null);
        return dto;
    }

    /**
     * Finds user profiles based on optional filters: employee name, workplace, and manager email.
     *
     * @param employeeName Partial or full name of the employee to filter (can be null).
     * @param workplace    Usual workplace to filter (can be null).
     * @param managerEmail Email of the manager to filter (can be null).
     * @return A list of FlatProfileDto objects matching the provided filters.
     */
    public ArrayList<FlatProfileDto> findProfilesWithFilters(String employeeName, UsualWorkPlaceType workplace, String managerEmail) {
        logger.info("User: {} | IP: {} | Filters -> Employee Name: {}, Workplace: {}, Manager Email: {} - Searching profiles with filters.",
                RequestContext.getAuthor(), RequestContext.getIp(),
                employeeName != null ? employeeName : "none",
                workplace != null ? workplace.name() : "none",
                managerEmail != null ? managerEmail : "none");
        List<ProfileEntity> profilesDB = profileDao.findProfilesWithFilters(employeeName, workplace, managerEmail);
        return new ArrayList<>(JavaConversionUtil.convertProfileEntityListToFlatProfileDtoList(profilesDB));
    }

    /**
     * Retrieves a paginated list of user profiles based on optional filters: employee name, workplace, and manager email.
     *
     * @param employeeName Partial or full name of the employee to filter (can be null).
     * @param workplace    Usual workplace to filter (can be null).
     * @param managerEmail Email of the manager to filter (can be null).
     * @param page         The page number requested for pagination.
     * @return A PaginatedProfilesDto containing the filtered profiles, total count, total pages, and current page.
     */
    public PaginatedProfilesDto findProfilesWithFiltersPaginated(String employeeName, UsualWorkPlaceType workplace, String managerEmail, int page) {
        logger.info("User: {} | IP: {} | Filters -> Employee Name: {}, Workplace: {}, Manager Email: {} | Page: {} - Searching paginated profiles with filters.",
                RequestContext.getAuthor(), RequestContext.getIp(),
                employeeName != null ? employeeName : "none",
                workplace != null ? workplace.name() : "none",
                managerEmail != null ? managerEmail : "none",
                page);
        List<ProfileEntity> profilesDB = profileDao.findProfilesWithFiltersPaginated(employeeName, workplace, managerEmail, page);
        long totalCount = profileDao.countProfilesWithFilters(employeeName, workplace, managerEmail);
        List<FlatProfileDto> profileDtos = JavaConversionUtil.convertProfileEntityListToFlatProfileDtoList(profilesDB);
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return new PaginatedProfilesDto(profileDtos, totalCount, totalPages, page);
    }


    /**
     * Updates the profile information of a user identified by their email.
     *
     * @param profileDto The ProfileDto object containing the new profile data.
     * @param email      The email of the user whose profile is to be updated.
     * @return true if the profile was successfully updated or created; false otherwise.
     */
    public boolean updateProfile(ProfileDto profileDto, String email) {
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
        profileToUpdate.setNormalizedFirstName(StringUtils.normalize(profileDto.getFirstName()));
        profileToUpdate.setNormalizedLastName(StringUtils.normalize(profileDto.getLastName()));
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
