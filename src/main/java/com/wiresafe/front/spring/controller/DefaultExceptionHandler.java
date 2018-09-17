package com.wiresafe.front.spring.controller;

import com.google.gson.JsonObject;
import com.wiresafe.front.exception.AuthenticationRequiredException;
import com.wiresafe.front.exception.ForbidenException;
import com.wiresafe.front.exception.InvalidArgumentException;
import io.kamax.matrix.json.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
@CrossOrigin
@ResponseBody
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultExceptionHandler extends BaseController {

    private Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    public String handle(String msg) {
        if (StringUtils.isBlank(msg)) {
            return toJson(new JsonObject());
        }

        return toJson(GsonUtil.makeObj("error", msg));
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

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle(Throwable ex) {
        log.error("Error when processing request", ex);
        return handle("Unknown error occurred. Contact your service provider.");
    }

}
