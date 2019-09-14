package com.example.orders.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;


@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Order {
    int orderId, customer_id;
    Date orderDate;

}
