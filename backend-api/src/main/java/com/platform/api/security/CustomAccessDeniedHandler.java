package com.platform.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) 
            throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse error = new ErrorResponse(
            "ACCESS_DENIED",
            "У вашей роли недостаточно прав для выполнения этой операции",
            List.of(accessDeniedException.getMessage())
        );

        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}