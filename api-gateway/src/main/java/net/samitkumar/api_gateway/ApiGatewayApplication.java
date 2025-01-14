package net.samitkumar.api_gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Consumer;

@SpringBootApplication
public class ApiGatewayApplication {

	final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public ApiGatewayApplication(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }


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

@Configuration
@EnableWebFluxSecurity
class OAuth2LoginSecurityConfig {

	@Autowired
	private ReactiveClientRegistrationRepository clientRegistrationRepository;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http
				.authorizeExchange(authorize -> authorize
						.anyExchange().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2
						.authorizationRequestResolver(
								authorizationRequestResolver(this.clientRegistrationRepository)
						)
				)
				.csrf(ServerHttpSecurity.CsrfSpec::disable);
		return http.build();
	}

	private ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver(
			ReactiveClientRegistrationRepository clientRegistrationRepository) {

		DefaultServerOAuth2AuthorizationRequestResolver authorizationRequestResolver =
				new DefaultServerOAuth2AuthorizationRequestResolver(
						clientRegistrationRepository);
		authorizationRequestResolver.setAuthorizationRequestCustomizer(
				authorizationRequestCustomizer());

		return  authorizationRequestResolver;
	}

	private Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer() {
		return customizer -> customizer
				.additionalParameters(params -> params.put("prompt", "consent"));
	}
}


