package kitra.awachat.next.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

@KeySequence(value = "private_chat_seq", dbType = DbType.POSTGRE_SQL)
@TableName("private_chat")
public class PrivateChatEntity {
    @TableId(value = "chat_id", type = IdType.INPUT)
    private Long chatId;

    @TableField("user1_id")
    private Integer user1Id;

    @TableField("user2_id")
    private Integer user2Id;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField("last_message_id")
    private Long lastMessageId;

    // Getters and Setters
    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }

    public Integer getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}