package com.company;

/**
 * Created by aor on 2017-04-19.
 */
public class ServiceStatusChangeInfo extends ServiceStatus {

    // Constants
    public static final String ADD_ACTION = "add";
    public static final String DELETE_ACTION = "delete";

    // States
    private String action;

    public ServiceStatusChangeInfo(String name, Integer pollingFrequency, String action) {
        super(name, pollingFrequency);
        this.action = action;
    }

    public ServiceStatusChangeInfo(ServiceStatus serviceStatus, String action) {
        this(serviceStatus.getName(), serviceStatus.getPollingFrequency(), action);
    }


    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return super.toString()
                + " action " + this.action;
    }

}
