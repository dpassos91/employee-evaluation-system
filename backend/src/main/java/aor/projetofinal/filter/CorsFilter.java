package aor.projetofinal.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "https://localhost:3000");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "Content-Type, Authorization, token, authToken, email, sessionToken");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

    }
}