package aor.projetofinal.exception.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.dto.ErrorResponseDto;
import aor.projetofinal.exception.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private static final Logger logger = LogManager.getLogger(BadRequestExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(BadRequestException exception) {
        logger.error("Bad request exception: {}", exception.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                exception.getMessage(),
                uriInfo.getPath());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
