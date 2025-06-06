package kitra.awachat.next.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import kitra.awachat.next.entity.PrivateMessageAcknowledgeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivateMessageAcknowledgeMapper extends BaseMapper<PrivateMessageAcknowledgeEntity> {
    int update(PrivateMessageAcknowledgeEntity newMessage);
}