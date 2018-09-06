package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import io.undertow.server.HttpServerExchange;

public class AuthLogoutHandler extends HttpServerExchangeHandler {

    public AuthLogoutHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        frontend.with(getAccessToken(exchange)).logout();
        sendJsonResponse(exchange, new JsonObject());
    }

}
