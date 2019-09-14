package com.example.orders.storage.impl;

import com.example.orders.storage.OrderStorage;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;


import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
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

    public static void main(String[] args) {
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();

        OrderItem orderItem1 = new OrderItem();
        OrderItem orderItem2 = new OrderItem();

        OrderStorageImpl orderStorage = new OrderStorageImpl();
        order.setOrderDate(Date.valueOf("2019-08-05"));
        order.setCustomer_id(2);

        orderItem1.setAmmmount(BigDecimal.valueOf(300));
        orderItem1.setBook_id(1);
        orderItem2.setAmmmount(BigDecimal.valueOf(500));
        orderItem2.setBook_id(2);
        Collections.addAll(orderItems, orderItem1, orderItem2);

        orderStorage.addOrder(order, orderItems);

    }

    @Override
    public void addOrder(Order order, List<OrderItem> orderItems) {

        try {
            Connection connection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASS);
            PreparedStatement preparedStatementOrder = connection.prepareStatement("INSERT INTO orders (order_date, customer_id)" +
                    "VALUES (?, ?) RETURNING order_id;");
            PreparedStatement preparedStatementOrderItem = connection.prepareStatement("INSERT INTO" +
                    " order_items (book_id, order_id, ammount) " +
                    "VALUES (?,?,?)");

            preparedStatementOrder.setDate(1,order.getOrderDate());
            preparedStatementOrder.setInt(2, order.getCustomer_id());
            ResultSet resultSet = preparedStatementOrder.executeQuery();
            resultSet.next();

            for (OrderItem orderItem: orderItems
                 ) {
                preparedStatementOrderItem.setInt(1, orderItem.getBook_id());
                preparedStatementOrderItem.setInt(2, resultSet.getInt(1));
                preparedStatementOrderItem.setBigDecimal(3, orderItem.getAmmmount());
                preparedStatementOrderItem.execute();
            }

            preparedStatementOrderItem.close();
            preparedStatementOrder.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
