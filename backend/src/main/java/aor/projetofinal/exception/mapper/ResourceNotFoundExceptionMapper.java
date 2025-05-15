package aor.projetofinal.exception.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.dto.ErrorResponseDto;
import aor.projetofinal.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    private static final Logger logger = LogManager.getLogger(ResourceNotFoundExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        logger.error("Resource not found exception: {}", exception.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.NOT_FOUND.getStatusCode(),
                "Not Found",
                exception.getMessage(),
                uriInfo.getPath());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }
}
