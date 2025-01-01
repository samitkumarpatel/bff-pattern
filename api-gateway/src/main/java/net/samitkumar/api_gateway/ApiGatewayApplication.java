package net.samitkumar.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}

/*
@Configuration
@EnableWebFluxSecurity
class OAuth2ClientSecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http
				.cors(ServerHttpSecurity.CorsSpec::disable)
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(authorize -> authorize
						.pathMatchers(HttpMethod.OPTIONS).permitAll()
						.anyExchange().authenticated()
				)
				.oauth2Login(Customizer.withDefaults());
		return http.build();
	}

}*/
