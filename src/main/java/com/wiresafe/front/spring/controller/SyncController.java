package com.wiresafe.front.spring.controller;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.SyncChunk;
import com.wiresafe.front.spring.factory.FrontApiFactory;
import io.kamax.matrix.json.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SyncController extends BaseController {

    private FrontApi model;

    @Autowired
    public SyncController(FrontApiFactory factory) {
        this.model = factory.get();
    }

    @GetMapping("/sync")
    public String getData(HttpServletRequest request, @RequestParam(required = false) String since) {
        SyncChunk chunk = model.with(getAccessToken(request)).sync(since);

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

        return toJson(res);
    }

}
