package aor.projetofinal.bean;

import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.dto.PaginatedProfilesDto;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import aor.projetofinal.util.ProfileValidator;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dao.ProfileDao;
import aor.projetofinal.util.PasswordUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ProfileBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(ProfileBean.class);

    @Inject
    JavaConversionUtil javaConversionUtil;


    @Inject
    private UserDao userDao;

    @Inject
    private ProfileDao profileDao;

    /**
 * Updates the user's profile photograph.
 * Logs both successful and failed attempts, including user, IP, and email for audit purposes.
 *
 * @param currentProfile The UserEntity whose photograph is to be updated.
 * @param photographUpdated The new photograph (URL, path or identifier).
 * @return true if updated successfully, false if the user is null.
 */
public boolean changePhotographOnProfile(UserEntity currentProfile, String photographUpdated) {
    // Extract email for logging; default to "unknown" if user is null
    String email = currentProfile != null ? currentProfile.getEmail() : "unknown";

    // If the user entity is null, log warning and abort operation
    if (currentProfile == null) {
        logger.warn(
                "User: {} | IP: {} | Email: {} - Attempted to update photograph for null user. Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );
        return false;
    }

    // Update profile photograph and persist changes
    currentProfile.getProfile().setPhotograph(photographUpdated);
    userDao.save(currentProfile);

    // Log successful update
    logger.info(
            "User: {} | IP: {} | Email: {} - Successfully updated profile photograph.",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            email
    );
    return true;
}

    /**
     * Converts a ProfileEntity object to its corresponding ProfileDto representation.
     * Logs the conversion attempt and warns if the input entity is null.
     *
     * @param entity The ProfileEntity to convert.
     * @return A ProfileDto representing the given entity, or null if the entity is null.
     */
    public ProfileDto convertToDto(ProfileEntity entity) {
        if (entity == null) {
            logger.warn(
                    "User: {} | IP: {} - Attempted to convert a null ProfileEntity to DTO. Operation aborted.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return null;
        }

        logger.info(
                "User: {} | IP: {} - Converting ProfileEntity to ProfileDto.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );


        ProfileDto dto = new ProfileDto();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setPhotograph(entity.getPhotograph());
        dto.setBio(entity.getBio());
        dto.setProfileComplete(ProfileValidator.isProfileComplete(entity));
        dto.setMissingFields(ProfileValidator.getMissingFields(entity));

        // birthDate can be declared as null
        if (entity.getBirthDate() != null) {
            dto.setBirthDate(entity.getBirthDate()); // YYYY-MM-DD structure
        } else {
            dto.setBirthDate(null);
        }

        if (entity.getUsualWorkplace() != null) {
            dto.setUsualWorkplace(entity.getUsualWorkplace().name()); // returns usual worplace in capitals
        } else {
            dto.setUsualWorkplace(null);
        }

        return dto;
    }

    /**
     * Finds user profiles based on optional filters: employee name, workplace, and manager email.
     * Logs the search action including user, IP, and filter parameters for audit purposes.
     *
     * @param employeeName     Partial or full name of the employee to filter (can be null).
     * @param workplace        Usual workplace to filter (can be null).
     * @param managerEmail     Email of the manager to filter (can be null).
     * @return A list of ProfileDto objects matching the provided filters.
     */
    public ArrayList<ProfileDto> findProfilesWithFilters(String employeeName, UsualWorkPlaceType workplace, String managerEmail) {

        logger.info(
                "User: {} | IP: {} | Filters -> Employee Name: {}, Workplace: {}, Manager Email: {} - Searching profiles with filters.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                employeeName != null ? employeeName : "none",
                workplace != null ? workplace.name() : "none",
                managerEmail != null ? managerEmail : "none"
        );



        List<ProfileEntity> profilesDB = profileDao.findProfilesWithFilters(employeeName, workplace, managerEmail);

        return javaConversionUtil.convertProfileEntityListtoProfileDtoList(new ArrayList<>(profilesDB));
    }

    /**
     * Retrieves a paginated list of user profiles based on optional filters: employee name, workplace, and manager email.
     * Logs the search request including user, IP, email, filter values, and requested page for audit purposes.
     *
     * @param employeeName  Partial or full name of the employee to filter (can be null).
     * @param workplace     Usual workplace to filter (can be null).
     * @param managerEmail  Email of the manager to filter (can be null).
     * @param page          The page number requested for pagination.
     * @return A PaginatedProfilesDto containing the filtered profiles, total count, total pages, and current page.
     */
    public PaginatedProfilesDto findProfilesWithFiltersPaginated(String employeeName, UsualWorkPlaceType workplace, String managerEmail, int page) {

        logger.info(
                "User: {} | IP: {} | Filters -> Employee Name: {}, Workplace: {}, Manager Email: {} | Page: {} - Searching paginated profiles with filters.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                employeeName != null ? employeeName : "none",
                workplace != null ? workplace.name() : "none",
                managerEmail != null ? managerEmail : "none",
                page
        );

        List<ProfileEntity> profilesDB = profileDao.findProfilesWithFiltersPaginated(employeeName, workplace, managerEmail, page);
        //Count the total number of profiles filtered
        long totalCount = profileDao.countProfilesWithFilters(employeeName, workplace, managerEmail);

        //Convert results to Dto
        List<ProfileDto> profileDtos = javaConversionUtil.convertProfileEntityListtoProfileDtoList(new ArrayList<>(profilesDB));

        //Calculate the total number of pages
        int pageSize = 10; // default number for now
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // Building a Dto to send as response already including pagination
        return new PaginatedProfilesDto(profileDtos, totalCount, totalPages, page);
    }

    /**
     * Resets the password of the given user profile.
     * Logs both successful and failed reset attempts, including user and IP, for audit purposes.
     *
     * @param currentProfile The UserEntity whose password is to be reset.
     * @param newPassword    The new plain text password to be hashed and stored.
     * @return true if the password was reset successfully, false if the user is null.
     */
    public boolean resetPasswordOnProfile(UserEntity currentProfile, String newPassword) {
        // Extract email for logging; default to "unknown" if user is null
        String email = currentProfile != null ? currentProfile.getEmail() : "unknown";

        if (currentProfile == null) {
            logger.warn(
                    "User: {} | IP: {} | Email: {} - Attempted to reset password for null user. Operation aborted.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
                                );

            return false;
        }

        // Hash for the new password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        currentProfile.setPassword(hashedPassword);  // Store hashed password

        userDao.save(currentProfile);  // Save changes on database

        logger.info(
                "User: {} | IP: {} | Email: {} - Successfully reset password.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );

        return true;
    }

    /**
 * Updates the profile information of a user identified by their email.
 * <p>
 * This method attempts to retrieve the {@link UserEntity} by the provided email.
 * If the user exists, it updates the associated {@link ProfileEntity} with the
 * information provided in the {@link ProfileDto}. If the profile does not exist,
 * a new one is created and linked to the user.
 * <p>
 * Required fields such as first name, last name, birth date, address, phone, and
 * usualWorkplace are validated and set. The usualWorkplace field must match one
 * of the valid {@link UsualWorkPlaceType} enum values (case-insensitive).
 * Optional fields (photograph, bio) are updated if provided.
 * <p>
 * All operations are logged for auditing purposes. If any validation fails, or the
 * user is not found, the method returns {@code false}.
 *
 * @param profileDto The {@link ProfileDto} object containing the new profile data.
 * @param email      The email of the user whose profile is to be updated.
 * @return {@code true} if the profile was successfully updated or created;
 *         {@code false} otherwise (e.g., user not found, invalid usualWorkplace).
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
