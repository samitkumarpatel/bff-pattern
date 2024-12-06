package net.samitkumar.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes() {
		return RouterFunctions
				.route()
				.GET("/api", request -> ServerResponse.ok().bodyValue(Map.of("message", "Hello from api")))
				.build();
	}

}
