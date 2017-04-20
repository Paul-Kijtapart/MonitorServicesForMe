package com.company;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by aor on 2017-04-17.
 */
class TrackServices implements Runnable, Observer {

    // Constants
    public static final int INITIAL_DELAY = 5; // in seconds

    // States
    private ScheduledExecutorService scheduler;
    private ServiceMonitor serviceMonitor;

    private Map<String, ScheduledFuture> serviceNameToScheduleWorkerMap;


    public TrackServices(ServiceMonitor serviceMonitor) {
        this.serviceMonitor = serviceMonitor;
        this.serviceNameToScheduleWorkerMap = new HashMap<>();
        this.serviceMonitor.addObserver(this);
        this.scheduler =
                Executors.newScheduledThreadPool(
                        ServiceMonitor.serviceNameToServiceInfoMap.size()
                );
    }


    @Override
    public void run() {
        System.out.println("Running Track services.");

    }

    void updatePollingFrequencySchedule(String serviceName, Integer pollingFrequency) {
        if (!isKnownService(serviceName)) {
            System.out.println("Unknown service name: " + serviceName);
            return;
        }
        cancelSchedule(serviceName);
        ScheduledFuture scheduledFuture =
                scheduler.scheduleAtFixedRate(new PollingServiceJob(serviceName),
                        INITIAL_DELAY,
                        pollingFrequency,
                        TimeUnit.SECONDS
                );
        serviceNameToScheduleWorkerMap.put(serviceName, scheduledFuture);
    }

    private boolean isKnownService(String serviceName) {
        if (ServiceMonitor.serviceNameToServiceInfoMap.containsKey(serviceName)) {
            return true;
        }
        return false;
    }

    void cancelSchedule(String serviceName) {
        ScheduledFuture scheduledFuture =
                serviceNameToScheduleWorkerMap.get(serviceName);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        ServiceStatusChangeInfo serviceStatusChangeInfo = (ServiceStatusChangeInfo) arg;
        System.out.println("Detected change on ServiceStatus on serviceName " + serviceStatusChangeInfo);

        String action = serviceStatusChangeInfo.getAction();
        if (action == "delete") {
            cancelSchedule(serviceStatusChangeInfo.getName());
        } else if (action == "add") {
            updatePollingFrequencySchedule(
                    serviceStatusChangeInfo.getName(),
                    serviceStatusChangeInfo.getPollingFrequency()
            );
        } else {
            System.err.println("Unrecognized action: " + action);
        }
    }

    private class PollingServiceJob implements Runnable {

        private String serviceName;

        public PollingServiceJob(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public void run() {
            checkIfServiceAlive();
//            report();
        }

        private void report() {
            System.out.println("PollingServie report for " + serviceName);
            System.out.println("========");
            System.out.println(serviceMonitor.getServiceNameToServiceStatusMap());
            System.out.println("================================");
        }

        private void checkIfServiceAlive() {
            ServiceInfo serviceInfo =
                    serviceMonitor.serviceNameToServiceInfoMap.get(serviceName);

            try (Socket socket = new Socket(serviceInfo.getHostName(), serviceInfo.getPortNumber())) {
                // Fix connection exception
                serviceMonitor.onSuccessConnectService(serviceName);

            } catch (UnknownHostException e) {
                // TODO: probable b/c service changes its location
                serviceMonitor.onFailConnectService(serviceName);

                e.printStackTrace();
            } catch (IOException e) {
                // Connection refused falls here
                serviceMonitor.onFailConnectService(serviceName);
            }
        }
    } // close inner PollingServiceJob class
} // close TrackServices class
