package aor.projetofinal.exception.mapper;

import jakarta.servlet.http.HttpServletRequest;
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

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        String ip = getClientIp();
        String author = getAuthenticatedUser();

        logger.error("User: {} | IP: {} - ResourceNotFoundException: {}", author, ip, exception.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.NOT_FOUND.getStatusCode(),
                "Not Found",
                exception.getMessage(),
                uriInfo.getPath(),
                ip,
                author
        );

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
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

