package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import io.undertow.server.HttpServerExchange;

public class ChannelListHandler extends HttpServerExchangeHandler {

    public ChannelListHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        JsonArray channels = new JsonArray();
        frontend.with(getAccessToken(exchange)).getChannels().forEach(channel -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("@id", "/channel/" + channel.getId());
            obj.addProperty("name", channel.getName());
            channels.add(obj);
        });
        sendJsonResponse(exchange, channels);
    }

}
