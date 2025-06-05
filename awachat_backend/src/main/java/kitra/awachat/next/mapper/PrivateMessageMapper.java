package kitra.awachat.next.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import kitra.awachat.next.entity.PrivateMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessageEntity> {
    /**
     * 自定义的私聊消息插入方法，用于规避 MyBatis Plus 不能正确处理自增主键的问题
     */
    int insertPrivateMessage(PrivateMessageEntity privateMessage);
}