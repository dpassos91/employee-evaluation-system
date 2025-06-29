package aor.projetofinal.filter;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.entity.UserEntity;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that checks if the user is authenticated by verifying the session token.
 * If the token is valid, it sets the current user in the RequestContext.
 * If not, it returns a 401 Unauthorized response.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Inject
    private UserDao userDao;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        
        String token = request.getHeader("token");

        if (token != null && !token.isBlank()) {
            UserEntity user = userDao.findBySessionToken(token);

            if (user != null) {
                // Valid token, set the user in RequestContext
                RequestContext.setAuthor(user.getEmail());
                RequestContext.setCurrentUser(user); // Ver ponto 2 para adicionar este método!
                // Goes to endpoint REST
                chain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        // If no valid token, return 401 and do not call REST endpoint
        RequestContext.setAuthor("Anonymous");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Invalid or missing token\"}");
        // Do not call chain.doFilter!
    }
}
