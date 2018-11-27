package com.wiresafe.front.spring.controller;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.User;
import com.wiresafe.front.spring.factory.FrontApiFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin
@RequestMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Async
public class UserController extends BaseController {

    private FrontApi model;

    @Autowired
    public UserController(FrontApiFactory factory) {
        this.model = factory.get();
    }

    @GetMapping("/{userId}")
    public CompletableFuture<String> getUser(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String userId
    ) {
        User user = model.with(apiKey).getUser(userId);

        JsonObject res = new JsonObject();
        res.addProperty("@id", "/user/" + user.getId());
        res.addProperty("name", user.getName());

        return CompletableFuture.completedFuture(toJson(res));
    }

}
