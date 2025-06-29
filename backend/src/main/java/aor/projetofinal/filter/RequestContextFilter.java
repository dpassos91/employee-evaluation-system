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

        // Capture IP address (supporting proxies/load balancers)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        // Only set the IP in the context; do not set author here.
        RequestContext.setIp(ip.trim());

        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            // Always clear the context at the end of each request to prevent memory leaks.
            RequestContext.clear();
        }
    }
}

