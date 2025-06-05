package kitra.awachat.next.dto.websocket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompoundMessagePartType {
    TEXT("text"),
    IMAGE("image");

    private final String value;

    CompoundMessagePartType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}