package com.example.orders.storage;

import com.example.orders.type.Order;

import java.util.List;

public interface OrderStorage {

    Order getOrder(int order_id);

    List<Order> getAllOrders();

    void addOrder(Order order);

}
