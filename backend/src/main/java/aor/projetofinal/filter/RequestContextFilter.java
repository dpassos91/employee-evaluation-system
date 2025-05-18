package aor.projetofinal.filter;

import aor.projetofinal.context.RequestContext;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter("/*")
public class RequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // Capturar IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        // Capturar utilizador autenticado
        String author = (request.getUserPrincipal() != null)
                ? request.getUserPrincipal().getName()
                : "Anonymous";

        // Guardar no contexto
        RequestContext.setIp(ip.trim());
        RequestContext.setAuthor(author);

        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            RequestContext.clear(); // evitar memory leaks
        }
    }
}

