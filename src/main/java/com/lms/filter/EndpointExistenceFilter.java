package com.lms.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.ErrorResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EndpointExistenceFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;
    private final ObjectMapper objectMapper;

    public EndpointExistenceFilter(@Qualifier("requestMappingHandlerMapping")RequestMappingHandlerMapping handlerMapping, ObjectMapper objectMapper) {
        this.handlerMapping = handlerMapping;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HandlerExecutionChain handler = null;
        try {
            handler = handlerMapping.getHandler(request);
        } catch (Exception ignored) { }

        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");

            ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                    request.getRequestURI(),
                    HttpStatus.NOT_FOUND,
                    HttpStatus.NOT_FOUND.value(),
                    "Endpoint not found",
                    LocalDateTime.now()
            );

            String json = objectMapper.writeValueAsString(errorResponseDto);
            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
