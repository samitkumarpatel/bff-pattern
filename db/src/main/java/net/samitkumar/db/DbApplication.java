package net.samitkumar.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@SpringBootApplication
public class DbApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction(DbRouterHandler dbRouterHandler) {
		return RouterFunctions
				.route()
				.before(request -> {
					log.info("REQUEST {} {}", request.method(), request.path());
					return request;
				})
				.path("/db", dbBuilder -> dbBuilder
						.path("/user", userbuilder -> userbuilder
								.POST("", dbRouterHandler::newUser)
								.GET("", dbRouterHandler::userByUsername)
								.GET("/{id}", dbRouterHandler::userById)
								.PATCH("/{id}", dbRouterHandler::patchUser)
								.PUT("/{id}", dbRouterHandler::updateUser)
						)
				)
				.after((request, response) -> {
					log.info("RESPONSE {} {} {}", request.method(), request.path(), response.statusCode());
					return response;
				})
				.build();
	}
}

@Component
class DbRouterHandler {

	UserRepository userRepository;

	DbRouterHandler(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Mono<ServerResponse> newUser(ServerRequest request) {
		return request
				.bodyToMono(User.class)
				.publishOn(Schedulers.boundedElastic())
				.map(userRepository::save)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> userById(ServerRequest request) {
		var id = Long.parseLong(request.pathVariable("id"));
		return Mono.fromCallable(() -> userRepository.findById(id).orElseThrow(UserNotFoundException::new))
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> patchUser(ServerRequest request) {
		var id = Long.parseLong(request.pathVariable("id"));
		return request.bodyToMono(User.class)
				.zipWith(Mono.fromCallable(() -> userRepository.findById(id).orElseThrow(UserNotFoundException::new)), (userRequest, dbUser) -> {
					return new User(
							dbUser.id(),
							Objects.nonNull(userRequest.username()) ? userRequest.username() : dbUser.username(),
							Objects.nonNull(userRequest.password()) ? userRequest.password() : dbUser.password(),
							dbUser.activate() == userRequest.activate() ?  dbUser.activate() : userRequest.activate(),
							dbUser.doj(),
							CollectionUtils.isEmpty(userRequest.roles()) ? dbUser.roles() : userRequest.roles()
					);
				})
				.map(userRepository::save)
				.flatMap(ServerResponse.ok()::bodyValue);

	}

	public Mono<ServerResponse> updateUser(ServerRequest request) {
		var id = Long.parseLong(request.pathVariable("id"));
		return request.bodyToMono(User.class)
				.flatMap(user -> Mono.fromCallable(() -> userRepository.findById(id).orElseThrow(UserNotFoundException::new)).thenReturn(user))
				.map(user -> new User(id, user.username(), user.password(), user.activate(), null, user.roles()))
				.map(userRepository::save)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> userByUsername(ServerRequest request) {
		var username = request.queryParam("username").orElseThrow(InvalidQueryParamException::new);
		return Mono.fromCallable(() -> userRepository.findUserByUsername(username).orElseThrow(UserNotFoundException::new))
				.flatMap(ServerResponse.ok()::bodyValue);
	}
}

@Table("users")
record User(@Id Long id, String username, String password, boolean activate, @ReadOnlyProperty LocalDate doj, @MappedCollection(idColumn = "id", keyColumn = "id") Set<Roles> roles) {}
enum Role { ADMIN, READ, WRITE }

@Table
record Roles(Long id, Role role) {}

interface UserRepository extends ListCrudRepository<User, Long> {
	Optional<User> findUserByUsername(String username);
}

@Table("messages")
record Message(@Id Long id, Long senderId, Long receiverId, String content, @ReadOnlyProperty LocalDate createdAt, @ReadOnlyProperty Boolean isRead){}

interface MessageRepository extends ListCrudRepository<Message, Long> {}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {
	UserNotFoundException() {}
	UserNotFoundException(String message) {
		super(message);
	}
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidQueryParamException extends RuntimeException {
	InvalidQueryParamException() {}
}