package org.nashinnov8.multitrack.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .findAndRegisterModules()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      @NonNull AuthenticationException authException)
      throws IOException, ServletException {

    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpStatus.UNAUTHORIZED.value());

    org.nashinnov8.multitrack.common.exception.ErrorResponse errorResponse =
        new org.nashinnov8.multitrack.common.exception.ErrorResponse(
            java.time.Instant.now(),
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Full authentication is required to access this resource",
            request.getRequestURI(),
            null,
            null);

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
