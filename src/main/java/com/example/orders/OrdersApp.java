package com.example.orders;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class OrdersApp extends NanoHTTPD {

    RequestUrlMapper requestUrlMapper = new RequestUrlMapper();

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

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session){
        return requestUrlMapper.delegateRequest(session);
    }

    public RequestUrlMapper getRequestUrlMapper() {
        return requestUrlMapper;
    }
}


