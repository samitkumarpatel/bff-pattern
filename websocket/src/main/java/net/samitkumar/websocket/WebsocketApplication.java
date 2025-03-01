package net.samitkumar.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@SpringBootApplication
public class WebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}

}

record Message(String from, String to, String text) {}

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/stomp-endpoint")
				.setAllowedOriginPatterns("*")
				.withSockJS()
				.setHeartbeatTime(60_000);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue/", "/topic/");
		// STOMP messages whose destination header begins with /app are routed to @MessageMapping methods in @Controller classes
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}
}

@Controller
@Slf4j
@RequiredArgsConstructor
class WebSocketController {

	final SimpMessagingTemplate messagingTemplate;
	final SimpMessageSendingOperations messagingSendingTemplate;

	@MessageMapping("/private")
	@SendToUser("/queue/private")
	public Message sendMessageToUser(@Payload Message message, @Headers Map<Object, Object> headers, Principal principal) {
		var from = principal.getName();
		var to = message.to();
		log.info("sendMessageToUser sendTo: {} userMessage: {} Headers: {}", message.to(), message, headers);
		messagingTemplate.convertAndSendToUser(to, "/queue/private", message);
		return message;
	}

	@MessageMapping("/public")
	@SendTo("/topic/public")
	public Message sendGreetingToUser(@Payload Message message, @Headers Map<Object, Object> headers, Principal principal) {
		log.info("Public Message Principle {} Headers: {}, {}", principal, headers, message);
		return message;
	}
}