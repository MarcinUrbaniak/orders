package com.example.orders.controller;

import com.example.orders.storage.impl.OrderStorageImpl;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import fi.iki.elonen.NanoHTTPD.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;

public class OrderController {

    private OrderStorageImpl orderStorage = new OrderStorageImpl();
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
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error can't read all orders");
        }
        return newFixedLengthResponse(OK,"application/json", response);
    }

    public Response serveChangeOrderRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        boolean isOrderIdAndItemId = false;
        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
        byte[] buffer = new byte[contentLength];
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        try {
            session.getInputStream().read(buffer,0,contentLength);
            String requestBody = new String(buffer);
            JsonNode jsonNodeTree = objectMapper.readTree(requestBody);
            order = objectMapper.readValue(jsonNodeTree.get(0).toString(), Order.class);

            for (int i = 1; i <jsonNodeTree.size() ; i++) {
                OrderItem orderItem = objectMapper.readValue(jsonNodeTree.get(i).toString(),OrderItem.class);
                orderItem.setOrder_id(order.getOrderId());
                orderItems.add(orderItem);
            }
            isOrderIdAndItemId = orderStorage.changeOrderAndItems(order,orderItems);
            if (isOrderIdAndItemId){
                return newFixedLengthResponse(OK, "text/plain", "Order has been changed");
            } else {
                return newFixedLengthResponse(NOT_FOUND, "text/plain", "Order or item hasn't been found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error. Order hasn't been changed");
        }
        //return newFixedLengthResponse(OK, "text/plain", "Order has been changed");
    }

    public Response serveAddOrderRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        //enable unknown properties
        //objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<OrderItem> orderItems = new ArrayList<>();
        Order order;

        String headers = session.getHeaders().get("content-length");
        int contentLength = Integer.parseInt(headers);
        byte[] buffer = new byte[contentLength];

        try {
            session.getInputStream().read(buffer,0,contentLength);
            String requestBody = new String(buffer);
            //create order
              JsonNode jsonNodeRoot = objectMapper.readTree(requestBody);
              order = objectMapper.readValue(jsonNodeRoot.get(0).toString(), Order.class);
              //alternative
              //order.setCustomer_id(jsonOrder.get("customer_id").asInt());
              //order.setOrderDate(Date.valueOf(jsonOrder.get("orderDate").asText()));
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
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error. Order hasn't been added");
        }

        orderStorage.addOrderAndItems(order, orderItems);


        return newFixedLengthResponse(OK, "text/plain", "Order has been added ="
                + orderStorage.getOrderId() + " items: " + orderStorage.getItems().toString());
    }

    public Response serveDelOrderRequest(IHTTPSession session){

        Map<String, List<String>> parameters = session.getParameters();
        boolean isOrderId = false;
        if(parameters.containsKey(ORDER_ID_PARAM_NAME)){
            String orderIdStr = parameters.get(ORDER_ID_PARAM_NAME).get(0);
            try{
                isOrderId = orderStorage.deleteOrder(Integer.parseInt(orderIdStr));
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
                return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Uncorrect order_id format");
            }
            if(isOrderId){
                return newFixedLengthResponse(OK, "text/plain", "Order has been deleted");
            } else{
                return newFixedLengthResponse(NOT_FOUND, "text/plain", "Order hasn't been found");
            }
        }
        return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Uncorrect request params");
    }

    public OrderStorageImpl getOrderStorage() {
        return orderStorage;
    }
}
