package aor.projetofinal.service;

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

    //@Inject
    //Notifier notifier;

    // Criar novo utilizador
    @POST
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


    @POST
    @Path("/login") // postman
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginUser(LoginUserDto userLog) { // retorna um user com email e password

        logger.info("Login request recebido");

        if (userLog == null || userLog.getEmail() == null || userLog.getPassword() == null) {
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

        //confirmação feita diretamente na DB
       if (!userBean.isAccountConfirmed(userLog.getEmail())) {
            String accountConfirmToken = userBean.getConfirmToken(userLog.getEmail());
            String confirmationlink = "http://localhost:8080/david-proj5-1.0-SNAPSHOT/rest/users/confirmAccount?accountConfirmToken=" + accountConfirmToken;

            logger.warn("Conta não confirmada. Link de confirmação: " + confirmationlink);

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"A conta ainda não foi confirmada.\", \"confirmationLink\": \"" + confirmationlink + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        String sessionToken = userBean.login(userLog);

        if (sessionToken == null) {
            logger.warn("sessionToken null - ocorreu um erro a fazer login");
            return Response.status(401)
                    .entity("{\"message\": \"Não foi possivel fazer login\"}")
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




}
