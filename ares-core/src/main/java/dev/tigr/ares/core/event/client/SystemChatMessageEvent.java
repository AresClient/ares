package dev.tigr.ares.core.event.client;

public class SystemChatMessageEvent {
    private final String text;

    public SystemChatMessageEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
