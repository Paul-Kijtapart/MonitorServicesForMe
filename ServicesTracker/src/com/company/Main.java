package com.company;

public class Main {

    public static void main(String[] args) {
        ServiceMonitor serviceMonitor = new ServiceMonitor(3000);
        serviceMonitor.start();
    }
}
