package kitra.awachat.next;

import kitra.awachat.next.entity.UserEntity;
import kitra.awachat.next.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @Transactional // 确保测试数据不会污染数据库
    public void testUserReadWriteWithJsonb() {
        // 1. 创建一个UserEntity实例
        UserEntity user = new UserEntity();
        user.setUsername("test_user_jsonb");
        user.setNickname("测试用户JSONB");
        user.setPassword("hashed_password");
        user.setDescription("这是一个测试用户");
        user.setAvatar("http://example.com/avatar.png");
        user.setCreatedAt(new Date());
        user.setLastOnlineAt(new Date());
        user.setRole((short) 1);
        user.setBanUntil(null);
        user.setDeleted(false);

        // 设置extendedData，包含JSONB数据
        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put("level", 10);
        extendedData.put("isVip", true);
        extendedData.put("preferences", Map.of("theme", "dark", "notifications", true));
        user.setExtendedData(extendedData);

        // 2. 插入用户数据
        int insertResult = userMapper.insert(user);
        assertTrue(insertResult > 0, "插入用户失败");

        // 3. 根据ID查询用户数据
        UserEntity retrievedUser = userMapper.selectById(user.getUserId());
        assertNotNull(retrievedUser, "查询用户失败");

        // 4. 验证查询到的数据，特别是extendedData字段
        assertEquals(user.getUsername(), retrievedUser.getUsername());
        assertEquals(user.getNickname(), retrievedUser.getNickname());
        assertEquals(user.getDescription(), retrievedUser.getDescription());
        assertEquals(user.getAvatar(), retrievedUser.getAvatar());
        assertEquals(user.getRole(), retrievedUser.getRole());
        assertEquals(user.isDeleted(), retrievedUser.isDeleted());

        // 验证extendedData
        assertNotNull(retrievedUser.getExtendedData(), "extendedData不应为空");
        assertEquals(extendedData.get("level"), retrievedUser.getExtendedData().get("level"));
        assertEquals(extendedData.get("isVip"), retrievedUser.getExtendedData().get("isVip"));
        // 对于嵌套的Map，需要进行深层比较
        assertEquals(extendedData.get("preferences"), retrievedUser.getExtendedData().get("preferences"));

        System.out.println("成功测试UserEntity的JSONB读写：" + retrievedUser.getExtendedData());
    }
}