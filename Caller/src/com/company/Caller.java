package com.company;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by aor on 2017-04-14.
 */
public class Caller {

    // States
    private String serverHostName;
    private Integer serverPortNumber;
    private Socket socket;
    private ObjectOutputStream smsOut;


    public Caller(String serverHostName, Integer serverPortNumber) {
        this.serverHostName = serverHostName;
        this.serverPortNumber = serverPortNumber;
    }

    public void start() {
        setUpNetworking();

        Thread userThread = new Thread(new HandleUser());
        userThread.setName("userWorker");
        userThread.start();

        Thread smsThread = new Thread(new HandleServiceMonitor());
        smsThread.setName("smsWorker");
        smsThread.start();
    }

    private void setUpNetworking() {
        try {
            socket = new Socket(serverHostName, serverPortNumber);
            smsOut = new ObjectOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ActionMessage toActionMessage(String line) throws MessageTypeException {
        String[] tokens = line.split("\\s");
        if (tokens.length < 2 || tokens.length > 2) {
            throw new MessageTypeException();
        }
        String command = tokens[0];
        String data = tokens[1];
        return new ActionMessage(command, data);
    }

    private class HandleServiceMonitor implements Runnable {
        @Override
        public void run() {
            try (ObjectInputStream smsIn =
                         new ObjectInputStream(socket.getInputStream())) {
                Object o = null;
                while ((o = smsIn.readObject()) != null) {
                    ResponseMessage responseMessage =
                            (ResponseMessage) o;
                    System.out.println(responseMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    } // close inner HandleServiceMonitor class

    private class HandleUser implements Runnable {

        @Override
        public void run() {

            try (BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {
                String line = null;

                while ((line = userIn.readLine()) != null) {
                    ActionMessage currentMessage = null;

                    try {
                        currentMessage = toActionMessage(line);
                    } catch (MessageTypeException e) {
                        e.printStackTrace();
                        continue;
                    }

                    smsOut.writeObject(currentMessage);
                    smsOut.flush();
                }
            } catch (SocketException e) {
                // Broken Pipe
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }// close inner HandleUser class
} // clsoe Caller class
