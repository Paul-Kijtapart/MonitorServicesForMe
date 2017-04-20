package com.company;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by aor on 2017-04-14.
 */
public class CallerInfo {

    // Constants
    public static final int INITIAL_DELAY = 5; // in seconds

    //States
    private Socket socket;
    private ObjectOutputStream outputStream;

    private ServiceMonitor serviceMonitor;
    private Map<String, Integer> callerServiceMap;

    private Map<String, ScheduledFuture> serviceNameToScheduleWorkerMap;
    private ScheduledExecutorService scheduler;


    public CallerInfo(Socket socket, ServiceMonitor serviceMonitor) {
        this.socket = socket;
        this.serviceMonitor = serviceMonitor;
        callerServiceMap = new HashMap<>();
        this.serviceNameToScheduleWorkerMap =
                new HashMap<>();
        this.scheduler =
                Executors.newScheduledThreadPool(
                        ServiceMonitor.serviceNameToServiceInfoMap.size()
                );
        setUpStreams();
    }

    private void setUpStreams() {
        try {
            outputStream =
                    new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getCallerServiceMap() {
        return callerServiceMap;
    }

    public void reload(List<ServiceOrder> serviceOrderList) {
        callerServiceMap.clear();
        update(serviceOrderList);
    }

    public void update(List<ServiceOrder> serviceOrderList) {
        for (ServiceOrder so : serviceOrderList) {
            String serviceName = so.getServiceName();
            Integer pollingFrequency = so.getPollingFrequency();
            callerServiceMap.put(serviceName, pollingFrequency);
            updatePollingFrequencySchedule(serviceName, pollingFrequency);
        }
    }

    public void delete(String[] serviceNames) {
        for (String serviceName : serviceNames) {
            callerServiceMap.remove(serviceName);
            cancelSchedule(serviceName);
        }
    }

    public void updatePollingFrequencySchedule(String serviceName, Integer pollingFrequency) {
        if (!isKnownService(serviceName)) {
            System.out.println("Unknown service name: " + serviceName);
            return;
        }
        cancelSchedule(serviceName);
        ScheduledFuture scheduledFuture =
                scheduler.scheduleAtFixedRate(new NotifyCaller(serviceName),
                        INITIAL_DELAY,
                        pollingFrequency,
                        TimeUnit.SECONDS
                );

        serviceNameToScheduleWorkerMap.put(serviceName, scheduledFuture);
    }

    public void cancelSchedule(String serviceName) {
        ScheduledFuture scheduledFuture =
                serviceNameToScheduleWorkerMap.get(serviceName);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    public static boolean isKnownService(String serviceName) {
        if (ServiceMonitor.serviceNameToServiceInfoMap.containsKey(serviceName)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "{ " + socket.getInetAddress() + ", \n" + callerServiceMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallerInfo that = (CallerInfo) o;

        return socket.equals(that.socket);
    }

    @Override
    public int hashCode() {
        return socket.hashCode();
    }


    private class NotifyCaller implements Runnable {
        // States
        private String serviceName;

        public NotifyCaller(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public void run() {
            ServiceStatus serviceStatus =
                    serviceMonitor.getServiceNameToServiceStatusMap().get(serviceName);

            ResponseMessage responseMessage =
                    new ResponseMessage(serviceName, serviceStatus.isAlive());

            try {
                outputStream.writeObject(responseMessage);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // close inner NotifyCaller class
} // close CallerInfo class
