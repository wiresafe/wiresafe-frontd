package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wiresafe.front.model.Channel;
import com.wiresafe.front.model.FrontApi;
import io.undertow.server.HttpServerExchange;

public class ChannelGetHandler extends HttpServerExchangeHandler {

    public ChannelGetHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String id = exchange.getQueryParameters().get("channelId").getFirst();
        Channel channel = frontend.with(getAccessToken(exchange)).getChannel(id);

        JsonArray members = new JsonArray();
        channel.getMembers().forEach(member -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("@id", "/user/" + member.getId());
            obj.addProperty("name", member.getName());
            members.add(obj);
        });

        JsonObject obj = new JsonObject();
        obj.addProperty("@id", "/channel/" + channel.getId());
        obj.addProperty("name", channel.getName());
        obj.add("members", members);
        obj.addProperty("messageToken", "HEAD");
        sendJsonResponse(exchange, obj);
    }

}
