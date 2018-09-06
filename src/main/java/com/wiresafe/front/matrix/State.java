package com.wiresafe.front.matrix;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

class State {

    private SynapseConfig cfg;
    private SqlConnectionPool db;
    private CloseableHttpClient client;

    public State(SynapseConfig cfg, SqlConnectionPool db) {
        this.cfg = cfg;
        this.db = db;
        this.client = HttpClients.createSystem();
    }

    public SynapseConfig getConfig() {
        return cfg;
    }

    public SqlConnectionPool getDb() {
        return db;
    }

    public CloseableHttpClient getClient() {
        return client;
    }

}
