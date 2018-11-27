package com.wiresafe.front.spring.controller;

import com.google.gson.JsonObject;
import com.wiresafe.front.exception.AuthenticationRequiredException;
import com.wiresafe.front.exception.ForbidenException;
import com.wiresafe.front.exception.InvalidArgumentException;
import io.kamax.matrix.client.MatrixClientRequestRateLimitedException;
import io.kamax.matrix.json.GsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseController {

    private Logger log = LoggerFactory.getLogger(BaseController.class);

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

    public String handle(String msg) {
        if (StringUtils.isBlank(msg)) {
            return handle(new JsonObject());
        }

        return handle(GsonUtil.makeObj("error", msg));
    }

    public String handle(JsonObject errorInfo) {
        return toJson(errorInfo);
    }

    @ExceptionHandler(InvalidArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handle(InvalidArgumentException ex) {
        return handle(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handle(AuthenticationRequiredException ex) {
        return handle(ex.getMessage());
    }

    @ExceptionHandler(ForbidenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handle(ForbidenException ex) {
        return handle(ex.getMessage());
    }

    @ExceptionHandler(MatrixClientRequestRateLimitedException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public String handle(MatrixClientRequestRateLimitedException ex) {
        return handle(ex.getError().get().getError());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public String handle(MissingRequestHeaderException e, HttpServletResponse res) {
        if (e.getHeaderName().equalsIgnoreCase("x-api-key")) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            return handle("API Key missing");
        } else {
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            return handle(e.getMessage());
        }
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle(Throwable ex) {
        log.error("Error when processing request", ex);
        return handle("Unknown error occurred. Contact your service provider.");
    }

}
