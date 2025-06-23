package aor.projetofinal.service;


import aor.projetofinal.bean.UserBean;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.dto.SessionStatusDto;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import aor.projetofinal.context.RequestContext;

import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.stream.Collectors;

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


    /*@GET
    @Path("/list-users-by-filters")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(
            @HeaderParam("sessionToken") String sessionToken,
            @QueryParam("profile-name") String profileName,
            @QueryParam("usual-work-place") UsualWorkPlaceType usualLocation,
            @QueryParam("manager-name") String managerName
    )
    {
        // Valida e renova a sessão
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);

        if (sessionStatusDto == null) {
            logger.warn("Sessão inválida ou expirada - update user");
            return Response.status(401)
                    .entity("{\"message\": \"Sessão expirada. Faça login novamente.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        ArrayList<ProductDto> products = productBean.getAllProductsByFilters(seller, state, category, active,
                sellerExcluded, updated);

    }*/

    // Get usual workplace options
    @GET
    @Path("/usualworkplaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getUsualWorkplaceOptions() {
        logger.info("User: {} | IP: {} - Attempt to fetch usual workplace options",
                RequestContext.getAuthor(), RequestContext.getIp());

        List<String> options = Arrays.stream(UsualWorkPlaceType.values())
            .map(Enum::name)
            .collect(Collectors.toList());

        logger.info("User: {} | IP: {} - Fetched {} usual workplace options: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), options.size(), options);

        return options;
    }

    //update perfil de user
    @PUT
    @Path("/update/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(@HeaderParam("sessionToken") String sessionToken, @PathParam("email") String email,
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
