package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.SyncChunk;
import io.kamax.matrix.json.GsonUtil;
import io.undertow.server.HttpServerExchange;

import java.util.stream.Collectors;

public class SyncHandler extends HttpServerExchangeHandler {

    public SyncHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String since = findParameter(exchange, "since").orElse(null);
        SyncChunk chunk = frontend.with(getAccessToken(exchange)).sync(since);

        JsonObject channels = new JsonObject();
        channels.add("join", GsonUtil.asArray(chunk.getJoined()));
        channels.add("leave", GsonUtil.asArray(chunk.getLeft()));

        JsonObject res = new JsonObject();
        res.add("channel", channels);
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
        res.addProperty("nextToken", chunk.getNextToken());

        sendJsonResponse(exchange, res);
    }

}
