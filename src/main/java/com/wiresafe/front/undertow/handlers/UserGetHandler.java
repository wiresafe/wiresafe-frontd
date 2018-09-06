package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.User;
import io.undertow.server.HttpServerExchange;

public class UserGetHandler extends HttpServerExchangeHandler {

    public UserGetHandler(FrontApi frontend) {
        super(frontend);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String id = exchange.getQueryParameters().get("userId").getFirst();
        User user = frontend.with(getAccessToken(exchange)).getUser(id);

        JsonObject res = new JsonObject();
        res.addProperty("@id", "/user/" + user.getId());
        res.addProperty("name", user.getName());

        sendJsonResponse(exchange, res);
    }

}
