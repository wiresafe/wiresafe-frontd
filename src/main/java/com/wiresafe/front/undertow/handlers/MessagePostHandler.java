package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import io.kamax.matrix.json.GsonUtil;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;

public class MessagePostHandler extends HttpServerExchangeHandler {

    public MessagePostHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws IOException {
        String id = exchange.getQueryParameters().get("channelId").getFirst();
        JsonObject body = getBodyObj(exchange);
        String evId = frontend.with(getAccessToken(exchange)).putMessage(id, GsonUtil.getStringOrThrow(body, "content"));

        sendJsonResponse(exchange, GsonUtil.makeObj("id", evId));
    }

}
