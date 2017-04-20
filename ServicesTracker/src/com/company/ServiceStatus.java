package com.company;

/**
 * Created by aor on 2017-04-14.
 */
public class ServiceStatus {
    private String name;
    private Integer pollingFrequency;
    private boolean alive;

    public ServiceStatus(String name, Integer pollingFrequency) {
        this.name = name;
        this.pollingFrequency = pollingFrequency;
        this.alive = false;
    }

    public String getName() {
        return name;
    }

    public Integer getPollingFrequency() {
        return pollingFrequency;
    }

    public void setPollingFrequency(Integer pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        return "{ name :" + this.name + ", \n" +
                " alive :" + this.alive + ", \n" +
                " pollingFrequency :" + this.pollingFrequency + " } \n";
    }
}
