package com.wiresafe.front.spring.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wiresafe.front.model.Channel;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.MessageChunk;
import com.wiresafe.front.spring.factory.FrontApiFactory;
import io.kamax.matrix.json.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(path = "/channel", produces = MediaType.APPLICATION_JSON_VALUE)
@Async
public class ChannelController extends BaseController {

    private FrontApi model;

    @Autowired
    public ChannelController(FrontApiFactory factory) {
        this.model = factory.get();
    }

    @GetMapping("/")
    public CompletableFuture<String> listChannels(
            @RequestHeader("X-API-Key") String apiKey
    ) {
        JsonArray channels = new JsonArray();
        model.with(apiKey).getChannels().forEach(channel -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("@id", "/channel/" + channel.getId());
            obj.addProperty("name", channel.getName());
            channels.add(obj);
        });
        return CompletableFuture.completedFuture(toJson(channels));
    }

    @GetMapping("/{channelId}")
    public CompletableFuture<String> getChannel(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String channelId
    ) {
        Channel channel = model.with(apiKey).getChannel(channelId);

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
        return CompletableFuture.completedFuture(toJson(obj));
    }

    @GetMapping("/{channelId}/messages/")
    public CompletableFuture<String> getChannelMessages(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String channelId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String type
    ) {
        from = StringUtils.defaultIfBlank(from, "HEAD");
        direction = StringUtils.defaultIfBlank(direction, "previous");
        MessageChunk chunk = model.with(apiKey).getMessages(channelId, from, direction, type);
        JsonObject res = new JsonObject();
        res.add("messages", GsonUtil.asArrayObj(chunk.getMessages().stream()
                .map(msg -> {
                    JsonObject output = new JsonObject();
                    output.addProperty("@id", "/channel/" + msg.getChannelId() + "/messages/" + msg.getId());
                    output.addProperty("@type", msg.getType());
                    output.addProperty("channelId", "/channel/" + msg.getChannelId());
                    output.addProperty("timestamp", msg.getTimestamp());
                    output.addProperty("sender", "/user/" + msg.getSender());
                    output.addProperty("content", msg.getContent());
                    output.addProperty("mediaId", msg.getMediaId());
                    output.addProperty("filename", msg.getFilename());
                    return output;
                }).collect(Collectors.toList())));

        JsonObject tokens = new JsonObject();
        tokens.addProperty("previous", chunk.getStartToken());
        tokens.addProperty("next", chunk.getEndToken());
        res.add("token", tokens);

        return CompletableFuture.completedFuture(toJson(res));
    }

    @PostMapping("/{channelId}/messages/")
    public CompletableFuture<String> addChanelMessage(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String channelId,
            HttpServletRequest request
    ) {
        JsonObject body = GsonUtil.parseObj(getBody(request));
        String evId = model.with(apiKey).putMessage(channelId, body);

        return CompletableFuture.completedFuture(toJson(GsonUtil.makeObj("id", evId)));
    }

}
