package com.platform.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) 
            throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse error = new ErrorResponse(
            "UNAUTHORIZED",
            "Для доступа к этому ресурсу требуется действующий токен авторизации",
            List.of(authException.getMessage() != null ? authException.getMessage() : "Missing or invalid Bearer token")
        );

        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}