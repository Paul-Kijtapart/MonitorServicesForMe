package com.company;

import java.io.Serializable;

/**
 * Created by aor on 2017-04-16.
 */
public class ActionMessage implements Serializable {
    private String action;
    private String payload;

    public ActionMessage(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "ActionMessage{" +
                "action='" + action + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
