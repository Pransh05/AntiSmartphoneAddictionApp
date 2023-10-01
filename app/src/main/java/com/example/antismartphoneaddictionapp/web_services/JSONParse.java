package com.example.antismartphoneaddictionapp.web_services;

import org.json.JSONObject;

public class JSONParse {

    public String parse(JSONObject json) {
        try {
            return json.getString("Value");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
