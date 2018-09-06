package com.wiresafe.front.undertow.handlers;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import io.kamax.matrix.json.GsonUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public abstract class HttpServerExchangeHandler implements HttpHandler {

    protected final FrontApi frontend;

    public HttpServerExchangeHandler(FrontApi frontend) {
        this.frontend = frontend;
    }

    protected String getBody(HttpServerExchange exchange) throws IOException {
        return IOUtils.toString(exchange.getInputStream(), StandardCharsets.UTF_8);
    }

    protected JsonObject getBodyObj(HttpServerExchange exchange) throws IOException {
        return GsonUtil.parseObj(getBody(exchange));
    }

    protected void sendJsonResponse(HttpServerExchange exchange, String body) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Headers"), "origin, content-type, accept, authorization, x-api-key");
        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Credentials"), "true");
        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Methods"), "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "application/json");
        exchange.setResponseContentLength(body.length());
        exchange.getResponseSender().send(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
    }

    protected void sendJsonResponse(HttpServerExchange exchange, Object o) {
        sendJsonResponse(exchange, GsonUtil.get().toJson(o));
    }

    protected Optional<String> findParameter(HttpServerExchange exchange, String name) {
            Deque<String> parameters = exchange.getQueryParameters().get(name);
            if (Objects.isNull(parameters) || parameters.size() < 1) {
                return Optional.empty();
            }

            if (parameters.size() > 1) {
                throw new IllegalArgumentException("Several query parameters " + name + "supplied");
            }

            return Optional.ofNullable(parameters.getFirst());
    }

    protected String getParameter(HttpServerExchange exchange, String name) {
        return findParameter(exchange, name).orElseThrow(() -> new IllegalArgumentException("No query parameter " + name + "supplied"));
    }

    protected String getHeader(HttpServerExchange exchange, String name) {
        Deque<String> headers = exchange.getRequestHeaders().get(name);
        if (Objects.isNull(headers) || headers.size() < 1) {
            throw new IllegalArgumentException("No header " + name + " supplied");
        }

        if (headers.size() > 1) {
            throw new IllegalArgumentException("Several headers " + name + "supplied");
        }

        return headers.getFirst();
    }

    protected String getAccessToken(HttpServerExchange exchange) {
        return getHeader(exchange, "X-API-Key");
    }

}
