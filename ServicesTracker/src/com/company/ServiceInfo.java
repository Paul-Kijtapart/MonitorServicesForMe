package com.company;

/**
 * Created by aor on 2017-04-17.
 */
public class ServiceInfo {
    private String name;
    private String hostName;
    private Integer portNumber;


    public ServiceInfo(String name, String hostName, Integer portNumber) {
        this.name = name;
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public String getName() {
        return name;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "hostName='" + hostName + '\'' +
                ", portNumber='" + portNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceInfo that = (ServiceInfo) o;

        if (!name.equals(that.name)) return false;
        if (!hostName.equals(that.hostName)) return false;
        return portNumber.equals(that.portNumber);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + hostName.hashCode();
        result = 31 * result + portNumber.hashCode();
        return result;
    }
}
