package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.MessageChunk;
import io.kamax.matrix.json.GsonUtil;
import io.undertow.server.HttpServerExchange;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class MessageGetHandler extends HttpServerExchangeHandler {

    public MessageGetHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String id = exchange.getQueryParameters().get("channelId").getFirst();
        String token = exchange.getQueryParameters().computeIfAbsent("from", token1 -> {
            Deque<String> parms = new LinkedList<>();
            parms.add("HEAD");
            return parms;
        }).getFirst();
        String direction = exchange.getQueryParameters().computeIfAbsent("direction", direction1 -> {
            Deque<String> parms = new LinkedList<>();
            parms.add("previous");
            return parms;
        }).getFirst();
        MessageChunk chunk = frontend.with(getAccessToken(exchange)).getMessages(id, token, direction);

        JsonObject res = new JsonObject();
        res.add("messages", GsonUtil.asArrayObj(chunk.getMessages().stream()
                .map(msg -> {
                    JsonObject output = new JsonObject();
                    output.addProperty("@id", "/channel/" + msg.getChannelId() + "/messages/" + msg.getId());
                    output.addProperty("@type", msg.getType());
                    output.addProperty("channelId", "/channel/" + msg.getChannelId());
                    output.addProperty("timestamp", msg.getTimestamp());
                    output.addProperty("sender", "/user/" + msg.getSender());
                    output.addProperty("content", msg.getContent());
                    return output;
                }).collect(Collectors.toList())));

        JsonObject tokens = new JsonObject();
        tokens.addProperty("previous", chunk.getStartToken());
        tokens.addProperty("next", chunk.getEndToken());
        res.add("token", tokens);

        sendJsonResponse(exchange, res);
    }

}
