package com.ecommerce.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

@Configuration
public class GatewayWebConfig {

    @Bean
    public RouterFunction<ServerResponse> htmlRouter() {
        return RouterFunctions.route()
                .GET("/", request -> ServerResponse.temporaryRedirect(URI.create("/index.html")).build())
                .resources("/**", new ClassPathResource("static/"))
                .build();
    }
}
