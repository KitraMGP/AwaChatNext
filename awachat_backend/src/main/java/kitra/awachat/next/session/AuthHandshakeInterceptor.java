package kitra.awachat.next.session;

import cn.dev33.satoken.stp.StpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final Logger logger = LogManager.getLogger(AuthHandshakeInterceptor.class);

    /**
     * 建立连接之前，进行验证。
     * <p>
     * 验证方式是，从请求 URL 中提取 token，然后进行检验
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // 1. 从请求中获取token (例如: ws://localhost:8081/ws/chat?token=xxx)
            String query = request.getURI().getQuery();
            if (query == null) { // 没有参数
                logger.warn("WebSocket握手失败：缺少查询参数");
                return false;
            }

            String token = extractTokenFromQuery(query);
            if (token == null) {
                logger.warn("WebSocket握手失败：未找到token参数");
                return false;
            }

            // 2. 验证token并获取用户ID - 使用不依赖上下文的方法
            // TODO 对比原方案和现在的修改，解释到底是如何解决上下文未初始化的问题的
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                logger.warn("WebSocket握手失败：token验证失败");
                return false; // 认证失败，拒绝连接
            }

            // 确保loginId是Integer类型
            Integer userId;
            if (loginId instanceof Integer) {
                userId = (Integer) loginId;
            } else if (loginId instanceof String) {
                try {
                    userId = Integer.parseInt((String) loginId);
                } catch (NumberFormatException e) {
                    logger.warn("WebSocket握手失败：用户ID格式错误");
                    return false;
                }
            } else {
                logger.warn("WebSocket握手失败：用户ID类型不支持");
                return false;
            }

            // 3. 将用户ID存入会话属性
            attributes.put("userId", userId);
            logger.info("WebSocket握手成功：用户ID={}", userId);
            return true;

        } catch (Exception e) {
            // 捕获所有异常，避免Sa-Token上下文问题导致握手失败
            logger.error("WebSocket握手验证失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
    }

    private String extractTokenFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}
