package net.samitkumar.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SpringBootApplication
@EnableWebFluxSecurity
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
		DefaultServerOAuth2AuthorizationRequestResolver pkceResolver = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
		pkceResolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
		http
				.headers(headers -> headers.contentSecurityPolicy(Customizer.withDefaults()))
				.authorizeExchange((authorize) -> authorize
						.anyExchange().authenticated()
				)
				.oauth2Login(login -> login
						.authorizationRequestResolver(pkceResolver)
				)
				.csrf((csrf) -> csrf
						.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
				);
		return http.build();
	}

}

class CsrfCookieWebFilter implements WebFilter {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String key = CsrfToken.class.getName();
		Mono<CsrfToken> csrfToken = null != exchange.getAttribute(key) ? exchange.getAttribute(key) : Mono.empty();
		assert csrfToken != null;
		return csrfToken.doOnSuccess(token -> {
			ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", token.getToken()).maxAge(Duration.ofHours(1))
					.httpOnly(false).path("/").sameSite(Cookie.SameSite.LAX.attributeValue()).build();
			exchange.getResponse().getCookies().add("XSRF-TOKEN", cookie);
		}).then(chain.filter(exchange));
	}
}