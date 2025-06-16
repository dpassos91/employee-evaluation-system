package aor.projetofinal.service;

import aor.projetofinal.Util.EmailUtil;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.LoginUserDto;
import aor.projetofinal.dto.UserDto;
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


    //@Inject
    //Notifier notifier;

    // Criar novo utilizador
    @POST
    @Path("/createUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(UserDto userDto) {

        logger.info("User: {} | IP: {} - Registration attempt for email: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userDto.getEmail());

        UserDto createdUser = userBean.registerUser(userDto);

        logger.info("User: {} | IP: {} - Successfully registered user with email: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), userDto.getEmail());

        return Response.ok(createdUser).build();
    }

    // login utilizador
    @POST
    @Path("/login") // postman
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginUser(LoginUserDto userLog) { // retorna um user com email e password

        logger.info("Login request recebido");

        if (userLog == null || userLog.getEmail() == null || userLog.getEmail().isEmpty()
                || userLog.getPassword() == null || userLog.getPassword().isEmpty()) {
            logger.warn("Parametros null recebidos no request");
            return Response.status(401)
                    .entity("{\"message\": \"password ou email vazio ou null\"}")
                    .type(MediaType.APPLICATION_JSON) // Força Content-Type JSON
                    .build();
        }



        // Obter o utilizador Dto correspondente a este userLog do tipo LoginUserDto
         UserDto user = userBean.findUserByEmail(userLog.getEmail());


        if (user == null) {
            logger.warn("Utilizador não encontrado");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Credenciais inválidas\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }



        if (!userBean.isAccountConfirmed(userLog.getEmail())) {
            String confirmToken = userBean.getConfirmToken(userLog.getEmail());


            String confirmationlink = "http://localhost:8080/grupo7/rest/users/confirmAccount?confirmToken=" + confirmToken;

            emailUtil.sendEmail(
                    user.getEmail(),
                    "Confirmação da Conta",
                    "Clique neste link para confirmar a sua conta: " + confirmationlink
            );

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"A sua conta ainda não foi confirmada. Foi enviado um email de confirmação.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        }


        String sessionToken = userBean.login(userLog);

        if (sessionToken == null) {
            logger.warn("sessionToken null - ocorreu um erro a fazer login");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Ocorreu um erro interno ao tentar fazer login.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("Login com sucesso a enviar sessionToken");
        return Response.status(200)
                .entity("{\"sessionToken\": \"" + sessionToken + "\"}") // garante que o token seja enviado dentro de
                // um objeto JSON e
                // não como uma string
                .type(MediaType.APPLICATION_JSON) // Força Content-Type JSON
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










}
