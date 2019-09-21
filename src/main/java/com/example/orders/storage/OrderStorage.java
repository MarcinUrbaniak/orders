package com.example.orders.storage;

import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;

import java.util.List;

public interface OrderStorage {

    Order getOrder(int order_id);

    List<Order> getAllOrders();

    void addOrderAndItems(Order order, List<OrderItem> orderItems);

    boolean deleteOrder(int order_id);

    boolean changeOrderAndItems(Order order, List<OrderItem> orderItems);



}
