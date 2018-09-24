package com.wiresafe.front.spring.controller;

import com.wiresafe.front.exception.InvalidArgumentException;
import com.wiresafe.front.model.FrontApi;
import io.kamax.matrix._MatrixContent;
import io.kamax.matrix.json.GsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@CrossOrigin
@RequestMapping("/media")
public class MediaController extends BaseController {

    private FrontApi model;

    @Autowired
    public MediaController(FrontApi model) {
        this.model = model;
    }

    @RequestMapping("/download/{mediaId}")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable String mediaId) throws IOException {
        _MatrixContent content = model.with(getAccessToken(request)).download(mediaId);
        byte[] data = content.getData();
        response.setContentType(content.getType().orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        response.setContentLength(data.length);
        IOUtils.write(data, response.getOutputStream());
    }

    @RequestMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String upload(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            throw new InvalidArgumentException("Content-Type header is missing");
        }

        try {
            int length = request.getContentLength();
            ByteArrayInputStream input = new ByteArrayInputStream(IOUtils.readFully(request.getInputStream(), length));
            String mediaId = model.with(getAccessToken(request)).upload(input, length, contentType);
            return toJson(GsonUtil.makeObj("id", mediaId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
