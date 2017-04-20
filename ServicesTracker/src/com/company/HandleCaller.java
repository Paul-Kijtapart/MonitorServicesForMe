package com.company;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by aor on 2017-04-17.
 */
class HandleCaller implements Runnable {


    // States
    private ServiceMonitor serviceMonitor;
    private Socket socket;
    private CallerInfo callerInfo;

    public HandleCaller(ServiceMonitor serviceMonitor,
                        Socket currentCaller) {
        this.serviceMonitor = serviceMonitor;
        this.socket = currentCaller;
        this.callerInfo = new CallerInfo(socket, serviceMonitor);
    }

    @Override
    public void run() {
        try (
                ObjectInputStream in =
                        new ObjectInputStream(socket.getInputStream());
                BufferedWriter out =
                        new BufferedWriter(new PrintWriter(socket.getOutputStream()));
        ) {
            Object obj = null;

            while ((obj = in.readObject()) != null) {
                ActionMessage currentMessage = (ActionMessage) obj;
                System.out.println(currentMessage);
                applyActionMessage(currentMessage);
//                report(currentMessage, "After");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {

        }
    }

    private void report(ActionMessage actionMessage, String timeFlag) {
        System.out.println(timeFlag + " action " + actionMessage.getAction());
        System.out.println("========");
        System.out.println("CallerInfo: ");
        System.out.println(callerInfo);
        System.out.println("========");
        System.out.println("serviceNameToServiceStatusMap: ");
        System.out.println(serviceMonitor.getServiceNameToServiceStatusMap());
        System.out.println("========");
        System.out.println("serviceNameToCallerListMap");
        System.out.println(serviceMonitor.getServiceNameToCallerListMap());
        System.out.println("========");
        System.out.println("There are " + serviceMonitor.getServiceNameToCallerListMap().size() + " clients.");
        System.out.println("================================");
    }

    private void applyActionMessage(ActionMessage currentMessage) {
        String action = currentMessage.getAction();
        action = action.replaceAll("[\\s]", "");
        action = action.toLowerCase();

        String payload = currentMessage.getPayload();

        if (action.equals("add")) {
            List<ServiceOrder> serviceOrderList =
                    serviceMonitor.toServiceOrderList(payload);

            serviceMonitor.removeCallerFromRegisteredServices(callerInfo);
            callerInfo.reload(serviceOrderList);
            serviceMonitor.applyAdd(serviceOrderList, callerInfo);

        } else if (action.equals("update")) {
            List<ServiceOrder> serviceOrderList =
                    serviceMonitor.toServiceOrderList(payload);

            callerInfo.update(serviceOrderList);
            serviceMonitor.applyUpdate(serviceOrderList, callerInfo);
        } else if (action.equals("delete")) {
            String[] serviceNames = payload.split(";");

            serviceMonitor.applyDelete(serviceNames, callerInfo);
            callerInfo.delete(serviceNames);

        } else {
            System.err.println("Unsupported action: " + action);
        }

    }

} // close HandleCaller class
