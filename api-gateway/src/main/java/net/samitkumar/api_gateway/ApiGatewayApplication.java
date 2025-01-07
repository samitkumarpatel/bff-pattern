package net.samitkumar.api_gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
public class ApiGatewayApplication {

	@Autowired
	ReactiveOAuth2AuthorizedClientService authorizedClientService;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction() {
		return RouterFunctions
				.route()
				.GET("/token", request -> ReactiveSecurityContextHolder.getContext()
						.map(SecurityContext::getAuthentication)
						.map(Authentication::getName)
						.flatMap(name -> authorizedClientService.loadAuthorizedClient("gateway", name).map(OAuth2AuthorizedClient::getAccessToken))
						.flatMap(ServerResponse.ok()::bodyValue)
				)
				.build();
	}
}

