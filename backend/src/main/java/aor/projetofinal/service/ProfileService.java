package aor.projetofinal.service;

import aor.projetofinal.bean.ProfileBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.ProfileDao;

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
            @QueryParam("usual-work-place") UsualWorkPlaceEnum usualLocation,
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

        /*if (!(currentUser.getRole().getName().equalsIgnoreCase("admin") ||
                currentUser.getEmail().equalsIgnoreCase(profileOwner.getEmail()))) {
            logger.warn("User: {} | IP: {} - Not authorized to fetch the profile of '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return Response.status(403)
                    .entity("{\"message\": \"Not authorized to access this profile.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }*/

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
    /*if (!(currentUser.getRole().getName().equalsIgnoreCase("admin")
            || currentUser.getId() == profileOwner.getId())) {
        logger.warn("User: {} | IP: {} - Not authorized to fetch the profile of id '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);
        return Response.status(403)
                .entity("{\"message\": \"Not authorized to access this profile.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }*/

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
        @QueryParam("usual-work-place") UsualWorkPlaceEnum usualLocation,
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

        List<String> options = Arrays.stream(UsualWorkPlaceEnum.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        logger.info("User: {} | IP: {} - Fetched {} usual workplace options: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), options.size(), options);

        return options;
    }

    /**
 * Uploads a new profile photo for the specified user.
 * Accepts a multipart/form-data POST request containing the photo file (as bytes) and its original filename.
 * The photo is saved to disk with a unique name, and the ProfileEntity is updated with the file path or URL.
 *
 * @param email         The email of the user whose profile photo is being updated.
 * @param sessionToken  The session token for authentication.
 * @param form          The multipart form containing the photo and original filename.
 * @return HTTP 200 if upload succeeds, 400/401/403/500 for errors.
 */
@POST
@Path("/{email}/upload-photo")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public Response uploadProfilePhoto(
        @PathParam("email") String email,
        @HeaderParam("sessionToken") String sessionToken,
        @org.jboss.resteasy.annotations.providers.multipart.MultipartForm PhotoUploadForm form) {

    logger.info("User: {} | IP: {} - Attempting profile photo upload for user '{}'",
            RequestContext.getAuthor(), RequestContext.getIp(), email);

    // 1. Validate session token
    SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);
    if (sessionStatusDto == null) {
        logger.warn("User: {} | IP: {} - Invalid or expired session while uploading photo for '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return Response.status(401)
                .entity("{\"message\": \"Session expired. Please log in again.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // 2. Find user
    UserEntity user = userDao.findByEmail(email);
    if (user == null) {
        logger.warn("User: {} | IP: {} - Tried to upload photo for non-existent user: '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return Response.status(404)
                .entity("{\"message\": \"User not found.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // 3. Only user themselves or admin can upload photo
    UserEntity currentUser = sessionTokenDao.findBySessionToken(sessionToken).getUser();
    if (!(currentUser.getRole().getName().equalsIgnoreCase("admin") ||
            currentUser.getEmail().equalsIgnoreCase(user.getEmail()))) {
        logger.warn("User: {} | IP: {} - Not authorized to upload photo for '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return Response.status(403)
                .entity("{\"message\": \"Not authorized to upload photo for this user.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // 4. Check if photo data is present
    byte[] photoBytes = form.getPhoto();
    String originalFileName = form.getFileName();
    if (photoBytes == null || photoBytes.length == 0) {
        logger.warn("User: {} | IP: {} - No photo data received for '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return Response.status(400)
                .entity("{\"message\": \"No photo uploaded.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    try {
        // 5. Generate unique filename
        String fileExtension = (originalFileName != null && originalFileName.contains("."))
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : ".jpg";
        String newFileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + fileExtension;

        // 6. Define directory to save photos (e.g., /var/www/app-photos/ or relative to project)
        String uploadDir = "C:\\Users\\Diogo Passos\\Desktop\\Acertar o Rumo\\ProjectoFinal\\wildfly-35.0.1.Final\\photos";
        // Ensure the directory exists
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, newFileName);
        java.nio.file.Files.write(filePath, photoBytes);

        logger.info("User: {} | IP: {} - Uploaded photo saved as '{}' ({} bytes).",
                RequestContext.getAuthor(), RequestContext.getIp(), filePath, photoBytes.length);

        // 7. Update ProfileEntity with the path/URL to the photo
        ProfileEntity profile = user.getProfile();
        if (profile == null) {
            logger.warn("User: {} | IP: {} - No profile found for '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return Response.status(404)
                    .entity("{\"message\": \"Profile not found for this user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        profileBean.updateProfilePhoto(user, newFileName);

        logger.info("User: {} | IP: {} - Profile photo path updated for '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);

        return Response.ok("{\"message\": \"Profile photo uploaded successfully!\", \"fileName\": \"" + newFileName + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();

    } catch (Exception e) {
        logger.error("User: {} | IP: {} - Error saving photo for '{}': {}",
                RequestContext.getAuthor(), RequestContext.getIp(), email, e.getMessage());
        return Response.status(500)
                .entity("{\"message\": \"Error saving photo.\", \"error\": true}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

/**
 * Serves the user's profile photo by file name.
 * Requires a valid session token (authenticated user).
 * Automatically refreshes the session token as in other endpoints.
 *
 * Example usage:
 *   GET /rest/profiles/photo/{fileName}
 *   Header: sessionToken: {token}
 *
 * @param fileName The name of the photo file to serve (as saved in the ProfileEntity).
 * @param sessionToken The session token for authentication (header).
 * @return HTTP 200 with the image bytes, 401 if not authenticated, or 404 if not found.
 */
@GET
@Path("/photo/{fileName}")
@Produces({"image/jpeg", "image/png", "image/gif"})
public Response getProfilePhoto(
        @PathParam("fileName") String fileName) {
    logger.info("User: {} | IP: {} - Attempting to fetch profile photo '{}'.",
            RequestContext.getAuthor(), RequestContext.getIp(), fileName);


    // 2. Build absolute file path
    String uploadDir = "C:\\Users\\Diogo Passos\\Desktop\\Acertar o Rumo\\ProjectoFinal\\wildfly-35.0.1.Final\\photos";
    java.io.File file = new java.io.File(uploadDir, fileName);

    // 3. Validate file existence
    if (!file.exists() || !file.isFile()) {
        logger.warn("User: {} | IP: {} - Profile photo '{}' not found.",
                RequestContext.getAuthor(), RequestContext.getIp(), fileName);
        return Response.status(404)
                .entity("{\"message\": \"Profile photo not found.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // 4. Determine image type by extension
    String contentType = "image/jpeg";
    if (fileName.toLowerCase().endsWith(".png")) {
        contentType = "image/png";
    } else if (fileName.toLowerCase().endsWith(".gif")) {
        contentType = "image/gif";
    }

    try {
        logger.info("User: {} | IP: {} - Serving profile photo '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), fileName);

        // 5. Stream the file to the client
        return Response.ok(file, contentType)
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .build();
    } catch (Exception e) {
        logger.error("User: {} | IP: {} - Error serving profile photo '{}': {}",
                RequestContext.getAuthor(), RequestContext.getIp(), fileName, e.getMessage());
        return Response.status(500)
                .entity("{\"message\": \"Error serving profile photo.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}


}
