package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by aor on 2017-04-14.
 */
public class ServiceMonitor extends Observable {

    // States
    private Integer portNumber;
    private ServerSocket serverSocket;
    private boolean listening;

    private Map<String, ServiceStatus> serviceNameToServiceStatusMap;
    private Map<String, List<CallerInfo>> serviceNameToCallerListMap;
    public static Map<String, ServiceInfo> serviceNameToServiceInfoMap;


    static {
        serviceNameToServiceInfoMap =
                new HashMap<>();

        ServiceInfo sa =
                new ServiceInfo("a",
                        "localhost",
                        3001);
        ServiceInfo sb =
                new ServiceInfo("b",
                        "localhost",
                        3003);
        ServiceInfo sc =
                new ServiceInfo("c",
                        "localhost",
                        3005);
        ServiceInfo sd =
                new ServiceInfo("d",
                        "localhost",
                        3006);

        serviceNameToServiceInfoMap
                .put(sa.getName(), sa);
        serviceNameToServiceInfoMap
                .put(sb.getName(), sb);
        serviceNameToServiceInfoMap
                .put(sc.getName(), sc);
        serviceNameToServiceInfoMap
                .put(sd.getName(), sd);
    }

    public ServiceMonitor(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public void start() {
        serviceNameToCallerListMap =
                new HashMap<>();
        serviceNameToServiceStatusMap =
                new HashMap<>();

        setUpNetworking();

        Thread trackServiceWorker = new Thread(new TrackServices(this));
        trackServiceWorker.setName("trackServices");
        trackServiceWorker.start();
        // TODO: trackServiceWorker need to know when there is a change on serviceNameToServiceStatusMap

        listening = true;
        while (listening) {
            try {
                Socket currentCaller = serverSocket.accept();
                Thread callerHandleWorker = new Thread(new HandleCaller(this, currentCaller));
                callerHandleWorker.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onServiceStatusMapChange(ServiceStatusChangeInfo serviceStatusChangeInfo) {
        setChanged();
        notifyObservers(serviceStatusChangeInfo);
        // TODO: add flag requires serviceName and new pollingFrequency
        // TODO: delete flag requires serviceName
    }


    public Map<String, ServiceStatus> getServiceNameToServiceStatusMap() {
        return serviceNameToServiceStatusMap;
    }

    public Map<String, List<CallerInfo>> getServiceNameToCallerListMap() {
        return serviceNameToCallerListMap;
    }

    private void setUpNetworking() {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ServiceMonitor start listening at portNumber " + this.portNumber);
    }

    /* Removes the caller from all its registered services */
    public synchronized void removeCallerFromRegisteredServices(CallerInfo callerInfo) {
        Set<String> serviceNameSet = callerInfo.getCallerServiceMap().keySet();
        for (String sn : serviceNameSet) {
            removeCallerWithServiceName(sn, callerInfo);
        }
    }

    /* Remove caller with the service name and update both states properly.*/
    public synchronized void removeCallerWithServiceName(String serviceName, CallerInfo callerInfo) {
        List<CallerInfo> currentCallerList =
                serviceNameToCallerListMap.get(serviceName);

        if (currentCallerList == null) {
            System.out.println("ServiceName doesn't exist.");
            return;
        }

        currentCallerList.remove(callerInfo);
        if (currentCallerList.isEmpty()) {
            serviceNameToCallerListMap.remove(serviceName);
        }

        updateServiceStatusOnDelete(
                serviceName,
                callerInfo.getCallerServiceMap().get(serviceName)
        );
    }

    /* Update the state's ServiceStatus if the removedFrequency equals to
    the current value of the state's ServiceStatus
    */
    private synchronized void updateServiceStatusOnDelete(String serviceName, Integer removedFrequency) {
        ServiceStatus current = serviceNameToServiceStatusMap.get(serviceName);
        if (current != null && current.getPollingFrequency() == removedFrequency) {
            updateServiceStatusToNextMinFrequency(serviceName);
        }
    }

    /*
    Pre-condition: callerInfo is already updated with the action and its payload
    Update pollingFrequency in serviceNameToServiceStatusMap
    Update serviceNameToCallerListMap
     */
    public synchronized void applyDelete(String[] serviceNames, CallerInfo callerInfo) {
        for (String serviceName : serviceNames) {
            removeCallerWithServiceName(serviceName, callerInfo);
        }
    }

    /*
    Pre-condition: callerInfo is already updated with the action and its payload
    Update pollingFrequency in serviceNameToServiceStatusMap
    Update serviceNameToCallerListMap
    */
    public synchronized void applyUpdate(List<ServiceOrder> serviceOrderList, CallerInfo callerInfo) {
        for (ServiceOrder so : serviceOrderList) {
            addCallerIfNotExist(so, callerInfo);
        }
    }

    /*
    Pre-condition: callerInfo is holding values before the update
    Update pollingFrequency in serviceNameToServiceStatusMap
    Update serviceNameToCallerListMap
     */
    public synchronized void applyAdd(List<ServiceOrder> serviceOrderList, CallerInfo callerInfo) {
        for (ServiceOrder so : serviceOrderList) {
            addCallerIfNotExist(so, callerInfo);
        }
    }

    /*
     Pre-condition: callerInfo is already updated.
     */
    public synchronized void addCallerIfNotExist(ServiceOrder so, CallerInfo callerInfo) {
        List<CallerInfo> currentList =
                serviceNameToCallerListMap.get(so.getServiceName());

        if (currentList == null) {
            currentList = new ArrayList<>();
            currentList.add(callerInfo);
            serviceNameToCallerListMap.put(so.getServiceName(), currentList);
        } else if (!currentList.contains(callerInfo)) {
            currentList.add(callerInfo);
        }
        updateServiceStatusWithName(so);
    }

    /*
    Update the pollingFrequency of the corresponding serviceName
    If and only if the new pollingFrequency is lower
    */
    public synchronized void updateServiceStatusWithName(ServiceOrder so) {
        ServiceStatus current = serviceNameToServiceStatusMap.get(so.getServiceName());
        if (current == null) {
            ServiceStatus sf = new ServiceStatus(so.getServiceName(), so.getPollingFrequency());
            serviceNameToServiceStatusMap.put(so.getServiceName(), sf);
            onServiceStatusMapChange(new ServiceStatusChangeInfo(
                    sf,
                    ServiceStatusChangeInfo.ADD_ACTION)
            );
            return;
        }

        if (current.getPollingFrequency() > so.getPollingFrequency()) {
            current.setPollingFrequency(so.getPollingFrequency());
            onServiceStatusMapChange(new ServiceStatusChangeInfo(
                    current,
                    ServiceStatusChangeInfo.ADD_ACTION)
            );
        } else {
            updateServiceStatusToNextMinFrequency(so.getServiceName());
        }
    }

    /* Update the state's ServiceStatus with the next minimum frequency*/
    private synchronized void updateServiceStatusToNextMinFrequency(String serviceName) {
        ServiceStatus current = serviceNameToServiceStatusMap.get(serviceName);
        Integer minFrequency = getMinPollingFrequency(serviceName);
        if (minFrequency == -1) {
            serviceNameToServiceStatusMap.remove(serviceName);
            onServiceStatusMapChange(new ServiceStatusChangeInfo(
                    current,
                    ServiceStatusChangeInfo.DELETE_ACTION)
            );
            return;
        }
        if (current.getPollingFrequency() != minFrequency) {
            onServiceStatusMapChange(new ServiceStatusChangeInfo(
                    current,
                    ServiceStatusChangeInfo.ADD_ACTION)
            );
        }
        current.setPollingFrequency(minFrequency);
    }


    /* Return next minimum frequency among callers who currently register for the given serviceName.*/
    private synchronized Integer getMinPollingFrequency(String serviceName) {
        List<CallerInfo> callerInfoList = serviceNameToCallerListMap.get(serviceName);

        if (callerInfoList == null || callerInfoList.isEmpty()) {
            return -1;
        }

        if (callerInfoList.size() == 1) {
            return callerInfoList.get(0).getCallerServiceMap().get(serviceName);
        }

        Integer res = callerInfoList.get(0).getCallerServiceMap().get(serviceName);
        for (int i = 1; i < callerInfoList.size(); ++i) {
            Integer currentFrequency =
                    callerInfoList.get(i).getCallerServiceMap().get(serviceName);
            res = Math.min(res, currentFrequency);
        }
        return res;
    }


    public List<ServiceOrder> toServiceOrderList(String payload) {
        String[] tokens = payload.split(";");
        List<ServiceOrder> res =
                new ArrayList<>(tokens.length);
        for (String serviceStr : tokens) {
            res.add(ServiceOrder.toServiceOrder(serviceStr));
        }
        return res;
    }


    public synchronized void onFailConnectService(String serviceName) {
        ServiceStatus serviceStatus =
                serviceNameToServiceStatusMap.get(serviceName);

        if (serviceName == null) {
            System.out.println(serviceName + " is No Longer exist.");
            return;
        }

        serviceStatus.setAlive(false);
    }

    public synchronized void onSuccessConnectService(String serviceName) {
        ServiceStatus serviceStatus =
                serviceNameToServiceStatusMap.get(serviceName);

        if (serviceName == null) {
            System.out.println(serviceName + " is No Longer exist.");
            return;
        }

        serviceStatus.setAlive(true);
    }

} // close ServiceMonitor class
