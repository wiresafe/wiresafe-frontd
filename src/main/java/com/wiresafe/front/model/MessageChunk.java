package com.wiresafe.front.model;

import java.util.Collections;
import java.util.List;

public class MessageChunk {

    private String startToken;
    private String endToken;
    private List<Message> messages;

    public MessageChunk(String startToken, String endToken, List<Message> messages) {
        this.startToken = startToken;
        this.endToken = endToken;
        this.messages = Collections.unmodifiableList(messages);
    }

    public String getStartToken() {
        return startToken;
    }

    public String getEndToken() {
        return endToken;
    }

    public List<Message> getMessages() {
        return messages;
    }

}
