package com.example.orders.type;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItem {
    int item_id, book_id, order_id;
    BigDecimal ammmount;

}
