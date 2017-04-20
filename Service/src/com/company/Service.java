package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by aor on 2017-04-14.
 */
public class Service implements Runnable{

    // States
    private boolean listening;
    private String name;
    private ServerSocket serverSocket;
    private Integer portNumber;


    public Service(String name, Integer portNumber) {
        this.name = name;
        this.portNumber = portNumber;
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        setUpNetworking();
        listening = true;
        try {
            while (listening) {
                Socket currentCaller = serverSocket.accept();
                // TODO: Ensures that for all connections, once connected, disconnect right after.
                System.out.println("Service name " + name +
                        " just receives connection from " +
                        currentCaller);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // close start

    private void setUpNetworking() {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // close setUpNetworking

} // close Service class
