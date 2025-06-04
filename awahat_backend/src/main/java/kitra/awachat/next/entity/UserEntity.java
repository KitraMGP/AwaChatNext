package kitra.awachat.next.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import kitra.awachat.next.config.JsonbTypeHandler;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.Map;

@TableName(value = "\"user\"", autoResultMap = true)
public class UserEntity {
    @TableId(value = "user_id")
    private int userId;
    private String username;
    private String nickname;
    private String password;
    private String description;
    @Nullable
    private String avatar;
    @TableField(value = "created_at")
    private Date createdAt;
    @Nullable
    @TableField(value = "last_online_at")
    private Date lastOnlineAt;
    private short role;
    @Nullable
    @TableField(value = "ban_until")
    private Date banUntil;
    @Nullable
    @TableField(value = "extended_data", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> extendedData;
    @TableField(value = "is_deleted")
    private boolean isDeleted;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getBanUntil() {
        return banUntil;
    }

    public void setBanUntil(Date banUntil) {
        this.banUntil = banUntil;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(Map<String, Object> extendedData) {
        this.extendedData = extendedData;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Date getLastOnlineAt() {
        return lastOnlineAt;
    }

    public void setLastOnlineAt(Date lastOnlineAt) {
        this.lastOnlineAt = lastOnlineAt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public short getRole() {
        return role;
    }

    public void setRole(short role) {
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
