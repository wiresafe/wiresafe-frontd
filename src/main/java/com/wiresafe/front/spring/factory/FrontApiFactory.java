package com.wiresafe.front.spring.factory;

import com.wiresafe.front.matrix.SynapseConfig;
import com.wiresafe.front.model.Config;
import com.wiresafe.front.model.FrontApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@Configuration
public class FrontApiFactory {

    private FrontApi obj;

    public FrontApiFactory() throws MalformedURLException {
        URL csEndpoint = new URL(Objects.requireNonNull(System.getenv("FRONTD_MATRIX_CS_ENDPOINT"), "A Home server endpoint must be provided"));
        URL isEndpoint = new URL(Objects.requireNonNull(System.getenv("FRONTD_MATRIX_IS_ENDPOINT"), "An Identity server endpoint must be provided"));
        String sharedSecret = Objects.requireNonNull(System.getenv("FRONTD_MATRIX_SYNAPSE_SHARED_SECRET"));

        SynapseConfig sCfg = new SynapseConfig();
        sCfg.getApi().setSharedSecret(sharedSecret);

        Config cfg = new Config();
        cfg.setCsEndpoint(csEndpoint);
        cfg.setIsEndpoint(isEndpoint);
        cfg.setSynapseCfg(sCfg);

        this.obj = new FrontApi(cfg);
    }

    @Bean
    public FrontApi get() {
        return obj;
    }

}
