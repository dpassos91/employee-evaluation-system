package aor.projetofinal.service;

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

    String ip = request.getRemoteAddr();

    // Criar novo utilizador
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser (UserDto userDto) {
        logger.info("{}, Registration attempt by user: {}", ip, userDto.getEmail());
        userDto createdUser = userBean.registerUser(userDto);

        logger.info("{}, User successfully registered: {}", ip, createdUser.getEmail());
        return Response.ok(createdUser).build();
    }
}
