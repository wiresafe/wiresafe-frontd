package com.wiresafe.front.model;

import java.util.List;

public class SyncChunk {

    private List<String> joined;
    private List<String> left;
    private List<Message> messages;
    private String nextToken;

    public SyncChunk(List<String> joined, List<String> left, List<Message> messages, String nextToken) {
        this.joined = joined;
        this.left = left;
        this.messages = messages;
        this.nextToken = nextToken;
    }

    public List<String> getJoined() {
        return joined;
    }

    public List<String> getLeft() {
        return left;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getNextToken() {
        return nextToken;
    }

}
