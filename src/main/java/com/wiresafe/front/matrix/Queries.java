package com.wiresafe.front.matrix;

public class Queries {

    public static final String findAccessToken = "select token from access_tokens where user_id ~ concat('@',?,':','.+') order by id limit 1";

}
