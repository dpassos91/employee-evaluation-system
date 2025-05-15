package aor.projetofinal.exception.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.dto.ErrorResponseDto;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LogManager.getLogger(GeneralExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        logger.error("Unexpected error occurred", exception);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                exception.getMessage(),
                uriInfo != null ? uriInfo.getPath() : "unknown");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
