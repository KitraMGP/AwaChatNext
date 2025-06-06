package kitra.awachat.next.entity;

import com.baomidou.mybatisplus.annotation.*;
import kitra.awachat.next.config.JsonbTypeHandler;

import java.util.Date;
import java.util.Map;

@KeySequence(value = "private_message_seq", dbType = DbType.POSTGRE_SQL)
@TableName(value = "private_message", autoResultMap = true)
public class PrivateMessageEntity {
    @TableId(value = "message_id", type = IdType.INPUT)
    private Long messageId;

    @TableField("chat_id")
    private Long chatId;

    @TableField("sender_id")
    private Integer senderId;

    @TableField("receiver_id")
    private Integer receiverId;

    @TableField(value = "content", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> content;

    /**
     * 0 为普通文本消息，1 为复合消息，2 为好友请求消息
     */
    @TableField("content_type")
    private Short contentType;

    @TableField("reply_to")
    private Long replyTo;

    @TableField("sent_at")
    private Date sentAt;

    @TableField("is_deleted")
    private Boolean isDeleted;

    // Getters and Setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public Short getContentType() {
        return contentType;
    }

    public void setContentType(Short contentType) {
        this.contentType = contentType;
    }

    public Long getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Long replyTo) {
        this.replyTo = replyTo;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}