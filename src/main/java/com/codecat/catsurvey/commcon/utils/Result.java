package com.codecat.catsurvey.commcon.utils;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private int status;

    private String message;

    private Object data;


    public String toString() {
        return JSON.toJSONString(this);
    }

    public static Result success(String message, Object data) { return new Result(200, message, data); }

    public static Result successData(Object data) {
        return new Result(200, "success", data);
    }

    public static Result successMsg(String message) {
        return new Result(200, message, null);
    }

    public static Result failedMsg(String message) {
        return new Result(500, message, null);
    }

    public static Result validatedFailed(String message) { return new Result(400, message, null); }

    public static Result unauthorized(String message) { return new Result(401, message, null); }

    public static Result success() {
        return new Result(200, "success", null);
    }

    public static Result failed() {
        return new Result(500, "failed", null);
    }

    public static Result validatedFailed() { return new Result(400, "validatedFailed", null); }

    public static Result unauthorized() { return new Result(401, "unauthorized", null); }
}
