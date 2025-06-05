package kitra.awachat.next.dto.chat;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatType {
    PRIVATE("private"),
    GROUP("group");

    private final String value;

    // 构造函数
    ChatType(String value) {
        this.value = value;
    }

    /**
     * 根据字符串获取相应的枚举值
     */
    public static ChatType fromString(String text) {
        for (ChatType chatType : ChatType.values()) {
            if (chatType.toString().equalsIgnoreCase(text)) {
                return chatType;
            }
        }
        throw new IllegalArgumentException("No constant with text: " + text);
    }

    // 获取枚举的字符串表示
    @JsonValue // 添加此注解使Jackson序列化时使用此方法的返回值
    @Override
    public String toString() {
        return this.value;
    }
}
