package com.company;

public class Main {

    public static void main(String[] args) {
        Caller caller1 = new Caller("localhost",
                3000);
        caller1.start();
    }
}
