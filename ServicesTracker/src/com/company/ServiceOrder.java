package com.company;

/**
 * Created by aor on 2017-04-16.
 */
public class ServiceOrder {
    // Constants
    public static final String SERVICE_ORDER_DELIM = ":";

    // State
    private String serviceName;
    private Integer pollingFrequency;

    public ServiceOrder(String serviceName, Integer pollingFrequency) {
        this.serviceName = serviceName;
        this.pollingFrequency = pollingFrequency;
    }

    @Override
    public String toString() {
        return "ServiceOrder{" +
                "serviceName='" + serviceName + '\'' +
                ", pollingFrequency=" + pollingFrequency +
                '}';
    }

    public static ServiceOrder toServiceOrder(String str) {
        String[] tokens = str.split(SERVICE_ORDER_DELIM);
        return new ServiceOrder(tokens[0], Integer.parseInt(tokens[1]));
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getPollingFrequency() {
        return pollingFrequency;
    }
}
