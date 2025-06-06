package kitra.awachat.next.dto.websocket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatMessageType {
    TEXT("text"),
    COMPOUND("compound"),
    FRIEND_REQUEST("friend_request");

    private final String value;

    ChatMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}