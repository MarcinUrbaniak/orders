package com.example.orders.storage.impl;

import com.example.orders.storage.OrderStorage;
import com.example.orders.type.Order;
import com.example.orders.type.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderStorageImpl implements OrderStorage {

    //private static List<Order> orderList = new ArrayList<>();
    private int order_id = 0;

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/book_store";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASS = "postgres";

    @Override
    public Order getOrder(int order_id) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement;
        Order order = new Order();

        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE order_id = ?");
            preparedStatement.setInt(1, order_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
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
    public List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement("SELECT *FROM orders;");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
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

            preparedStatementOrder.setDate(1, order.getOrderDate());
            preparedStatementOrder.setInt(2, order.getCustomer_id());
            ResultSet resultSet = preparedStatementOrder.executeQuery();
            resultSet.next();
            order_id = resultSet.getInt(1);

            for (OrderItem orderItem : orderItems
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

    @Override
    public boolean deleteOrder(int order_id) {
        Connection connection = getConnection();
        PreparedStatement preparedStatementDelete = null;
        String delOrderItem = " DELETE FROM order_items WHERE order_id = ?;";
        String delOrder = "DELETE FROM orders WHERE order_id = ?;";

        try {
            preparedStatementDelete = connection.prepareStatement(delOrderItem);
            preparedStatementDelete.setInt(1, order_id);
            preparedStatementDelete.execute();

            preparedStatementDelete = connection.prepareStatement(delOrder);
            preparedStatementDelete.setInt(1, order_id);
            preparedStatementDelete.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeDataBaseConnection(connection, preparedStatementDelete);
        }
        return true;
    }

    @Override
    public void changeOrderAndItems(Order order, List<OrderItem> orderItems) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE orders SET order_date=?, customer_id=? WHERE order_id=?;");
            preparedStatement.setDate(1, order.getOrderDate());
            preparedStatement.setInt(2, order.getCustomer_id());
            preparedStatement.setInt(3, order.getOrderId());
            preparedStatement.execute();

            preparedStatement = connection.prepareStatement("UPDATE order_items SET book_id = ?, ammount=? WHERE item_id = ?");

            for (OrderItem orderItem : orderItems
            ) {
                preparedStatement.setInt(1, orderItem.getBook_id());
                preparedStatement.setBigDecimal(2, orderItem.getAmmount());
                preparedStatement.setInt(3, orderItem.getItem_id());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDataBaseConnection(connection, preparedStatement);
        }
    }

    ;

    private void closeDataBaseConnection(Connection connection, PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("System can't initialize database connection");
        }
    }

    public int getOrder_id() {
        return order_id;
    }

    public  void clearTablesOrderOrderItem() {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM order_items;" +
                    "DELETE FROM orders;");
            preparedStatement.execute();

        } catch (SQLException sql) {
            sql.printStackTrace();
        }
        closeDataBaseConnection(connection, preparedStatement);
    }

}
