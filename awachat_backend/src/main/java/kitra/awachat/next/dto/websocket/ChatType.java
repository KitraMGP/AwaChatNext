package kitra.awachat.next.dto.websocket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatType {
    PRIVATE("private"),
    GROUP("group");

    private final String value;

    ChatType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
