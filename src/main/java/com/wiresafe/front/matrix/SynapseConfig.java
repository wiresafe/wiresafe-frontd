package com.wiresafe.front.matrix;

import java.net.URL;

public class SynapseConfig {

    public static class API {

        private URL client;
        private String sharedSecret;

        public URL getClient() {
            return client;
        }

        public void setClient(URL client) {
            this.client = client;
        }

        public String getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(String sharedSecret) {
            this.sharedSecret = sharedSecret;
        }

    }

    public static class Database {

        private String connection;

        public String getConnection() {
            return connection;
        }

        public void setConnection(String connection) {
            this.connection = connection;
        }

    }

    private API api = new API();
    private Database db = new Database();

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }

}
