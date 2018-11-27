package com.wiresafe.front.spring.controller;

import com.wiresafe.front.exception.InvalidArgumentException;
import com.wiresafe.front.model.FrontApi;
import io.kamax.matrix._MatrixContent;
import io.kamax.matrix.json.GsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Controller
@ResponseBody
@CrossOrigin
@RequestMapping("/media")
@Async
public class MediaController extends BaseController {

    private FrontApi model;

    @Autowired
    public MediaController(FrontApi model) {
        this.model = model;
    }

    @RequestMapping("/download/{mediaId}")
    public CompletableFuture<byte[]> download(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String mediaId,
            HttpServletResponse response
    ) {
        _MatrixContent content = model.with(apiKey).download(mediaId);
        byte[] data = content.getData();
        response.setContentType(content.getType().orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        response.setContentLength(data.length);
        return CompletableFuture.completedFuture(data);
    }

    @RequestMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> upload(
            @RequestHeader("X-API-Key") String apiKey,
            HttpServletRequest request
    ) {
        String contentType = request.getHeader("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            throw new InvalidArgumentException("Content-Type header is missing");
        }

        try {
            byte[] data = IOUtils.readFully(request.getInputStream(), request.getContentLength());
            String mediaId = model.with(apiKey).upload(data, contentType);
            return CompletableFuture.completedFuture(toJson(GsonUtil.makeObj("id", mediaId)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
