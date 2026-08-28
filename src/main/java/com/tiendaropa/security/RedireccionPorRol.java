package com.tiendaropa.security;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** Despues del login cada rol llega a la pantalla que le corresponde. */
@Component
public class RedireccionPorRol implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication autenticacion) throws IOException, ServletException {
        boolean esCliente = autenticacion.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        response.sendRedirect(request.getContextPath() + (esCliente ? "/mi-cuenta" : "/panel"));
    }
}
