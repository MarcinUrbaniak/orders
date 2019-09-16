package com.example.orders.controller;

import com.example.orders.storage.OrderStorage;
import com.example.orders.storage.impl.OrderStorageImpl;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD.*;


import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fi.iki.elonen.NanoHTTPD.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;

public class OrderController {

    private OrderStorage orderStorage = new OrderStorageImpl();

    public Response serveGetOrderRequest(IHTTPSession session){

        return null;
    }

    public Response serveGetOrdersRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        String response = "";
        try {
            response = objectMapper.writeValueAsString(orderStorage.getAllOrders());
            System.out.println("response = " + response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error can't read all orders");
        }
        return newFixedLengthResponse(OK,"application/json", response);
    }

    public Response serveAddOrderRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = new Order();

        String headers = session.getHeaders().get("content-length");
        int contentLength = Integer.parseInt(headers);
        byte[] buffer = new byte[contentLength];

        try {
            session.getInputStream().read(buffer,0,contentLength);
            String requestBody = new String(buffer);
            //create order
              JsonNode jsonNodeRoot = objectMapper.readTree(requestBody);
              JsonNode jsonOrder = jsonNodeRoot.get(0);
              order.setCustomer_id(jsonOrder.get("customer_id").asInt());
              order.setOrderDate(Date.valueOf(jsonOrder.get("orderDate").asText()));
            //create orderItem list
            for (int i = 1; i < jsonNodeRoot.size(); i++) {
                OrderItem orderItem = new OrderItem();
                JsonNode jsonOrderItem = jsonNodeRoot.get(i);
                orderItem.setBook_id(jsonOrderItem.get("book_id").asInt());
                orderItem.setAmmount(jsonOrderItem.get("ammount").decimalValue());

                orderItems.add(orderItem);
            }
        } catch (IOException e) {
            System.err.println("Error during process request \n"+ e );
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error. Order hasn't been adder");
        }

        orderStorage.addOrderAndItems(order, orderItems);

        return newFixedLengthResponse(OK, "text/plain", "Order has been added");
    }
}
