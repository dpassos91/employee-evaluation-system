package aor.projetofinal.exception.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.dto.ErrorResponseDto;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import jakarta.servlet.http.HttpServletRequest;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LogManager.getLogger(GeneralExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(Exception exception) {
        String ip = getClientIp();
        String author = getAuthenticatedUser();

        logger.error("User: {} | IP: {} - Unexpected exception occurred", author, ip, exception);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                exception.getMessage(),
                (uriInfo != null ? uriInfo.getPath() : "unknown"),
                ip,
                author
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
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

