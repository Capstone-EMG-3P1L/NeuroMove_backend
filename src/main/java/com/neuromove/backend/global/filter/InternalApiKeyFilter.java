package com.neuromove.backend.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuromove.backend.global.api.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-API-KEY";

    @Value("${security.api-key}")
    private String internalApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        boolean isInternalApi =
                requestUri.startsWith("/api/ai/")
                        || requestUri.startsWith("/api/emg-devices-info")
                        || requestUri.startsWith("/api/motor-devices-info");

        if (!isInternalApi) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader(HEADER_NAME);

        if (key == null || key.isBlank() || !internalApiKey.equals(key)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiResponse.error("INVALID_INTERNAL_KEY", "유효하지 않은 Internal Key입니다.")
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}