package com.badasstechie.infinitychat.WebSocket;

import com.badasstechie.infinitychat.AppUser.AppUser;
import com.badasstechie.infinitychat.AppUser.AppUserRepository;
import com.badasstechie.infinitychat.Utils.Urls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketMessageConfig implements WebSocketMessageBrokerConfigurer {
    private final AppUserRepository appUserRepository;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public WebSocketMessageConfig(AppUserRepository appUserRepository, JwtDecoder jwtDecoder) {
        this.appUserRepository = appUserRepository;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins(Urls.WEBSOCKET_ALLOWED_ORIGINS)
                .withSockJS();

        registry
                .addEndpoint("/ws")
                .setAllowedOrigins(Urls.WEBSOCKET_ALLOWED_ORIGINS);
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry
                .setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authToken = accessor.getFirstNativeHeader("access-token");
                    Jwt jwt = jwtDecoder.decode(authToken);
                    AppUser appUser = appUserRepository.findByUsername(jwt.getSubject()).orElseThrow(
                            () -> new RuntimeException("Authenticated user not found")
                    );
                    Authentication authentication = new JwtAuthenticationToken(jwt, appUser.getAuthorities());
                    accessor.setUser(authentication);
                    log.info("User {} connected", appUser.getUsername());
                }
                return message;
            }
        });
    }
}
