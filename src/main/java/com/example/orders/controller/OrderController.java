package com.example.orders.controller;

import com.example.orders.storage.OrderStorage;
import com.example.orders.storage.impl.OrderStorageImpl;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.iki.elonen.NanoHTTPD.*;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static fi.iki.elonen.NanoHTTPD.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;

public class OrderController {

    private OrderStorage orderStorage = new OrderStorageImpl();
    public static final String ORDER_ID_PARAM_NAME ="order_id";

    public Response serveGetOrderRequest(IHTTPSession session){
        Map<String, List<String>> requestParameters = session.getParameters();

        if(requestParameters.containsKey(ORDER_ID_PARAM_NAME)){
            List<String> orderIdParams = requestParameters.get(ORDER_ID_PARAM_NAME);
            String orderId = orderIdParams.get(0);
            int orderIdInt = 0;

            try {
                orderIdInt = Integer.parseInt(orderId);
            } catch (NumberFormatException nfe){
                System.err.println("Error during convert  " + nfe);
                return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Order id hasn't been a number");
            }
            Order order = orderStorage.getOrder(orderIdInt);
            if(order!=null){
                ObjectMapper objectMapper = new ObjectMapper();
                //disable serialization dates as timestamps
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                try {
                    String response = objectMapper.writeValueAsString(order);
                    return newFixedLengthResponse(OK, "application/json", response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error, can't read all books");
                }
            }
            return newFixedLengthResponse(NOT_FOUND, "text/plain", "Your order haven't been found");
        }

        return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Uncorrect request params");
    }

    public Response serveGetOrdersRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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

        //TODO: obsuzyc sytuacje, w ktorych dostajemy dane, ktorych nie ma w bazie danych (metody statyczne)
        orderStorage.addOrderAndItems(order, orderItems);

        return newFixedLengthResponse(OK, "text/plain", "Order has been added");
    }
}
