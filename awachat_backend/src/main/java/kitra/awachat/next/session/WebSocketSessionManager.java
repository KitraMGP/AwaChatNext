package kitra.awachat.next.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理 WebSocket 连接与用户之间的对应关系，实现一个会话管理器
 */
@Component
public class WebSocketSessionManager {
    // 用户ID -> Session 映射 (支持单用户多设备)
    private final ConcurrentMap<Integer, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    // Session ID -> 用户ID 映射
    private final ConcurrentMap<String, Integer> sessionToUser = new ConcurrentHashMap<>();

    /**
     * 向会话管理器中添加新的 Session，在新的 WebSocket 连接建立的时候调用
     *
     * @param userId  Session 对应的用户ID
     * @param session WebSocketSession 实例
     */
    public void addSession(Integer userId, WebSocketSession session) {
        userSessions.compute(userId, (key, sessions) -> {
            if (sessions == null) sessions = ConcurrentHashMap.newKeySet();
            sessions.add(session);
            return sessions;
        });
        sessionToUser.put(session.getId(), userId);
    }

    /**
     * 从会话管理器中移除 Session，在连接终止的时候调用
     *
     * @param session WebSocketSession 实例
     */
    public void removeSession(WebSocketSession session) {
        Integer userId = sessionToUser.remove(session.getId());
        if (userId != null) {
            // 因为一个用户 ID 对应的是一个 Set，所以要对该 Set 进行操作
            userSessions.computeIfPresent(userId, (key, sessions) -> {
                sessions.remove(session);
                return sessions.isEmpty() ? null : sessions;
            });
        }
    }

    /**
     * 获取一个用户的所有会话
     *
     * @param userId 用户ID
     * @return 含有该用户所有 WebSocketSession 的 Set
     */
    public Set<WebSocketSession> getSessionsByUser(Integer userId) {
        return userSessions.getOrDefault(userId, Collections.emptySet());
    }

    /**
     * 获取一个 WebSocketSession 对应的用户ID
     *
     * @param sessionId WebSocketSession 实例
     * @return 用户ID
     */
    public Integer getUserIdBySession(String sessionId) {
        return sessionToUser.get(sessionId);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     */
    public boolean isUserOnline(Integer userId) {
        return userSessions.containsKey(userId);
    }
}
