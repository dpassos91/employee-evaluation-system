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

import jakarta.servlet.http.HttpServletRequest;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private static final Logger logger = LogManager.getLogger(BadRequestExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(BadRequestException exception) {
        String ip = getClientIp();
        String author = getAuthenticatedUser();

        logger.error("User: {} | IP: {} - BadRequestException: {}", author, ip, exception.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                exception.getMessage(),
                uriInfo.getPath(),
                ip,
                author
        );

        return Response.status(Response.Status.BAD_REQUEST)
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
