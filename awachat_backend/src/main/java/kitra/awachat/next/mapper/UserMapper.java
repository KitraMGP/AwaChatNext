package kitra.awachat.next.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import kitra.awachat.next.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    /**
     * 自定义的用户插入方法，用于规避 MyBatis Plus 不能正确处理自增主键的问题
     */
    int insertUser(UserEntity user);
}