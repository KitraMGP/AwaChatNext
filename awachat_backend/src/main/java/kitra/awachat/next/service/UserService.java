package kitra.awachat.next.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import kitra.awachat.next.entity.UserEntity;
import kitra.awachat.next.exception.BadInputException;
import kitra.awachat.next.exception.LoginFailedException;
import kitra.awachat.next.exception.UserAlreadyExistsException;
import kitra.awachat.next.exception.UserNotFoundException;
import kitra.awachat.next.mapper.UserMapper;
import kitra.awachat.next.util.UserInfoUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import static kitra.awachat.next.util.DataBaseUtil.checkResult;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 登录功能
     */
    public UserEntity login(String username, String password) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserEntity userEntity = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (userEntity == null) {
            throw new LoginFailedException();
        }
        if (!BCrypt.checkpw(password, userEntity.getPassword())) {
            throw new LoginFailedException();
        }
        return userEntity;
    }

    /**
     * 用户注册
     */
    public void register(String username, String nickname, String password) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserEntity userEntity = userMapper.selectOne(queryWrapper);
        // 用户已存在
        if (userEntity != null) {
            throw new UserAlreadyExistsException();
        }
        // 对输入进行检验
        if (!UserInfoUtil.validateNickname(nickname)) {
            throw new BadInputException("昵称");
        }
        if (!UserInfoUtil.validateUsername(username)) {
            throw new BadInputException("用户名");
        }
        if (!UserInfoUtil.validatePassword(password)) {
            throw new BadInputException("密码");
        }
        // 加密密码并插入数据库
        String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(encryptedPassword);

        // 只调用一次 insert 方法，并将返回值传递给 checkResult
        int insertResult = userMapper.insertUser(user);
        checkResult(insertResult);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    public UserEntity getUserById(int userId) {
        UserEntity userEntity = userMapper.selectById(userId);
        if (userEntity == null) {
            throw new UserNotFoundException();
        }
        return userEntity;
    }
}
