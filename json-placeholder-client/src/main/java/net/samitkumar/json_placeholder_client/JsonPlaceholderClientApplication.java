package net.samitkumar.json_placeholder_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootApplication
public class JsonPlaceholderClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(JsonPlaceholderClientApplication.class, args);
	}

	@Bean
	JsonPlaceholderClient jsonPlaceholderClient(WebClient.Builder webClientBuilder) {
		WebClientAdapter adapter = WebClientAdapter.create(webClientBuilder.baseUrl("https://jsonplaceholder.typicode.com").build());
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(JsonPlaceholderClient.class);
	}

	@Bean
	RouterFunction<ServerResponse> routes(JsonPlaceHolderHandler jsonPlaceHolderHandler) {
		return RouterFunctions
				.route()
				.path("/users", builder -> builder
						.GET("/search", jsonPlaceHolderHandler::searchUser)
						.GET("", jsonPlaceHolderHandler::getAllUsers)
						.GET("/{id}", jsonPlaceHolderHandler::getUserById)

				)
				.build();
	}
}

/*@Configuration
@EnableWebFluxSecurity
class WebfluxSecurityConfig {
	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
		return http
				.cors(ServerHttpSecurity.CorsSpec::disable)
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
						.pathMatchers(HttpMethod.OPTIONS).permitAll() // Allow OPTIONS requests without authentication
						.anyExchange().authenticated()
				)
				.oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()))
				.build();
	}
}*/

record User(int id, String name, String username, String email, String phone, String website, Address address, Company company) {}
record Address(String street, String suite, String city, String zipcode, Geo geo) {}
record Geo(String lat, String lng) {}
record Company(String name, String catchPhrase, String bs) {}

@HttpExchange(accept = "application/json")
interface JsonPlaceholderClient {

	@GetExchange("/users")
	Mono<List<User>> getAllUsers();

	@GetExchange("/users/{id}")
	Mono<User> getUserById(@PathVariable int id);
}

@Component
class JsonPlaceHolderHandler {
	JsonPlaceholderClient jsonPlaceholderClient;

	public JsonPlaceHolderHandler(JsonPlaceholderClient jsonPlaceholderClient) {
		this.jsonPlaceholderClient = jsonPlaceholderClient;
	}

	public Mono<ServerResponse> getAllUsers(ServerRequest request) {
		return jsonPlaceholderClient
				.getAllUsers()
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> getUserById(ServerRequest request) {
		return jsonPlaceholderClient
				.getUserById(Integer.parseInt(request.pathVariable("id")))
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> searchUser(ServerRequest request) {
		var text = request.queryParam("text").orElseThrow(() -> new IllegalArgumentException("text is required"));
		if (text.contains(":")) {
			String[] searchText = text.split(":");
			return switch (searchText[0]) {
				case "name" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.name().contains(searchText[1]))
								.toList()));
				case "username" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.username().contains(searchText[1]))
								.toList()));
				case "email" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.email().contains(searchText[1]))
								.toList()));
				case "phone" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.phone().contains(searchText[1]))
								.toList()));
				case "website" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.website().contains(searchText[1]))
								.toList()));
				case "address" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.address().toString().contains(searchText[1]))
								.toList()));
				case "company" -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.company().toString().contains(searchText[1]))
								.toList()));
				default -> jsonPlaceholderClient.getAllUsers()
						.flatMap(users -> ServerResponse.ok().bodyValue(users.stream()
								.filter(user -> user.toString().contains(searchText[1]))
								.toList()));
			};
		} else {
			return ServerResponse.badRequest().bodyValue("Invalid search query");
		}
	}
}