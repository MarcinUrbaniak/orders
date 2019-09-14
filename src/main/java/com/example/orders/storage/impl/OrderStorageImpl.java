package com.example.orders.storage.impl;

import com.example.orders.storage.OrderStorage;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderStorageImpl implements OrderStorage {

    private static List<Order> orderList = new ArrayList<>();

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/book_store";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASS = "postgres";


    @Override
    public Order getOrder(int order_id) {
        return null;
    }

    @Override
    public List<Order> getAllOrders() {
        return null;
    }

    @Override
    public void addOrder(Order order, OrderItem orderItem) {

        try {
            Connection connection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASS);
            PreparedStatement preparedStatementOrder = connection.prepareStatement("");
            PreparedStatement preparedStatementOrderItem = connection.prepareStatement("");

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
