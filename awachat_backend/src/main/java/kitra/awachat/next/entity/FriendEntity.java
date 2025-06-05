package kitra.awachat.next.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 好友关系实体类
 */
@TableName("friend")
public class FriendEntity {
    @TableField("user1")
    private Integer user1;

    @TableField("user2")
    private Integer user2;

    public Integer getUser1() {
        return user1;
    }

    public void setUser1(Integer user1) {
        this.user1 = user1;
    }

    public Integer getUser2() {
        return user2;
    }

    public void setUser2(Integer user2) {
        this.user2 = user2;
    }
}