package kitra.awachat.next.dto.user;

import java.util.Date;
import java.util.Map;

/**
 * 用户基本信息。用作登录和获取用户信息接口的额返回数据
 */
public record UserDataResponse(int userId, String username, String nickname, String description, String avatar,
                               Date createdAt, Date lastOnlineAt, short role, Date banUntil,
                               Map<String, Object> extendedData) {
}
