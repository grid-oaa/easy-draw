package com.easydraw.backend.config;

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

  @Value("${app.allowed-origins:https://easy-draw.vercel.app}")
  private String allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins =
        Stream.of(allowedOrigins.split(","))
            .map(String::trim)
            .filter((value) -> !value.isEmpty())
            .toArray(String[]::new);

    if (origins.length == 0) {
      origins = new String[] {"https://easy-draw.vercel.app"};
    }

    registry
        .addMapping("/api/**")
        .allowedOrigins(origins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
