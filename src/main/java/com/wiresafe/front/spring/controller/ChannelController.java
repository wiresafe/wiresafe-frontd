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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(path = "/channel", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChannelController extends BaseController {

    private FrontApi model;

    @Autowired
    public ChannelController(FrontApiFactory factory) {
        this.model = factory.get();
    }

    @GetMapping("/")
    public String listChannels(HttpServletRequest request) {
        JsonArray channels = new JsonArray();
        model.with(getAccessToken(request)).getChannels().forEach(channel -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("@id", "/channel/" + channel.getId());
            obj.addProperty("name", channel.getName());
            channels.add(obj);
        });
        return toJson(channels);
    }

    @GetMapping("/{channelId}")
    public String getChannel(HttpServletRequest request, @PathVariable String channelId) {
        Channel channel = model.with(getAccessToken(request)).getChannel(channelId);

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
        return toJson(obj);
    }

    @GetMapping("/{channelId}/messages/")
    public String getChannelMessages(
            HttpServletRequest request,
            @PathVariable String channelId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String type
    ) {
        from = StringUtils.defaultIfBlank(from, "HEAD");
        direction = StringUtils.defaultIfBlank(direction, "previous");
        MessageChunk chunk = model.with(getAccessToken(request)).getMessages(channelId, from, direction, type);
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

        return toJson(res);
    }

    @PostMapping("/{channelId}/messages/")
    public String addChanelMessage(HttpServletRequest request, @PathVariable String channelId) {
        JsonObject body = GsonUtil.parseObj(getBody(request));
        String evId = model.with(getAccessToken(request)).putMessage(channelId, body);

        return toJson(GsonUtil.makeObj("id", evId));
    }

}
