package com.example.orders.type;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Order {
    int orderId, customer_id;
    LocalDate orderDate;

}
