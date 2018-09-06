package com.wiresafe.front.model;

import com.wiresafe.front.matrix.SynapseConfig;

import java.net.URL;

public class Config {

    private URL csEndpoint;
    private URL isEndpoint;
    private SynapseConfig synapseCfg;

    public URL getCsEndpoint() {
        return csEndpoint;
    }

    public void setCsEndpoint(URL csEndpoint) {
        this.csEndpoint = csEndpoint;
    }

    public URL getIsEndpoint() {
        return isEndpoint;
    }

    public void setIsEndpoint(URL isEndpoint) {
        this.isEndpoint = isEndpoint;
    }

    public SynapseConfig getSynapseCfg() {
        return synapseCfg;
    }

    public void setSynapseCfg(SynapseConfig synapseCfg) {
        this.synapseCfg = synapseCfg;
    }

}
