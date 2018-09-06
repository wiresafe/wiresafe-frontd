package com.wiresafe.front.undertow;

import com.wiresafe.front.matrix.SynapseConfig;
import com.wiresafe.front.model.Config;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.undertow.handlers.*;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.HttpString;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class UndertowFrontd {

    public static void main(String[] args) {
        try {
            int port = Optional.ofNullable(System.getenv("FRONTD_HTTP_PORT")).map(Integer::parseInt).orElse(9000);
            URL csEndpoint = new URL(Objects.requireNonNull(System.getenv("FRONTD_MATRIX_CS_ENDPOINT"), "A Home server endpoint must be provided"));
            URL isEndpoint = new URL(Objects.requireNonNull(System.getenv("FRONTD_MATRIX_IS_ENDPOINT"), "An Identity server endpoint must be provided"));
            String sharedSecret = Objects.requireNonNull(System.getenv("FRONTD_MATRIX_SYNAPSE_SHARED_SECRET"));
            // String passwd = System.getenv("FRONTD_MATRIX_CS_PASSWORD");

            SynapseConfig sCfg = new SynapseConfig();
            sCfg.getApi().setSharedSecret(sharedSecret);

            Config cfg = new Config();
            cfg.setCsEndpoint(csEndpoint);
            cfg.setIsEndpoint(isEndpoint);
            cfg.setSynapseCfg(sCfg);

            FrontApi frontend = new FrontApi(cfg);

            Undertow gwSrv = Undertow.builder()
                    .addHttpListener(port, "0.0.0.0")
                    .setHandler(Handlers.routing()
                            .add("OPTIONS", "*", exchange -> {
                                exchange.setStatusCode(200);
                                exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Origin"), "*");
                                exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Headers"), "origin, content-type, accept, authorization, x-api-key");
                                exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Credentials"), "true");
                                exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Methods"), "GET, POST, PUT, DELETE, OPTIONS, HEAD");
                                exchange.endExchange();
                            })
                            .add("POST", "/auth/login", new BlockingHandler(new AuthLoginHandler(frontend)))
                            .add("GET", "/auth/logout", new BlockingHandler(new AuthLogoutHandler(frontend)))
                            .add("GET", "/user/{userId}", new BlockingHandler(new UserGetHandler(frontend)))
                            .add("GET", "/channel/", new BlockingHandler(new ChannelListHandler(frontend)))
                            .add("GET", "/channel/{channelId}", new BlockingHandler(new ChannelGetHandler(frontend)))
                            .add("GET", "/channel/{channelId}/messages/", new BlockingHandler(new MessageGetHandler(frontend)))
                            .add("POST", "/channel/{channelId}/messages/", new BlockingHandler(new MessagePostHandler(frontend)))
                            .add("GET", "/sync", new BlockingHandler(new SyncHandler(frontend))))
                    .build();

            gwSrv.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
