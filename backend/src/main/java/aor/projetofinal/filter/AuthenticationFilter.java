package aor.projetofinal.filter;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.entity.UserEntity;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Filter that checks if the user is authenticated by verifying the session token.
 * If the token is valid, it sets the current user in the RequestContext.
 * If not, it returns a 401 Unauthorized response.
 * Accepts token in header ("token") for REST and in querystring ("?token=") for WebSocket.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

    @Inject
    private UserDao userDao;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Allow preflight requests (CORS)
        // This is necessary for CORS requests, especially for REST APIs
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        chain.doFilter(servletRequest, servletResponse);
        return;
    }

        String path = request.getRequestURI();
        logger.info("User: {} | IP: {} - Incoming request path: {}", RequestContext.getAuthor(), RequestContext.getIp(), path);

        // Allow public endpoints without authentication
        if (path.endsWith("/login") || path.endsWith("/createUser") || path.contains("/confirmAccount") || path.contains("/request-reset") || path.contains("/reset-password")) {
            logger.info("User: {} | IP: {} - Public endpoint, skipping authentication for path: {}", RequestContext.getAuthor(), RequestContext.getIp(), path);
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Try to get token from header (REST) or querystring (WebSocket)
        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            token = request.getHeader("sessionToken"); // also accepts sessionToken
        }
        boolean fromQueryString = false;

        if ((token == null || token.isBlank()) && request.getQueryString() != null) {
            String[] params = request.getQueryString().split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring("token=".length());
                    fromQueryString = true;
                    break;
                }
            }
        }

        if (token != null && !token.isBlank()) {
            UserEntity user = userDao.findBySessionToken(token);

            if (user != null) {
                // Valid token, set the user in RequestContext
                RequestContext.setAuthor(user.getEmail());
                RequestContext.setCurrentUser(user);
                logger.info("User: {} | IP: {} - Valid token (from {}). Authenticated access granted for path: {}",
                        RequestContext.getAuthor(), RequestContext.getIp(),
                        fromQueryString ? "querystring" : "header", path);
                chain.doFilter(servletRequest, servletResponse);
                return;
            } else {
                logger.warn("User: {} | IP: {} - Invalid token (from {}) provided for path: {}",
                        RequestContext.getAuthor(), RequestContext.getIp(),
                        fromQueryString ? "querystring" : "header", path);
            }
        } else {
            logger.warn("User: {} | IP: {} - Missing token for path: {}",
                    RequestContext.getAuthor(), RequestContext.getIp(), path);
        }

        // No valid token found, block the request
        RequestContext.setAuthor("Anonymous");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Invalid or missing token\"}");
        // Do not call chain.doFilter!
    }
}


