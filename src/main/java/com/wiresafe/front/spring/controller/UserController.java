package com.wiresafe.front.spring.controller;

import com.google.gson.JsonObject;
import com.wiresafe.front.model.FrontApi;
import com.wiresafe.front.model.User;
import com.wiresafe.front.spring.factory.FrontApiFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController extends BaseController {

    private FrontApi model;

    @Autowired
    public UserController(FrontApiFactory factory) {
        this.model = factory.get();
    }

    @GetMapping("/{userId}")
    public String getUser(HttpServletRequest request, @PathVariable String userId) {
        User user = model.with(getAccessToken(request)).getUser(userId);

        JsonObject res = new JsonObject();
        res.addProperty("@id", "/user/" + user.getId());
        res.addProperty("name", user.getName());

        return toJson(res);
    }

}
