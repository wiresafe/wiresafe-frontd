package com.wiresafe.front.model;

import com.wiresafe.front.HexUtil;
import io.kamax.matrix.MatrixID;
import io.kamax.matrix.client.regular.MatrixHttpClient;

public class User {

    private MatrixHttpClient client;
    private String id;

    public User(MatrixHttpClient client, String id) {
        this.client = client;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return client.getUser(MatrixID.asAcceptable(HexUtil.decode(id))).getName().orElse(null);
    }

}
