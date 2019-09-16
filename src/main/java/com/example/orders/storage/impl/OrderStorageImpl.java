package com.example.orders.storage.impl;

import com.example.orders.storage.OrderStorage;
import com.example.orders.type.Customer;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;
import com.sun.tools.corba.se.idl.constExpr.Or;


import javax.swing.plaf.nimbus.State;
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
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        Order order = new Order();

        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE order_id = ?");
            preparedStatement.setInt(1, order_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                order.setOrderId(resultSet.getInt(1));
                order.setOrderDate(resultSet.getDate(2));
                order.setCustomer_id(resultSet.getInt(3));

                return order;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public  List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement("SELECT *FROM orders;");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                Order order = new Order();
                order.setOrderId(resultSet.getInt(1));
                order.setOrderDate(resultSet.getDate(2));
                order.setCustomer_id(resultSet.getInt(3));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDataBaseConnection(connection, preparedStatement);
        }

        return orders;
    }



    @Override
    public void addOrderAndItems(Order order, List<OrderItem> orderItems) {

        Connection connection = getConnection();

        PreparedStatement preparedStatementOrder = null;
        PreparedStatement preparedStatementOrderItem = null;

        try {
            preparedStatementOrder = connection.prepareStatement("INSERT INTO orders (order_date, customer_id)" +
                    "VALUES (?, ?) RETURNING order_id;");
            preparedStatementOrderItem = connection.prepareStatement("INSERT INTO" +
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
                preparedStatementOrderItem.setBigDecimal(3, orderItem.getAmmount());
                preparedStatementOrderItem.execute();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDataBaseConnection(connection, preparedStatementOrder);
            closeDataBaseConnection(connection, preparedStatementOrderItem);

        }
    }

    private void closeDataBaseConnection(Connection connection, PreparedStatement preparedStatement) {
        try {
            if(preparedStatement != null) preparedStatement.close();
            if(connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection()  {
        try {
            return DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("System can't initialize database connection");
        }
    }
}
