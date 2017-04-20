package com.company;

import java.io.Serializable;

/**
 * Created by aor on 2017-04-20.
 */
public class ResponseMessage implements Serializable {
    private String serviceName;
    private boolean alive;

    public ResponseMessage(String serviceName, boolean alive) {
        this.serviceName = serviceName;
        this.alive = alive;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "serviceName='" + serviceName + '\'' +
                ", alive=" + alive +
                '}';
    }
}
