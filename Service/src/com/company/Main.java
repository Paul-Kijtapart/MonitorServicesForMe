package com.company;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Service sa =
                new Service("a", 3001);
        Service sb =
                new Service("b", 3003);
        Service sc =
                new Service("c", 3005);
        Service sd =
                new Service("d", 3006);

        Thread ta =
                new Thread(sa);
        ta.setName("a");
        ta.start();

        Thread tb =
                new Thread(sb);
        tb.setName("b");
        tb.start();

        Thread tc =
                new Thread(sc);
        tc.setName("c");
        tc.start();

        Thread td =
                new Thread(sd);
        td.setName("d");
        td.start();
    }
}
