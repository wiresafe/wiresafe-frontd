package com.wiresafe.front.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wiresafe.front.matrix.Synapse;
import io.kamax.matrix.client.MatrixClientContext;
import io.kamax.matrix.client.MatrixPasswordCredentials;
import io.kamax.matrix.client.regular.MatrixHttpClient;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.concurrent.TimeUnit;

public class FrontApi {

    private Config cfg;
    private Synapse synapse;
    private LoadingCache<String, Session> clients;

    private HmacUtils hmac;

    public FrontApi(Config cfg) {
        this.cfg = cfg;
        this.synapse = new Synapse(cfg.getSynapseCfg());

        this.clients = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Session>() {
                    @Override
                    public Session load(String key) {
                        return new Session(new MatrixHttpClient(getContext().setToken(key)));
                    }
                });

        this.hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_512, cfg.getSynapseCfg().getApi().getSharedSecret());
    }

    private MatrixClientContext getContext() {
        return new MatrixClientContext().setHsBaseUrl(cfg.getCsEndpoint()).setIsBaseUrl(cfg.getIsEndpoint());
    }

    public String login(String userId) {
        MatrixHttpClient client = new MatrixHttpClient(getContext());
        String token = synapse.findAccessToken(userId).orElseGet(() -> {
            String password = hmac.hmacHex(userId + cfg.getSynapseCfg().getApi().getSharedSecret());
            client.register(new MatrixPasswordCredentials(userId, password), synapse.getConfig().getApi().getSharedSecret(), false);
            return ""; // FIXME we need the actual token
        });

        Session session = new Session(client);
        clients.put(token, session);
        return token;
    }

    public Session with(String accessToken) {
        return clients.getUnchecked(accessToken);
    }

}
