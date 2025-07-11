package aor.projetofinal.service;

import aor.projetofinal.dto.*;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.util.EmailUtil;
import aor.projetofinal.util.ProfileValidator;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;

import aor.projetofinal.entity.ProfileEntity;

import aor.projetofinal.entity.UserEntity;
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
    @PATCH
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







    @GET
    @Path("/confirmAccount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmAccount(@QueryParam("confirmToken") String confirmToken) {
        if (confirmToken == null || confirmToken.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Token de confirmação de conta em falta.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = userBean.confirmAccount(confirmToken);

        if (success) {
            return Response.ok("{\"message\": \"Conta confirmada com sucesso! Já pode fazer login\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Link inválido ou expirado. Foi gerado um novo Link de confirmação de conta." +
                            " Torne a aceder ao seu email registado" +
                            " para ter acesso ao novo link de confirmação de conta\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }



    // Criar novo utilizador
    @POST
    @Path("/createUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(LoginUserDto loginUserDto) {

        logger.info("User: {} | IP: {} - Registration attempt for email: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), loginUserDto.getEmail());

        UserDto createdUser = userBean.registerUser(loginUserDto);

        logger.info("User: {} | IP: {} - Successfully registered user with email: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), loginUserDto.getEmail());

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

    @POST
    @Path("/request-reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestPasswordReset( UserDto userDto) {
        String email = userDto.getEmail();


        if (email == null || email.isEmpty()) {
            return Response.status(400)
                    .entity("{\"message\": \"Email inválido.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserDto user = userBean.findUserByEmail(email);
        if (user == null) {
            // Por razões de segurança, não revelar se o email existe
            return Response.ok("{\"message\": \"Se o email existir, o link de recuperação será enviado.\"}").build();
        }


        String recoveryToken = userBean.generateRecoveryToken(user.getEmail());

        String recoveryLink = "https://127.0.0.1:8443/grupo7/rest/users/reset-password?recoveryToken=" + recoveryToken;


        EmailUtil.sendEmail(
                user.getEmail(),
                "Pedido de recuperação de password",
                "Clique neste link para escolher nova password: " + recoveryLink
        );


        return Response.ok()
                .entity("{\"message\": \"Se o email existir, o link de recuperação será enviado.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }



    @POST
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(@QueryParam("recoveryToken") String recoveryToken, ResetPasswordDto resetDto)
 {
        if (!userBean.isRecoveryTokenValid(recoveryToken)) {
            return Response.status(Response.Status.BAD_REQUEST)

                    .entity("{\"message\": \"Token expirado ou inválido.\", \"error\": true}")

                    .type(MediaType.APPLICATION_JSON)

                    .build();
        }

        String newPassword = resetDto.getPassword();

        if (newPassword.length() <= 5) {
            logger.warn("Password com comprimento insuficiente");
            return Response.status(400)
                    .entity("{\"message\": \"A password deve ter mais de 5 caracteres.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }




        boolean success = userBean.resetPasswordWithToken(recoveryToken, newPassword);

        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)

                    .entity("{\"message\": \"Erro ao redefinir a senha. Tente novamente.\", \"error\": true}")

                    .type(MediaType.APPLICATION_JSON)

                    .build();
        }

        return Response.ok()

                .entity("{\"message\": \"Password atualizada com sucesso!\", \"error\": false}")

                .type(MediaType.APPLICATION_JSON)

                .build();
    }



    @POST
    @Path("/validate-session")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateSession(@HeaderParam("sessionToken") String sessionToken) {

        // Verifica se o sessionToken foi fornecido
        if (sessionToken == null || sessionToken.isEmpty()) {
            return Response.status(400)
                    .entity("{\"message\": \"Token de sessão não fornecido.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // Analisa se a sessão ainda está válida
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);


        if (sessionStatusDto == null) {
            // Se o sessionToken for inválido ou expirado, retorna erro 401 (Unauthorized)
            return Response.status(401)
                    .entity("{\"message\": \"Sessão expirada. Faça login novamente.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Se o sessionToken for válido e renovado, retorna a resposta 200 com o SessionStatusDto, convertendo-o em JSON
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
