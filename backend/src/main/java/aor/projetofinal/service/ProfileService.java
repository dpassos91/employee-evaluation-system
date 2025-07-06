package aor.projetofinal.service;

import aor.projetofinal.bean.ProfileBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.context.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/profiles")
public class ProfileService {

    private static final Logger logger = LogManager.getLogger(ProfileService.class);

    @Inject
    UserBean userBean;

    @Inject
    private UserDao userDao;

    @Inject
    private SessionTokenDao sessionTokenDao;

    @Inject
    private ProfileBean profileBean;

    /**
     * Exports the list of users filtered by parameters to a CSV file.
     * Returns a CSV as attachment, with user information including name, workplace, manager, contact, and photograph.
     *
     * @param sessionToken   The session token for authentication (header).
     * @param profileName    (Optional) Filter by profile name.
     * @param usualLocation  (Optional) Filter by usual workplace (enum).
     * @param managerEmail   (Optional) Filter by manager's email.
     * @return HTTP 200 with the CSV content, or error if not authorized.
     */
    @GET
    @Path("/export-users-csv")
    @Produces("text/csv")
    public Response exportUsersToCSV(
            @HeaderParam("sessionToken") String sessionToken,
            @QueryParam("profile-name") String profileName,
            @QueryParam("usual-work-place") UsualWorkPlaceType usualLocation,
            @QueryParam("manager-email") String managerEmail
    ) {
        // 1. Validate session
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(sessionToken);
        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Session expired or invalid.")
                    .build();
        }

// 2. Get filtered profiles
ArrayList<FlatProfileDto> profiles = profileBean.findProfilesWithFilters(
        profileName, usualLocation, managerEmail);

// 3. Build CSV content using the util
String csv = JavaConversionUtil.buildUsersCsvFromFlatProfiles(profiles);

// 4. Return as CSV attachment
return Response.ok(csv)
        .header("Content-Disposition", "attachment; filename=users_export.csv")
        .type("text/csv")
        .build();
    }

    /**
     * Retrieves a user profile by email.
     * Only the user themselves or an admin can access the profile.
     *
     * @param email         The email of the profile to retrieve.
     * @param sessionToken  The session token for authentication (header).
     * @return HTTP 200 with the ProfileDto, or error if not authorized.
     */
    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@PathParam("email") String email, @HeaderParam("sessionToken") String sessionToken) {
        // Validate session
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);
        if (sessionStatusDto == null) {
            logger.warn("User: {} | IP: {} - Invalid or expired session while fetching profile '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return Response.status(401)
                    .entity("{\"message\": \"Session expired. Please log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Only the user themselves or an admin can access the profile
        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        UserEntity currentUser = sessionTokenEntity.getUser();
        UserEntity profileOwner = userDao.findByEmail(email);

        if (profileOwner == null) {
            logger.warn("User: {} | IP: {} - Tried to fetch non-existent user profile: '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return Response.status(404)
                    .entity("{\"message\": \"User not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!(currentUser.getRole().getName().equalsIgnoreCase("admin") ||
                currentUser.getEmail().equalsIgnoreCase(profileOwner.getEmail()))) {
            logger.warn("User: {} | IP: {} - Not authorized to fetch the profile of '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return Response.status(403)
                    .entity("{\"message\": \"Not authorized to access this profile.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Get the associated ProfileEntity
        var profileEntity = profileOwner.getProfile();
        if (profileEntity == null) {
            logger.warn("User: {} | IP: {} - No profile found for user '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return Response.status(404)
                    .entity("{\"message\": \"Profile not found for this user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Convert to DTO
        ProfileDto profileDto = profileBean.convertToDto(profileEntity);

        logger.info("User: {} | IP: {} - Successfully fetched profile of '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);

        return Response.ok(profileDto).build();
    }

    /**
 * Retrieves a user's profile by their unique user ID.
 * Only accessible by the user themselves or by an admin.
 *
 * @param userId         The unique ID of the user (from the URL path).
 * @param sessionToken   The session token for authentication (from the request header).
 * @return HTTP 200 with the ProfileDto if authorized, or an appropriate error response otherwise.
 */
@GET
@Path("/by-id/{userId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getProfileById(
        @PathParam("userId") int userId,
        @HeaderParam("sessionToken") String sessionToken
) {
    // Validate and refresh the session token
    SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);
    if (sessionStatusDto == null) {
        logger.warn("User: {} | IP: {} - Invalid or expired session while fetching profile by id '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);
        return Response.status(401)
                .entity("{\"message\": \"Session expired. Please log in again.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Get current user (by token) and profile owner (by id)
    SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
    UserEntity currentUser = sessionTokenEntity.getUser();
    UserEntity profileOwner = userDao.findById(userId);

    if (profileOwner == null) {
        logger.warn("User: {} | IP: {} - Tried to fetch non-existent user profile by id: '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);
        return Response.status(404)
                .entity("{\"message\": \"User not found.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Only admin or the profile owner can access the profile
    if (!(currentUser.getRole().getName().equalsIgnoreCase("admin")
            || currentUser.getId() == profileOwner.getId())) {
        logger.warn("User: {} | IP: {} - Not authorized to fetch the profile of id '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);
        return Response.status(403)
                .entity("{\"message\": \"Not authorized to access this profile.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Get the associated ProfileEntity
    ProfileEntity profileEntity = profileOwner.getProfile();
    if (profileEntity == null) {
        logger.warn("User: {} | IP: {} - No profile found for user id '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);
        return Response.status(404)
                .entity("{\"message\": \"Profile not found for this user.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Convert ProfileEntity to ProfileDto
    ProfileDto profileDto = profileBean.convertToDto(profileEntity);

    logger.info("User: {} | IP: {} - Successfully fetched profile of user id '{}'.",
            RequestContext.getAuthor(), RequestContext.getIp(), userId);

    // Success
    return Response.ok(profileDto).build();
}


    /**
     * Retrieves a paginated list of user profiles filtered by name, workplace, or manager.
     * Returns only flat DTOs (FlatProfileDto) for API safety.
     *
     * @param sessionToken   The session token for authentication (header).
     * @param profileName    (Optional) Filter by profile name.
     * @param usualLocation  (Optional) Filter by usual workplace (enum).
     * @param managerEmail   (Optional) Filter by manager's email.
     * @param page           The page number (pagination, default = 1).
     * @return HTTP 200 with a PaginatedProfilesDto containing FlatProfileDto list, or error if not authorized.
     */
@GET
@Path("/list-users-by-filters")
@Produces(MediaType.APPLICATION_JSON)
public Response listUsersPaginated(
        @HeaderParam("sessionToken") String sessionToken,
        @QueryParam("profile-name") String profileName,
        @QueryParam("usual-work-place") UsualWorkPlaceType usualLocation,
        @QueryParam("manager-email") String managerEmail,
        @QueryParam("page") @DefaultValue("1") int page
) {
    SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);
    if (sessionStatusDto == null) {
        logger.warn("Invalid or expired session - list users");
        return Response.status(401)
                .entity("{\"message\": \"Session expired. Please log in again.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    PaginatedProfilesDto paginatedResult = profileBean.findProfilesWithFiltersPaginated(
            profileName, usualLocation, managerEmail, page);

    return Response.ok(paginatedResult)
            .type(MediaType.APPLICATION_JSON)
            .build();
}


    /**
     * Updates the user's profile photograph.
     * Only the user themselves or admin can update the photograph.
     *
     * @param sessionToken       The session token for authentication (header).
     * @param email              The email of the user whose photograph is to be updated.
     * @param photographUpdated  The new photograph data (URL or identifier).
     * @return HTTP 200 if updated, or error if not authorized.
     */
    @PATCH
    @Path("/update/{email}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserPhotograph(@HeaderParam("sessionToken") String sessionToken, @PathParam("email") String email,
                                         PhotographDto photographUpdated) {

        // Validate session
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Invalid or expired session - update photograph");
            return Response.status(401)
                    .entity("{\"message\": \"Session expired. Please log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        UserEntity currentUserLoggedIn = sessionTokenEntity.getUser();
        UserEntity currentProfile = userDao.findByEmail(email);

        // Authorization: only admin or the user themselves
        if (!(currentUserLoggedIn.getRole().getName()).equalsIgnoreCase("admin") &&
                !(currentUserLoggedIn.getEmail().equalsIgnoreCase(currentProfile.getEmail()))) {
            logger.warn("update user - not authorized");
            return Response.status(403)
                    .entity("{\"message\": \"You are not authorized to update this user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String photo = photographUpdated.getPhotograph();
        if (photo != null && photo.length() > 250) {
            logger.warn("Invalid photograph information");
            return Response.status(400)
                    .entity("{\"message\": \"Please, provide a photograph link with up to 250 characters.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = profileBean.changePhotographOnProfile(currentProfile, photo);

        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error updating profile's photograph. Please try again.\", \"error\": true}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok()
                .entity("{\"message\": \"Profile's photograph successfully updated!\", \"error\": false}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Updates the user's profile information.
     * Only the user themselves or admin can update the profile.
     *
     * @param sessionToken     The session token for authentication (header).
     * @param email            The email of the user whose profile is to be updated.
     * @param profileToUpdate  The ProfileDto containing updated profile data.
     * @return HTTP 200 if updated, or error if not authorized or invalid input.
     */
    @PUT
    @Path("/update/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(@HeaderParam("sessionToken") String sessionToken, @PathParam("email") String email,
                                 ProfileDto profileToUpdate) {

        // Validate session
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Invalid or expired session - update user");
            return Response.status(401)
                    .entity("{\"message\": \"Session expired. Please log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        UserEntity currentUserLoggedIn = sessionTokenEntity.getUser();
        UserEntity currentProfile = userDao.findByEmail(email);

        // Authorization: only admin or the user themselves
        if (!(currentUserLoggedIn.getRole().getName()).equalsIgnoreCase("admin") &&
                !(currentUserLoggedIn.getEmail().equalsIgnoreCase(currentProfile.getEmail()))) {
            logger.warn("update user - not authorized");
            return Response.status(403)
                    .entity("{\"message\": \"You are not authorized to update this user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String photo = profileToUpdate.getPhotograph();
        if (photo != null && photo.length() > 250) {
            logger.warn("Invalid photograph information");
            return Response.status(400)
                    .entity("{\"message\": \"Please, provide a photograph link with up to 250 characters.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String phone = profileToUpdate.getPhone();
        if (phone == null || !phone.matches("^[0-9-]+$")) {
            logger.warn("Invalid phone number");
            return Response.status(400)
                    .entity("{\"message\": \"Please, enter only digits and hyphens in the phone number.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!profileBean.updateProfile(profileToUpdate, email)) {
            logger.warn("Error updating user - update user");
            return Response.status(400)
                    .entity("{\"message\": \"Could not update user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("Successfully updated user: {}", email);
        return Response.status(200)
                .entity("{\"message\": \"Data update successful!\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Updates the user's password.
     * Only the user themselves can update their password.
     *
     * @param sessionToken       The session token for authentication (header).
     * @param email              The email of the user whose password is to be updated.
     * @param passwordAtualizado The new password in the ResetPasswordDto object.
     * @return HTTP 200 if updated, or error if not authorized or invalid input.
     */
    @PATCH
    @Path("/update/{email}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserPassword(@HeaderParam("sessionToken") String sessionToken, @PathParam("email") String email,
                                       ResetPasswordDto passwordAtualizado) {

        // Validate session
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Invalid or expired session - update user password");
            return Response.status(401)
                    .entity("{\"message\": \"Session expired. Please log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        UserEntity currentUserLoggedIn = sessionTokenEntity.getUser();
        UserEntity currentProfile = userDao.findByEmail(email);

        // Authorization: only the user themselves can update their password
        if (!(currentUserLoggedIn.getEmail()).equals(currentProfile.getEmail())) {
            logger.warn("update user - not authorized");
            return Response.status(403)
                    .entity("{\"message\": \"You are not authorized to update this user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String newPassword = passwordAtualizado.getPassword();

        if (newPassword.length() <= 5) {
            logger.warn("Password too short");
            return Response.status(400)
                    .entity("{\"message\": \"Password's length must be over 5 characters.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = profileBean.resetPasswordOnProfile(currentProfile, newPassword);

        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error redefining password. Please try again.\", \"error\": true}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok()
                .entity("{\"message\": \"Password successfully updated!\", \"error\": false}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Gets all possible values for usual workplace (enum) for filtering or forms.
     *
     * @return List of workplace options as strings.
     */
    @GET
    @Path("/usualworkplaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getUsualWorkplaceOptions() {
        logger.info("User: {} | IP: {} - Fetching usual workplace options",
                RequestContext.getAuthor(), RequestContext.getIp());

        List<String> options = Arrays.stream(UsualWorkPlaceType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        logger.info("User: {} | IP: {} - Fetched {} usual workplace options: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), options.size(), options);

        return options;
    }
}
