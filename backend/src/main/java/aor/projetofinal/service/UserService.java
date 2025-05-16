package aor.projetofinal.service;

import aor.projetofinal.bean.UserBean;
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
    Notifier notifier;

    @Context
    private HttpServletRequest request;

    // Criar novo utilizador
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(UserDto userDto) {
        String ip = getClientIp();
        String author = getAuthenticatedUser(); // Se tiveres autenticação

        logger.info("User: {} | IP: {} - Registration attempt for email: {}", author, ip, userDto.getEmail());

        UserDto createdUser = userBean.registerUser(userDto);

        logger.info("User: {} | IP: {} - Successfully registered user with email: {}", author, ip, createdUser.getEmail());

        return Response.ok(createdUser).build();
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return (ip != null) ? ip.trim() : request.getRemoteAddr();
    }

    private String getAuthenticatedUser() {
        return (request.getUserPrincipal() != null)
                ? request.getUserPrincipal().getName()
                : "Anonymous";
    }
}
