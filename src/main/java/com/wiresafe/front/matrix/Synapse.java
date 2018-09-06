package com.wiresafe.front.matrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class Synapse {

    private final Logger log = LoggerFactory.getLogger(Synapse.class);

    private long id;
    private String name;
    private String domain;
    private State state;

    public Synapse(SynapseConfig cfg) {
        this.state = new State(cfg, new SqlConnectionPool(cfg.getDb()));
    }

    public SynapseConfig getConfig() {
        return state.getConfig();
    }

    public Optional<String> findAccessToken(String localpart) {
        return state.getDb().withConnFunction(conn -> {
            PreparedStatement stmt = conn.prepareStatement(Queries.findAccessToken);
            stmt.setString(1, localpart);
            ResultSet rSet = stmt.executeQuery();
            if (!rSet.next()) {
                return Optional.empty();
            }

            return Optional.ofNullable(rSet.getString(1));
        });
    }

}
