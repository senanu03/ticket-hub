package com.tp.snapseat.booking_service.security;

import com.tp.snapseat.booking_service.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/** 401 / 403 in the same standard error format as every other error. */
@Component
public class RestSecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You are not allowed to perform this action");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                       String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = "{"
                + "\"timestamp\":" + JsonUtil.quote(Instant.now().toString())
                + ",\"status\":" + status.value()
                + ",\"error\":" + JsonUtil.quote(status.getReasonPhrase())
                + ",\"code\":" + JsonUtil.quote(code)
                + ",\"message\":" + JsonUtil.quote(message)
                + ",\"path\":" + JsonUtil.quote(request.getRequestURI())
                + ",\"correlationId\":" + JsonUtil.quote(MDC.get("correlationId"))
                + ",\"details\":null}";
        response.getWriter().write(body);
    }
}
