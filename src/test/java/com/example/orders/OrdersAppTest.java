package com.example.orders;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class OrdersAppTest {
    private static final String ORDER_AND_ITEMS_1 = "[{\n" +
            "  \"customer_id\": 3,\n" +
            "  \"orderDate\": \"2019-01-08\"\n" +
            "}, {\n" +
            "  \"book_id\": 1,\n" +
            "  \"ammount\": 450\n" +
            "},\n" +
            "  {\n" +
            "    \"book_id\": 2,\n" +
            "    \"ammount\": 550\n" +
            "  },\n" +
            "  {\n" +
            "    \"book_id\": 3,\n" +
            "    \"ammount\": 550\n" +
            "  }\n" +
            "]\n";

    private static final String ORDER_AND_ITEMS_2 = "[{\n" +
            "  \"customer_id\": 3,\n" +
            "  \"orderDate\": \"2019-01-08\"\n" +
            "}, {\n" +
            "  \"book_id\": 1,\n" +
            "  \"ammount\": piecset \n" +
            "},\n" +
            "  {\n" +
            "    \"book_id\": 2,\n" +
            "    \"ammount\": 550\n" +
            "  },\n" +
            "  {\n" +
            "    \"book_id\": 3,\n" +
            "    \"ammount\": 550\n" +
            "  }\n" +
            "]\n";

    private static final int APP_PORT = 8090;

    private  OrdersApp ordersApp;


    @BeforeAll
    public static void beforeAll(){
        RestAssured.port = APP_PORT;

    }

    @BeforeEach
    public void beforeEach() throws IOException {
        ordersApp = new OrdersApp(APP_PORT);
    }

    @AfterEach
    public void afterEach(){
        ordersApp.stop();
    }

    @Disabled
    @Test
    public void addMethodOrder_correctBody_shouldReturnStatus200(){
        with()
                .body(ORDER_AND_ITEMS_1)
                .when().post("/order/add")
                .then().statusCode(200)
                .body(equalTo("Order has been added"));
    }

    @Disabled
    @Test
    public void addMethodOrder_inCorrectBody_shouldReturnStatus500(){
        with()
                .body(ORDER_AND_ITEMS_2)
                .when().post("/order/add")
                .then().statusCode(500)
                .body(equalTo("Internal error. Order hasn't been added"));
    }

    @Disabled
    @Test
    public void addMethodOrder_unexpectedField_ShouldReturnStatus500(){
        with()
                .body("[{\n" +
                        "  \"name\": 2\n" +
                        "}\n" +
                        "]")
                .when().post("/order/add")
                .then().statusCode(500)
                .body(equalTo("Internal error. Order hasn't been added"));
    }
}
