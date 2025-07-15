package aor.projetofinal.service;

import aor.projetofinal.dto.*;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
import aor.projetofinal.util.EmailUtil;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.util.ProfileValidator;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;

import aor.projetofinal.entity.ProfileEntity;

import aor.projetofinal.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/users")
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Inject
    UserBean userBean;

    @Inject
    private SessionTokenDao sessionTokenDao;
    
    @Inject
    private UserDao userDao;

/**
     * Updates the user's password.
     * Only the user themselves can update their password.
     *
     * @param sessionToken       The session token for authentication (header).
     * @param email              The email of the user whose password is to be updated.
     * @param passwordAtualizado The new password in the ResetPasswordDto object.
     * @return HTTP 200 if updated, or error if not authorized or invalid input.
     */
    @PUT
@Path("/update/{email}/password")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
/**
 * Updates the user's password.
 * Only the user themselves can update their password and must provide the current password for validation.
 *
 * @param sessionToken The session token for authentication (header).
 * @param email The email of the user whose password is to be updated.
 * @param updatePasswordDto The DTO containing current and new passwords.
 * @return HTTP 200 if updated, or error if not authorized or invalid input.
 */
public Response updateUserPassword(
    @HeaderParam("sessionToken") String sessionToken,
    @PathParam("email") String email,
    UpdatePasswordDto updatePasswordDto) {

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

    String currentPassword = updatePasswordDto.getCurrentPassword();
    String newPassword = updatePasswordDto.getNewPassword();

    // Check password length
    if (newPassword == null || newPassword.length() <= 5) {
        logger.warn("Password too short");
        return Response.status(400)
            .entity("{\"message\": \"Password's length must be over 5 characters.\"}")
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    // Validate current password
    if (!userBean.isPasswordValid(currentProfile, currentPassword)) {
        logger.warn("Current password invalid for user '{}'", email);
        return Response.status(400)
            .entity("{\"message\": \"Current password is incorrect.\"}")
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    boolean success = userBean.resetPasswordOnProfile(currentProfile, newPassword);

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
     * Assigns a manager to a given user.
     * This action is restricted to admins only. If an evaluation cycle is active,
     * the evaluation for the user (if exists) is updated with the new manager as evaluator.
     *
     * @param dto   The DTO containing the user's email and the manager's email.
     * @param token The session token for authentication and authorization.
     * @return HTTP response with success or failure message.
     */
    @POST
    @Path("/assign-manager")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response assignManager(AssignManagerDto dto,
                                  @HeaderParam("sessionToken") String token) {

        logger.info("User: {} | IP: {} - Attempting to assign manager.",
                RequestContext.getAuthor(), RequestContext.getIp());


        // 1. Validate and refresh session token if close to expiration
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        //  Validate session token
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("User: {} | IP: {} - Unauthorized attempt to assign manager (invalid token).",
                    RequestContext.getAuthor(), RequestContext.getIp());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        //  Check admin role
        if (!requester.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden: non-admin tried to assign manager.",
                    requester.getEmail(), RequestContext.getIp());

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can assign managers.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        //  Prevent self-assignment
        if (dto.getUserEmail().equalsIgnoreCase(dto.getManagerEmail())) {
            logger.warn("User: {} | IP: {} - Attempted to assign user {} as their own manager.",
                    requester.getEmail(), RequestContext.getIp(), dto.getUserEmail());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"A user cannot be their own manager.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }





        //  Assign the manager
        boolean success = userBean.assignManagerToUser(dto.getUserEmail(), dto.getManagerEmail());

        if (!success) {
            logger.warn("User: {} | IP: {} - Failed to assign manager {} to user {}.",
                    requester.getEmail(), RequestContext.getIp(),
                    dto.getManagerEmail(), dto.getUserEmail());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Could not assign manager. Please check the provided emails or rules.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Successfully assigned manager {} to user {}.",
                requester.getEmail(), RequestContext.getIp(),
                dto.getManagerEmail(), dto.getUserEmail());

        return Response.status(Response.Status.OK)
                .entity("{\"message\": \"Manager successfully assigned.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }




    /**
     * Endpoint to check the current session status and return its expiration info.
     * Should be called periodically by the frontend to detect automatic logouts.
     *
     * @param token The session token from the client.
     * @return 200 with session info if valid, 401 if expired.
     */
    @GET
    @Path("/session-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkSessionStatus(@HeaderParam("sessionToken") String token) {
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);

        if (session == null || session.getExpiryDate() == null || session.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("User: unknown | IP: {} - Session token expired or not found during session status check.",
                    RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Sessão expirada.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Session is active. Expiry: {}",
                session.getUser().getEmail(), RequestContext.getIp(), session.getExpiryDate());

        SessionStatusDto dto = JavaConversionUtil.convertSessionTokenEntityToSessionStatusDto(session);
        return Response.ok(dto).build();
    }



    /**
     * Confirms a user account using the provided confirmation token.
     *
     * @param confirmToken The token sent to the user's email for confirmation.
     * @return HTTP response indicating success or failure of account confirmation.
     */
    @GET
    @Path("/confirmAccount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmAccount(@QueryParam("confirmToken") String confirmToken) {
        if (confirmToken == null || confirmToken.isEmpty()) {
            logger.warn("Attempted account confirmation with missing token. IP: {}", RequestContext.getIp());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Token de confirmação de conta em falta.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = userBean.confirmAccount(confirmToken);

        if (success) {
            logger.info("Account confirmed successfully using token. IP: {}", RequestContext.getIp());
            return Response.ok("{\"message\": \"Conta confirmada com sucesso! Já pode fazer login\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } else {
            logger.warn("Failed account confirmation with invalid or expired token. IP: {}", RequestContext.getIp());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Link inválido ou expirado. Foi gerado um novo Link de confirmação de conta." +
                            " Torne a aceder ao seu email registado" +
                            " para ter acesso ao novo link de confirmação de conta\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }




    /**
 * Registers a new user in the system and immediately sends an account confirmation email
 * with a unique confirmation token.
 *
 * @param loginUserDto The DTO containing the user's registration data (email, password, etc.).
 * @return HTTP 200 response with the created user data if successful.
 *
 * The method workflow is as follows:
 * 1. Calls the userBean to register the new user (persists in the database).
 * 2. Generates or retrieves a confirmation token for the user.
 * 3. Constructs the confirmation link using the generated token.
 * 4. Sends a confirmation email to the user with the link.
 * 5. Returns a 200 OK response with the user data.
 */
@POST
@Path("/createUser")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response createUser(LoginUserDto loginUserDto) {

    // Log the registration attempt
    logger.info("User: {} | IP: {} - Registration attempt for email: {}",
            RequestContext.getAuthor(), RequestContext.getIp(), loginUserDto.getEmail());

    // 1. Register the new user (persists in the database)
    UserDto createdUser = userBean.registerUser(loginUserDto);

    // 2. Generate or retrieve a confirmation token for the user
    String confirmToken = userBean.getConfirmToken(createdUser.getEmail());

    // 3. Build the confirmation link containing the token
    String confirmationLink = "https://localhost:8443/grupo7/rest/users/confirmAccount?confirmToken=" + confirmToken;

    // 4. Send the confirmation email with the link
    EmailUtil.sendEmail(
        createdUser.getEmail(),
        "Confirm your account",
        "Welcome! To confirm your account, please click this link: " + confirmationLink
    );

    // Log the successful registration
    logger.info("User: {} | IP: {} - Successfully registered user with email: {}",
            RequestContext.getAuthor(), RequestContext.getIp(), loginUserDto.getEmail());

    // 5. Return 200 OK with the created user data
    return Response.ok(createdUser).build();
}


    /**
     * Returns a list of active, confirmed non-admin users for the manager assignment dropdown menu.
     * Only accessible by users with ADMIN role.
     *
     * @param token The session token passed via the request header.
     * @return HTTP 200 with list of users or an appropriate error status.
     */
    @GET
    @Path("/manager-dropdown")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersForManagerDropdown(@HeaderParam("sessionToken") String token) {

        logger.info("User: {} | IP: {} - Requesting users for manager dropdown.",
                RequestContext.getAuthor(), RequestContext.getIp());

        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("User: {} | IP: {} - Invalid or expired session token.",
                    RequestContext.getAuthor(), RequestContext.getIp());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        if (!requester.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden access to manager dropdown (not admin).",
                    requester.getEmail(), RequestContext.getIp());

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can access this resource.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        List<UsersDropdownMenuDto> dropdownMenuList = userBean.getUsersForManagerDropdownMenu();

        logger.info("User: {} | IP: {} - Successfully retrieved {} users for manager dropdown.",
                requester.getEmail(), RequestContext.getIp(), dropdownMenuList.size());

        return Response.ok(dropdownMenuList, MediaType.APPLICATION_JSON).build();
    }


    /**
     * Retrieves a paginated list of confirmed, active users who do not have a manager assigned.
     * Supports optional filters by name and office.
     * Accessible only to users with ADMIN role.
     *
     * @param name       Optional name filter (matches first or last name).
     * @param officeStr  Optional office filter (as string, converted to UsualWorkPlaceEnum).
     * @param page       Page number for pagination (default 1).
     * @param pageSize   Number of results per page (default 10).
     * @param token      Session token from request header for authentication.
     * @return HTTP 200 with paginated users; 401 if session invalid; 403 if not admin; 400 if office is invalid.
     */
    @GET
    @Path("/users-without-manager-paginated")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersWithoutManagerPaginated(
            @QueryParam("name") String name,
            @QueryParam("office") String officeStr,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @HeaderParam("sessionToken") String token
    ) {
        logger.info("User: {} | IP: {} - Requested users without manager (paginated). Filters - Name: '{}', Office: '{}', Page: {}, PageSize: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), name, officeStr, page, pageSize);


        // 1. Validate and refresh session token if close to expiration
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }



        // Validate session token
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("User: {} | IP: {} - Invalid or expired session token.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        // Check admin privileges
        if (!"ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            logger.warn("User: {} | IP: {} - Forbidden: only ADMIN can access this resource.",
                    requester.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only administrators can access this resource.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Parse office enum
        UsualWorkPlaceEnum officeEnum = null;
        try {
            if (officeStr != null && !officeStr.isBlank()) {
                officeEnum = UsualWorkPlaceEnum.fromString(officeStr);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("User: {} | IP: {} - Invalid office filter value: '{}'",
                    requester.getEmail(), RequestContext.getIp(), officeStr);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid office value: " + officeStr + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Delegate to bean
        PaginatedUsersDto result = userBean.listUsersWithoutManagerFiltered(name, officeEnum, page, pageSize);

        logger.info("User: {} | IP: {} - Retrieved {} users without manager (page {}).",
                requester.getEmail(), RequestContext.getIp(), result.getUsers().size(), page);

        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }



    /**
     * Handles user login by validating credentials and returning a session token along with user profile info.
     *
     * Logs all major steps including missing parameters, failed login attempts, account confirmation,
     * profile loading, and successful logins.
     *
     * @param userLog DTO containing user email and password.
     * @return HTTP response with login result and session token or error message.
     */
    // User login
    @POST
@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public Response loginUser(LoginUserDto userLog) {

    logger.info("User: {} | IP: {} - Login request received",
            RequestContext.getAuthor(), RequestContext.getIp());

    if (userLog == null || userLog.getEmail() == null || userLog.getEmail().isEmpty()
            || userLog.getPassword() == null || userLog.getPassword().isEmpty()) {
        logger.warn("User: {} | IP: {} - Null or empty parameters in login request",
                RequestContext.getAuthor(), RequestContext.getIp());
        return Response.status(401)
                .entity("{\"message\": \"Email or password is empty or null.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Get user from database
    UserEntity userEntity = userBean.findUserEntityByEmail(userLog.getEmail());
    if (userEntity == null) {
        logger.warn("User: {} | IP: {} - Login failed: user not found for email: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userLog.getEmail());
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"message\": \"Invalid credentials.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    logger.info("User: {} | IP: {} - User found for email: {}",
            RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getEmail());

    if (!userBean.isAccountConfirmed(userLog.getEmail())) {
        String confirmToken = userBean.getConfirmToken(userLog.getEmail());
        String confirmationLink = "https://localhost:8443/grupo7/rest/users/confirmAccount?confirmToken=" + confirmToken;

        EmailUtil.sendEmail(
                userEntity.getEmail(),
                "Account Confirmation",
                "Click this link to confirm your account: " + confirmationLink
        );

        logger.warn("User: {} | IP: {} - Account not confirmed. Confirmation email sent to: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getEmail());

        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"message\": \"Your account has not yet been confirmed. A confirmation email has been sent.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    String sessionToken = userBean.login(userLog);

    if (sessionToken == null) {
        logger.error("User: {} | IP: {} - sessionToken is null. Error during login for email: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getEmail());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"message\": \"An internal error occurred during login.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // === NOVO: Validação do perfil ===
    ProfileEntity profile = userEntity.getProfile();
    boolean profileComplete = ProfileValidator.isProfileComplete(profile);
    List<String> missingFields = ProfileValidator.getMissingFields(profile);

    // -- Build LoginResponseDTO --
    LoginResponseDto response = new LoginResponseDto();
    response.setSessionToken(sessionToken);
    response.setId(userEntity.getId());
    response.setEmail(userEntity.getEmail());
    response.setRole(userEntity.getRole().getName());
    response.setProfileComplete(profileComplete);
    response.setMissingFields(missingFields);

    // Profile fields (firstName, lastName) may be in user.getProfile() or similar
    if (profile != null) {
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        logger.info("User: {} | IP: {} - Profile data loaded for user id: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getId());
    } else {
        response.setFirstName(null);
        response.setLastName(null);
        logger.warn("User: {} | IP: {} - Profile data missing for user id: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getId());
    }

    logger.info("User: {} | IP: {} - Login successful. Returning sessionToken, user info, and profile status for email: {}",
            RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getEmail());

    return Response.ok(response)
            .type(MediaType.APPLICATION_JSON)
            .build();
}


    /**
     * Handles user logout by invalidating the session token.
     *
     * Validates the Authorization header for a Bearer token and checks if the session is authorized.
     * Logs all important events: request received, invalid headers, unauthorized tokens, and successful logout.
     *
     * @param authorization The Authorization header containing the Bearer token.
     * @return HTTP response indicating success or failure.
     */
    @POST
@Path("/logout")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public Response logoutUser(@HeaderParam("Authorization") String authorization) {
    logger.info("User: {} | IP: {} - Logout request received",
            RequestContext.getAuthor(), RequestContext.getIp());

    if (authorization == null || !authorization.startsWith("Bearer ")) {
        logger.warn("User: {} | IP: {} - Logout failed: invalid or missing Authorization header",
                RequestContext.getAuthor(), RequestContext.getIp());
        return Response.status(401)
                .entity("{\"message\": \"Invalid Authorization header for logout.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    String sessionToken = authorization.substring("Bearer ".length());

    if (!userBean.authorization(sessionToken)) {
        logger.warn("User: {} | IP: {} - Logout failed: unauthorized sessionToken",
                RequestContext.getAuthor(), RequestContext.getIp());
        return Response.status(403)
                .entity("{\"message\": \"No user authorized for logout.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    logger.info("User: {} | IP: {} - SessionToken authorized, proceeding with logout",
            RequestContext.getAuthor(), RequestContext.getIp());

    userBean.logout(sessionToken);

    logger.info("User: {} | IP: {} - Logout successful",
            RequestContext.getAuthor(), RequestContext.getIp());

    return Response.status(200)
            .entity("{\"message\": \"Logout successful!\"}")
            .type(MediaType.APPLICATION_JSON)
            .build();
}

    /**
     * Promotes a user to ADMIN if the requester has admin privileges.
     * The user to promote must exist, and must not already be an admin.
     * On success, the user loses any evaluation-related roles and responsibilities.
     *
     * @param targetEmail Email of the user to promote to admin.
     * @param token       Session token of the authenticated admin.
     * @return 200 OK if successful, 403 if unauthorized, 401 if session invalid.
     */
    @PUT
    @Path("/promote-to-admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response promoteToAdmin(@QueryParam("email") String targetEmail,
                                   @HeaderParam("sessionToken") String token) {


        // 1. Validate and refresh session token if close to expiration
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn(
                    "User: unknown | IP: {} - Unauthorized attempt to promote {} (invalid session).",
                    RequestContext.getIp(),
                    targetEmail
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        logger.info(
                "User: {} | IP: {} - Attempting to promote {} to ADMIN.",
                currentUser.getEmail(),
                RequestContext.getIp(),
                targetEmail
        );

        boolean success = userBean.promoteUserToAdmin(currentUser, targetEmail);

        if (!success) {
            logger.warn(
                    "User: {} | IP: {} - Failed to promote {} to ADMIN.",
                    currentUser.getEmail(),
                    RequestContext.getIp(),
                    targetEmail
            );
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"User could not be promoted. Ensure you are admin and user exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info(
                "User: {} | IP: {} - Successfully promoted {} to ADMIN.",
                currentUser.getEmail(),
                RequestContext.getIp(),
                targetEmail
        );
        return Response.ok()
                .entity("{\"message\": \"User successfully promoted to admin.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
 * Updates the role and manager of a user.
 * Only administrators can perform this operation.
 *
 * @param userId The ID of the user to update.
 * @param dto    The DTO containing the new role and managerId.
 * @param token  The session token for authorization.
 * @return HTTP 200 if successful, 401/403/400 otherwise.
 */
@PUT
@Path("/{id}/role-manager")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response updateUserRoleAndManager(
        @PathParam("id") int userId,
        RoleUpdaterDto dto,
        @HeaderParam("sessionToken") String token
) {

    // 1. Validate and refresh session token if close to expiration
    SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

    if (sessionStatus == null) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


    // Validate session and authorization
    SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
    if (tokenEntity == null || tokenEntity.getUser() == null) {
        logger.warn("User: unknown | IP: {} - Unauthorized attempt to update userId={}.", RequestContext.getIp(), userId);
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"message\": \"Invalid or expired session.\"}").build();
    }

    UserEntity currentUser = tokenEntity.getUser();

    // Only allow admins to update roles/managers
    if (!currentUser.getRole().getName().equals("ADMIN")) {
        logger.warn("User: {} | IP: {} - Unauthorized attempt to update userId={} (not admin).",
                currentUser.getEmail(), RequestContext.getIp(), userId);
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"message\": \"Only administrators can update users.\"}").build();
    }

    logger.info("User: {} | IP: {} - Attempting to update userId={} to role '{}' and managerId={}.",
            currentUser.getEmail(), RequestContext.getIp(), userId, dto.getRole(), dto.getManagerId());

    try {
        userBean.updateRoleAndManager(userId, dto.getRole(), dto.getManagerId());
        logger.info("User: {} | IP: {} - Successfully updated userId={}.",
                currentUser.getEmail(), RequestContext.getIp(), userId);
        return Response.ok()
                .entity("{\"message\": \"User successfully updated.\"}")
                .build();
    } catch (Exception ex) {
        logger.error("User: {} | IP: {} - Error updating userId={}: {}",
                currentUser.getEmail(), RequestContext.getIp(), userId, ex.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"message\": \"Failed to update user: " + ex.getMessage() + "\"}")
                .build();
    }
}

    /**
     * Handles a password reset request by generating and emailing a recovery link.
     *
     * For security, does not reveal whether the email exists in the system.
     * Logs the request and any invalid email attempts.
     *
     * @param userDto DTO containing the user's email address.
     * @return HTTP response indicating the reset request status.
     */
    @POST
    @Path("/request-reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestPasswordReset(UserDto userDto) {
        String email = userDto.getEmail();

        if (email == null || email.isEmpty()) {
            logger.warn("Password reset requested with invalid email. IP: {}", RequestContext.getIp());
            return Response.status(400)
                    .entity("{\"message\": \"Email inválido.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserDto user = userBean.findUserByEmail(email);
        if (user == null) {
            logger.info("Password reset requested for non-existent email '{}'. IP: {}", email, RequestContext.getIp());
            // Do not reveal user existence for security
            return Response.ok("{\"message\": \"If the email is registered in our servers, the recovery link will be sent.\"}")
                    .build();
        }

        String recoveryToken = userBean.generateRecoveryToken(user.getEmail());

        String recoveryLink = "https://localhost:3000/redefinepassword?recoveryToken=" + recoveryToken;

        EmailUtil.sendEmail(
                user.getEmail(),
                "Request for password reset",
                "Please, click on this link to select your new password: " + recoveryLink
        );

        logger.info("Password reset link sent to email '{}'. IP: {}", user.getEmail(), RequestContext.getIp());

        return Response.ok()
                .entity("{\"message\": \"If the email is registered in our servers, the recovery link will be sent.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }




    /**
     * Resets the user's password using a valid recovery token.
     *
     * Validates the token, password length, and resets the password if all checks pass.
     * Logs warnings for invalid tokens and weak passwords.
     *
     * @param recoveryToken The token provided for password recovery (query parameter).
     * @param resetDto DTO containing the new password.
     * @return HTTP response indicating success or failure.
     */
    @POST
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(@QueryParam("recoveryToken") String recoveryToken, ResetPasswordDto resetDto) {
        if (!userBean.isRecoveryTokenValid(recoveryToken)) {
            logger.warn("User: {} | IP: {} - Invalid or expired recovery token used for password reset.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid or expired token.\", \"error\": true}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String newPassword = resetDto.getPassword();

        if (newPassword == null || newPassword.length() <= 5) {
            logger.warn("User: {} | IP: {} - Password length insufficient during reset attempt.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(400)
                    .entity("{\"message\": \"A password deve ter mais de 5 caracteres.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = userBean.resetPasswordWithToken(recoveryToken, newPassword);

        if (!success) {
            logger.error("User: {} | IP: {} - Failed to reset password with valid token.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Erro ao redefinir a senha. Tente novamente.\", \"error\": true}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Password reset successfully completed.",
                RequestContext.getAuthor(), RequestContext.getIp());

        return Response.ok()
                .entity("{\"message\": \"Password atualizada com sucesso!\", \"error\": false}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }




    /**
     * Validates and refreshes a user's session token.
     *
     * Checks if the session token is provided and still valid. If valid, returns updated session info.
     * Logs attempts with missing or invalid tokens.
     *
     * @param sessionToken The session token provided in the HTTP header.
     * @return HTTP response with session status or error message.
     */
    @POST
    @Path("/validate-session")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateSession(@HeaderParam("sessionToken") String sessionToken) {

        if (sessionToken == null || sessionToken.isEmpty()) {
            logger.warn("Session validation failed: token not provided. IP: {}", RequestContext.getIp());
            return Response.status(400)
                    .entity("{\"message\": \"Token de sessão não fornecido.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Session validation failed: token invalid or expired. IP: {}", RequestContext.getIp());
            return Response.status(401)
                    .entity("{\"message\": \"Sessão expirada. Faça login novamente.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("Session validated and refreshed successfully. User: {} | IP: {}",
                RequestContext.getAuthor(), RequestContext.getIp());

        return Response.ok(sessionStatusDto)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


    /**
 * REST endpoint to retrieve all active and confirmed users with the "MANAGER" role.
 * The returned list is converted to UsersDropdownMenuDto for UI dropdowns.
 *
 * @return HTTP 200 with a JSON array of UsersDropdownMenuDto representing managers.
 */
@GET
@Path("/managers")
@Produces(MediaType.APPLICATION_JSON)
public Response getManagers() {
    // Get the list of managers from the bean
    List<UserEntity> managers = userBean.listManagers();

    // Convert UserEntity list to UsersDropdownMenuDto (contains only needed fields)
    List<UsersDropdownMenuDto> dtos = managers.stream()
        .map(u -> new UsersDropdownMenuDto(
            u.getId(),
            u.getEmail(),
            u.getProfile() != null ? u.getProfile().getFirstName() : "",
            u.getProfile() != null ? u.getProfile().getLastName() : ""
        ))
        .collect(Collectors.toList());

    // Return as JSON response
    return Response.ok(dtos).build();
}

}
