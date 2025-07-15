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

import jakarta.servlet.http.HttpServletRequest;

@Provider
public class EmailAlreadyExistsExceptionMapper implements ExceptionMapper<EmailAlreadyExistsException> {

    private static final Logger logger = LogManager.getLogger(EmailAlreadyExistsExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(EmailAlreadyExistsException exception) {
        String ip = getClientIp();
        String author = getAuthenticatedUser();

        logger.error("User: {} | IP: {} - EmailAlreadyExistsException: {}", author, ip, exception.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.CONFLICT.getStatusCode(),
    "EMAIL_ALREADY_EXISTS",  
    "Email already in use.",  
    uriInfo != null ? uriInfo.getPath() : "unknown",
    ip,
    author
        );

        return Response.status(Response.Status.CONFLICT)
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
