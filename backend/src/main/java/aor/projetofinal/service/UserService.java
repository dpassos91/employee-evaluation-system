package aor.projetofinal.service;

import aor.projetofinal.Util.EmailUtil;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.LoginUserDto;
import aor.projetofinal.dto.LoginResponseDto;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.dto.ResetPasswordDto;
import aor.projetofinal.dto.SessionStatusDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
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
    EmailUtil emailUtil;
    @Inject
    private SessionTokenDao sessionTokenDao;
    @Inject
    private UserDao userDao;


    //@Inject
    //Notifier notifier;

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

    // login utilizador
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

        emailUtil.sendEmail(
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

    // -- Build LoginResponseDTO --
    LoginResponseDto response = new LoginResponseDto();
    response.setSessionToken(sessionToken);
    response.setId(userEntity.getId());
    response.setEmail(userEntity.getEmail());
    response.setRole(userEntity.getRole().getName());

    // Profile fields (firstName, lastName) may be in user.getProfile() or similar
    if (userEntity.getProfile() != null) {
        response.setFirstName(userEntity.getProfile().getFirstName());
        response.setLastName(userEntity.getProfile().getLastName());
        logger.info("User: {} | IP: {} - Profile data loaded for user id: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getId());
    } else {
        response.setFirstName(null);
        response.setLastName(null);
        logger.warn("User: {} | IP: {} - Profile data missing for user id: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getId());
    }

    logger.info("User: {} | IP: {} - Login successful. Returning sessionToken and user info for email: {}",
            RequestContext.getAuthor(), RequestContext.getIp(), userEntity.getEmail());

    return Response.ok(response)
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


    @POST
    @Path("/logout") // postman
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logoutUser(@HeaderParam("sessionToken") String sessionToken) {
        logger.info("Logout request recebido");
        if (sessionToken == null) {
            logger.warn("SessionToken null - ocorreu um erro a fazer logout");
            return Response.status(401)
                    .entity("{\"message\": \"Dados inválidos no header para logout\"}")
                    .type(MediaType.APPLICATION_JSON) // Força Content-Type JSON
                    .build();
        }

        if (!userBean.authorization(sessionToken)) {
            logger.warn("Tentativa de logout com sessionToken sem autorização");
            return Response.status(403)
                    .entity("{\"message\": \"Sem utilizador para logout.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        userBean.logout(sessionToken);
        logger.info("Logout com sucesso");
        return Response.status(200)
                .entity("{\"message\": \"Logout bem-sucedido!\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();

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


        emailUtil.sendEmail(
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


    /*//update password de user
    @PATCH
    @Path("/update/{email}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserPassword(@HeaderParam("sessionToken") String sessionToken, @PathParam("email") String email,
                               UserDto passwordAtualizado) {

        // Valida e renova a sessão
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Sessão inválida ou expirada - update user");
            return Response.status(401)
                    .entity("{\"message\": \"Sessão expirada. Faça login novamente.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        UserDto sessionUserDto = userBean.findUserBySessionToken(sessionToken);





    }
*/

        //update perfil de user
    @PUT
    @Path("/update/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("sessionToken") String sessionToken, @PathParam("email") String email,
                               ProfileDto profileToUpdate) {


        // Valida e renova a sessão
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Sessão inválida ou expirada - update user");
            return Response.status(401)
                    .entity("{\"message\": \"Sessão expirada. Faça login novamente.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }




        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);

        UserEntity currentUserLoggedIn = sessionTokenEntity.getUser();
        UserEntity currentProfile = userDao.findByEmail(email);

        // Autorização: apenas o próprio ou admin pode atualizar
        if(!(currentUserLoggedIn.getRole().getName()).equalsIgnoreCase("admin") && (!(currentUserLoggedIn.getEmail().equalsIgnoreCase(currentProfile.getEmail())))) {
            logger.warn("update user - não autorizado");
            return Response.status(403)
                    .entity("{\"message\": \"Não autorizado a atualizar este utilizador.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        String photo = profileToUpdate.getPhotograph();
        if (photo != null && photo.length() > 250) {
            logger.warn("Utilizador recebido com informação invalida");
            return Response.status(400)
                    .entity("{\"message\": \"Por favor, coloque um link para a fotografia com até 250 caracteres.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        //validar numeros de telefones para apenas permitir dígitos e hifenes - numeros internacionais
        String phone = profileToUpdate.getPhone();
        if (phone == null || !phone.matches("^[0-9-]+$")) {
            logger.warn("Número de telefone inválido");
            return Response.status(400)
                    .entity("{\"message\": \"Por favor, insira apenas dígitos e hífenes no número de telefone.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!userBean.updateInfo(profileToUpdate, email)) {
            logger.warn("Erro a atualizar utilizador - update user");
            return Response.status(400)
                    .entity("{\"message\": \"Não foi possível atualizar o utilizador\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("Update user com sucesso username: {}", email);
        return Response.status(200)
                .entity("{\"message\": \"Update de dados com sucesso!\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();


    }








}
