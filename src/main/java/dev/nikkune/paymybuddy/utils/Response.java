package dev.nikkune.paymybuddy.utils;

import java.util.HashMap;

public class Response extends HashMap<String, Object> {

    public Response(String message, boolean success) {
        this.put("message", message);
        this.put("success", success);
    }

    public Response add(String key, Object value) {
        this.put(key, value);
        return this;
    }

    public Response error(Object value) {
        this.replace("success", false);
        this.put("error", value);
        return this;
    }
}
