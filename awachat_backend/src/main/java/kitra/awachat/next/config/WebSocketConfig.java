package kitra.awachat.next.config;

import kitra.awachat.next.handler.ChatWebSocketHandler;
import kitra.awachat.next.session.AuthHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 服务的根配置类。这里用来添加 Handler 和拦截器
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final AuthHandshakeInterceptor authInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatHandler,
                           AuthHandshakeInterceptor authInterceptor) {
        this.chatHandler = chatHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins("*"); // 生产环境应限制具体域名
    }
}
