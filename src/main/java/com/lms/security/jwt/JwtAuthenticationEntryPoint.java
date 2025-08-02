package com.lms.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("apiPath",request.getRequestURI());
        data.put("errorCodeString",HttpStatus.UNAUTHORIZED.value());
        data.put("errorCode",HttpStatus.UNAUTHORIZED);
        data.put("errorMessage",getErrorMessage(authException));
        data.put("errorTime",LocalDateTime.now().toString());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        new ObjectMapper().writeValue(response.getOutputStream(),data);
    }
    private String getErrorMessage(AuthenticationException ex) {
        if (ex instanceof BadCredentialsException) return "Invalid credentials";
        if (ex instanceof DisabledException) return "User account is disabled";
        return "Unauthorized access";
    }
}
