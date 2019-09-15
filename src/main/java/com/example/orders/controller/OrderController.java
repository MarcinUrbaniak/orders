package com.example.orders.controller;

import com.example.orders.storage.OrderStorage;
import com.example.orders.storage.impl.OrderStorageImpl;
import fi.iki.elonen.NanoHTTPD.*;

public class OrderController {

    private OrderStorage orderStorage = new OrderStorageImpl();

    public Response serveGetOrderRequest(IHTTPSession session){return null;}

    public Response serveGetOrdersRequest(IHTTPSession session){return null;}

    public Response serveAddOrderRequest(IHTTPSession session){return null;}

}
