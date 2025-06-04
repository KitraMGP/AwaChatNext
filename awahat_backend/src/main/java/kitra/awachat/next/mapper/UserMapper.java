package kitra.awachat.next.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import kitra.awachat.next.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // 您可以在这里添加自定义的查询方法
}