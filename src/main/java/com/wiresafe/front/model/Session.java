package com.wiresafe.front.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wiresafe.front.HexUtil;
import com.wiresafe.front.exception.InvalidArgumentException;
import io.kamax.matrix._MatrixContent;
import io.kamax.matrix.client._SyncData;
import io.kamax.matrix.client.regular.MatrixHttpClient;
import io.kamax.matrix.client.regular.SyncOptions;
import io.kamax.matrix.event.EventKey;
import io.kamax.matrix.hs.RoomMembership;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.matrix.json.event.MatrixJsonRoomMembershipEvent;
import io.kamax.matrix.json.event.MatrixJsonRoomMessageEvent;
import io.kamax.matrix.json.event.MatrixJsonRoomNameEvent;
import io.kamax.matrix.room.MatrixRoomMessageChunkOptions;
import io.kamax.matrix.room.RoomCreationOptions;
import io.kamax.matrix.room._MatrixRoomMessageChunk;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Session {

    private MatrixHttpClient client;

    public Session(MatrixHttpClient client) {
        this.client = client;
    }

    public void logout() {
        client.logout();
    }

    public User getUser(String id) {
        return new User(client, id);
    }

    private Message build(MatrixJsonRoomMessageEvent ev, String roomId) {
        Message msg = new Message();
        msg.setId(HexUtil.encode(ev.getId()));
        msg.setChannelId(HexUtil.encode(roomId));
        msg.setTimestamp(ev.getTime().toEpochMilli());
        msg.setSender(HexUtil.encode(ev.getSender().getId()));
        if ("m.text".equalsIgnoreCase(ev.getBodyType())) {
            msg.setType("Text");
            msg.setContent(ev.getFormattedBody().orElseGet(ev::getBody));
        }

        if ("m.file".equalsIgnoreCase(ev.getBodyType())) {
            JsonObject content = GsonUtil.getObj(ev.getJson(), "content");
            msg.setType("Attachment");
            msg.setMediaId(HexUtil.encode(GsonUtil.getStringOrThrow(content, "url")));
            msg.setContent(ev.getBody());
            GsonUtil.findString(content, "filename").ifPresent(msg::setFilename);
        }

        return msg;
    }

    public List<Channel> getChannels() {
        JsonArray wildcard = new JsonArray();
        wildcard.add("*");

        JsonObject excludeAllTypesFilter = new JsonObject();
        excludeAllTypesFilter.add("not_types", wildcard);

        JsonObject roomFilter = new JsonObject();
        roomFilter.add("ephemeral", excludeAllTypesFilter);
        roomFilter.add("account_data", excludeAllTypesFilter);
        roomFilter.add("timeline", excludeAllTypesFilter);

        JsonObject filter = new JsonObject();
        filter.add("presence", excludeAllTypesFilter);
        filter.add("account_data", excludeAllTypesFilter);
        SyncOptions opts = SyncOptions.build().setFilter(GsonUtil.get().toJson(filter)).get();

        _SyncData sync = client.sync(opts);
        return sync.getRooms().getJoined().stream().map(room -> {
            String id = HexUtil.encode(room.getId());
            String name = Stream.concat(room.getState().getEvents().stream(), room.getTimeline().getEvents().stream())
                    .filter(ev -> "m.room.name".equals(ev.getType()))
                    .map(ev -> new MatrixJsonRoomNameEvent(ev.getJson()).getName().orElse(null))
                    .findFirst()
                    .orElse(null);
            return new Channel(id, name, Collections.emptyList());
        }).collect(Collectors.toList());
    }

    public Channel getChannel(String id) {
        JsonArray wildcard = new JsonArray();
        wildcard.add("*");

        JsonObject excludeAllTypesFilter = new JsonObject();
        excludeAllTypesFilter.add("not_types", wildcard);

        JsonArray stateEventTypes = new JsonArray();
        stateEventTypes.add("m.room.name");
        stateEventTypes.add("m.room.member");
        JsonObject roomEventFilter = GsonUtil.makeObj("types", stateEventTypes);

        JsonArray roomIds = new JsonArray();
        roomIds.add(HexUtil.decode(id));

        JsonObject roomFilter = new JsonObject();
        roomFilter.add("rooms", roomIds);
        roomFilter.add("ephemeral", excludeAllTypesFilter);
        roomFilter.add("account_data", excludeAllTypesFilter);
        roomFilter.add("timeline", roomEventFilter);
        roomFilter.add("state", roomEventFilter);

        JsonObject filter = new JsonObject();
        filter.add("presence", excludeAllTypesFilter);
        filter.add("account_data", excludeAllTypesFilter);
        filter.add("room", roomFilter);
        SyncOptions opts = SyncOptions.build().setFilter(GsonUtil.get().toJson(filter)).get();

        _SyncData sync = client.sync(opts);
        _SyncData.JoinedRoom room = sync.getRooms().getJoined().stream().findFirst().orElseThrow(NotFoundException::new);
        String name = Stream.concat(room.getState().getEvents().stream(), room.getTimeline().getEvents().stream())
                .filter(ev -> "m.room.name".equals(ev.getType()))
                .map(ev -> new MatrixJsonRoomNameEvent(ev.getJson()).getName().orElse(null))
                .findFirst()
                .orElse(null);
        Map<String, MatrixJsonRoomMembershipEvent> joins = new HashMap<>();
        Stream.concat(room.getState().getEvents().stream(), room.getTimeline().getEvents().stream())
                .filter(ev -> "m.room.member".equals(ev.getType()))
                .map(ev -> new MatrixJsonRoomMembershipEvent(ev.getJson()))
                .filter(ev -> RoomMembership.Join.is(ev.getMembership()))
                .forEach(ev -> joins.put(ev.getInvitee().getId(), ev));
        List<Member> members = new ArrayList<>();
        joins.forEach((uId, ev) -> members.add(new Member(HexUtil.encode(uId), ev.getDisplayName().orElse(""))));
        return new Channel(id, name, members);
    }

    public String makeChannel(String orderId, List<Contact> contacts) {
        RoomCreationOptions.build().setGuestCanJoin(false).get();
        // Set name to Order ID + contact email or phone number

        throw new RuntimeException();
    }

    public String findChannel(String orderId, List<Contact> contacts) {
        throw new RuntimeException();
    }

    public MessageChunk getMessages(String channelId, String token, String direction) {
        String roomId = HexUtil.decode(channelId);

        if ("HEAD".equals(token)) { // We want a sync from the start
            JsonArray wildcard = new JsonArray();
            wildcard.add("*");

            JsonObject excludeAllTypesFilter = new JsonObject();
            excludeAllTypesFilter.add("not_types", wildcard);

            JsonArray messageEventType = new JsonArray();
            messageEventType.add("m.room.message");
            JsonObject roomEventFilter = GsonUtil.makeObj("types", messageEventType);

            JsonArray roomIds = new JsonArray();
            roomIds.add(roomId);

            JsonObject roomFilter = new JsonObject();
            roomFilter.add("rooms", roomIds);
            roomFilter.add("ephemeral", excludeAllTypesFilter);
            roomFilter.add("account_data", excludeAllTypesFilter);
            roomFilter.add("timeline", roomEventFilter);
            roomFilter.add("state", excludeAllTypesFilter);

            JsonObject filter = new JsonObject();
            filter.add("presence", excludeAllTypesFilter);
            filter.add("account_data", excludeAllTypesFilter);
            filter.add("room", roomFilter);
            SyncOptions opts = SyncOptions.build().setFilter(GsonUtil.get().toJson(filter)).get();

            _SyncData sync = client.sync(opts);
            _SyncData.JoinedRoom room = sync.getRooms().getJoined().stream()
                    .findFirst().orElseThrow(NotFoundException::new);
            List<Message> messages = room.getTimeline().getEvents().stream()
                    .map(ev -> new MatrixJsonRoomMessageEvent(ev.getJson()))
                    .map(ev -> build(ev, roomId))
                    .collect(Collectors.toList());

            return new MessageChunk(HexUtil.encode(room.getTimeline().getPreviousBatchToken()), null, messages);
        } else { // We want a sync from a previous batch
            token = HexUtil.decode(token);
            if ("previous".equals(direction)) {
                direction = "b";
            }
            if ("next".equals(direction)) {
                direction = "f";
            }

            MatrixRoomMessageChunkOptions opts = MatrixRoomMessageChunkOptions.build()
                    .setDirection(direction)
                    .setFromToken(token)
                    .get();
            _MatrixRoomMessageChunk chunk = client.getRoom(roomId).getMessages(opts);

            String startToken = HexUtil.encode(chunk.getStartToken());
            String endToken = HexUtil.encode(chunk.getEndToken());
            List<Message> messages = chunk.getEvents().stream()
                    .filter(ev -> "m.room.message".equals(ev.getType()))
                    .map(ev -> new MatrixJsonRoomMessageEvent(ev.getJson())).map(ev -> build(ev, roomId)).collect(Collectors.toList());

            if (StringUtils.equals(startToken, endToken)) {
                startToken = null;
                endToken = null;
            }

            return new MessageChunk(startToken, endToken, messages);
        }
    }

    public String putMessage(String channelId, JsonObject message) {
        String type = GsonUtil.findString(message, "@type").orElse("Text");
        if ("Text".equalsIgnoreCase(type)) {
            String content = GsonUtil.getStringOrThrow(message, "content");
            String evId = client.getRoom(HexUtil.decode(channelId)).sendFormattedText(content, content);
            return HexUtil.encode(evId);
        } else if ("Attachment".equalsIgnoreCase(type)) {
            String mediaId = GsonUtil.getStringOrThrow(message, "mediaId");
            Optional<String> filename = GsonUtil.findString(message, "filename");
            Optional<String> content = GsonUtil.findString(message, "content");

            JsonObject fileInfo = new JsonObject();
            fileInfo.addProperty("mimetype", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            fileInfo.addProperty("size", 0);

            JsonObject data = new JsonObject();
            data.addProperty("msgtype", "m.file");
            data.addProperty("url", HexUtil.decode(mediaId));
            data.add("info", fileInfo);
            data.addProperty("body", content.orElse(filename.orElse("")));
            filename.ifPresent(v -> data.addProperty("filename", v));

            String evId = client.getRoom(HexUtil.decode(channelId)).sendEvent("m.room.message", data);
            return HexUtil.encode(evId);
        } else {
            throw new InvalidArgumentException("Type value is unknown: " + type);
        }
    }

    public SyncChunk sync(String since) {
        List<String> joined = new ArrayList<>();
        List<String> left = new ArrayList<>();
        List<Message> messages = new ArrayList<>();
        SyncOptions opts = SyncOptions.build().setSince(HexUtil.decode(since)).get();
        _SyncData data = client.sync(opts);

        data.getRooms().getJoined().forEach(room -> {
            String channelId = HexUtil.encode(room.getId());
            joined.add(channelId);
            room.getTimeline().getEvents().stream()
                    .filter(ev -> "m.room.message".equals(ev.getType()))
                    .map(ev -> {
                        JsonObject json = ev.getJson();
                        json.addProperty(EventKey.RoomId.get(), room.getId());
                        return new MatrixJsonRoomMessageEvent(json);
                    })
                    .forEach(ev -> messages.add(build(ev, room.getId())));
        });

        data.getRooms().getLeft().forEach(room -> {
            String channelId = HexUtil.encode(room.getId());
            left.add(channelId);
            room.getTimeline().getEvents().stream()
                    .filter(ev -> "m.room.message".equals(ev.getType()))
                    .map(ev -> {
                        JsonObject json = ev.getJson();
                        json.addProperty(EventKey.RoomId.get(), room.getId());
                        return new MatrixJsonRoomMessageEvent(json);
                    })
                    .forEach(ev -> messages.add(build(ev, room.getId())));
        });

        return new SyncChunk(joined, left, messages, HexUtil.encode(data.nextBatchToken()));
    }

    public String upload(InputStream io, long length, String type) {
        String mxcUri = client.putMedia(io, length, type);
        return HexUtil.encode(mxcUri);
    }

    public _MatrixContent download(String id) {
        return client.getMedia(HexUtil.decode(id));
    }

}
