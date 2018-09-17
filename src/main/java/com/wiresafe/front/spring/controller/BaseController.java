package com.wiresafe.front.spring.controller;

import com.wiresafe.front.exception.AuthenticationRequiredException;
import io.kamax.matrix.json.GsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseController {

    private Logger log = LoggerFactory.getLogger(BaseController.class);

    protected String getAccessToken(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (StringUtils.isBlank(apiKey)) {
            throw new AuthenticationRequiredException("API Key missing");
        }

        return apiKey;
    }

    protected String getBody(HttpServletRequest req) {
        try {
            return IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Unable to get body from request {} {}", req.getMethod(), req.getRequestURL(), e);
            throw new RuntimeException("Unable to get body from request", e);
        }
    }

    protected String toJson(Object obj) {
        return GsonUtil.get().toJson(obj);
    }

}
