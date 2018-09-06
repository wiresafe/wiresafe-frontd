package com.wiresafe.front.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Channel {

    private String id;
    private String name;
    private List<Member> members;

    public Channel(String id, String name, List<Member> members) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.members = Collections.unmodifiableList(Objects.requireNonNull(members));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Member> getMembers() {
        return members;
    }

}
