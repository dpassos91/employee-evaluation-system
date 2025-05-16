package aor.projetofinal.exception.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.dto.ErrorResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import jakarta.servlet.http.HttpServletRequest;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger logger = LogManager.getLogger(ValidationExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String ip = getClientIp();
        String author = getAuthenticatedUser();

        logger.error("User: {} | IP: {} - Validation exception occurred: {}", author, ip, exception.getMessage());

        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
            logger.warn("Validation failed for {}: {}", propertyPath, message);
        }

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Validation Error",
                "One or more fields are invalid.",
                uriInfo.getPath(),
                ip,
                author
        );
        errorResponse.setValidationErrors(errors);

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
