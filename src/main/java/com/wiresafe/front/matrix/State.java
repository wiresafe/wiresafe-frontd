package com.wiresafe.front.matrix;

class State {

    private SynapseConfig cfg;
    private SqlConnectionPool db;

    public State(SynapseConfig cfg, SqlConnectionPool db) {
        this.cfg = cfg;
        this.db = db;
    }

    public SynapseConfig getConfig() {
        return cfg;
    }

    public SqlConnectionPool getDb() {
        return db;
    }

}
