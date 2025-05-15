package aor.projetofinal.exception.mapper;

import aor.projetofinal.dto.ErrorResponseDto;
import aor.projetofinal.exception.EmailAlreadyExistsException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class EmailAlreadyExistsExceptionMapper implements ExceptionMapper<EmailAlreadyExistsException> {

    private static final Logger logger = LogManager.getLogger(EmailAlreadyExistsExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(EmailAlreadyExistsException exception) {
        logger.error("Email already exists: {}", exception.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                exception.getMessage(),
                uriInfo.getPath());

        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}

