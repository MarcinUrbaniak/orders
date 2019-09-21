package com.example.orders.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class OrderItem {
    int item_id, book_id, order_id;
    BigDecimal ammount;

}
