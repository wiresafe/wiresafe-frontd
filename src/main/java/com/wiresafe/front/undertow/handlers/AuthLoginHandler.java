package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import io.kamax.matrix.json.GsonUtil;
import io.undertow.server.HttpServerExchange;

public class AuthLoginHandler extends HttpServerExchangeHandler {

    public AuthLoginHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        JsonObject obj = getBodyObj(exchange);
        String userId = GsonUtil.getStringOrThrow(obj, "userId");
        String token = frontend.login(userId);

        sendJsonResponse(exchange, GsonUtil.makeObj("accessToken", token));
    }

}
