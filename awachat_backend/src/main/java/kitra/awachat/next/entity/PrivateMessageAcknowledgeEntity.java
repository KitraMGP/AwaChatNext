package kitra.awachat.next.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("private_message_acknowledge")
public class PrivateMessageAcknowledgeEntity {
    @TableField("chat_id")
    private Long chatId;

    @TableField("user_id")
    private Integer userId;

    @TableField("last_message_id")
    private Long lastMessageId;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}