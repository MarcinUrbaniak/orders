package com.example.orders;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class OrdersApp extends NanoHTTPD {


    public OrdersApp(int port) throws IOException {
        super(port);
        start(5000, false);
        System.out.println("Server has been started");
    }

    public static void main(String[] args) {
        try {
            new OrdersApp(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


