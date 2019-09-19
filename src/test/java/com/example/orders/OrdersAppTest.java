package com.example.orders;

import com.example.orders.storage.OrderStorage;
import com.example.orders.storage.impl.OrderStorageImpl;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Method;

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
    private static final String ORDER_AND_ITEMS_3 = "[{\n" +
            "  \"customer_id\": 3,\n" +
            "  \"orderDate\": \"2019-01-08\"\n" +
            "}, {\n" +
            "  \"book_id\": 1,\n" +
            "  \"ammount\": 440 \n" +
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

    private OrdersApp ordersApp;


    @BeforeAll
    public static void beforeAll() {
        RestAssured.port = APP_PORT;

    }

    @BeforeEach
    public void beforeEach() throws IOException {
        ordersApp = new OrdersApp(APP_PORT);
    }

    @AfterEach
    public void afterEach() {
        ordersApp.stop();
    }

    @Disabled
    @Test
    public void addMethodOrder_correctBody_shouldReturnStatus200() {
        with()
                .body(ORDER_AND_ITEMS_1)
                .when().post("/order/add")
                .then().statusCode(200)
                .body(equalTo("Order has been added"));
    }

    @Disabled
    @Test
    public void addMethodOrder_inCorrectBody_shouldReturnStatus500() {
        with()
                .body(ORDER_AND_ITEMS_2)
                .when().post("/order/add")
                .then().statusCode(500)
                .body(equalTo("Internal error. Order hasn't been added"));
    }

    @Disabled
    @Test
    public void addMethodOrder_unexpectedField_shouldReturnStatus500() {
        with()
                .body("[{\n" +
                        "  \"name\": 2\n" +
                        "}\n" +
                        "]")
                .when().post("/order/add")
                .then().statusCode(500)
                .body(equalTo("Internal error. Order hasn't been added"));
    }


    private int addOrderAndGetId(String json) {
        String responseText = with()
                .body(json)
                .when().post("/order/add")
                .then().statusCode(200).body(startsWith("Order has been added ="))
                .extract().body().asString();
        String id = responseText.substring(responseText.indexOf("=") + 1);
        return Integer.parseInt(id);

    }

    private void deleteData() {
        ordersApp.getRequestUrlMapper()
                .getOrderController()
                .getOrderStorage()
                .clearTablesOrderOrderItem();
    }

    @Disabled
    @Test
    public void getMetchod_correctID_shouldReturnStatus200() {
        int orderId = addOrderAndGetId(ORDER_AND_ITEMS_1);

        with().param("order_id", orderId)
                .when().get("/order/get")
                .then().statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("customer_id", equalTo(3))
                .body("orderDate", equalTo("2019-01-08"));
    }

    @Disabled
    @Test
    public void getMethod_noOrderIdParameter_ShouldReturnStatus500() {
        with().get("/order/get")
                .then().statusCode(400)
                .body(equalTo("Uncorrect request params"));
    }

    @Disabled
    @Test
    public void getMethod_OrderIdAsText_shouldReturnStatus400() {
        with().param("order_id", "abc")
                .when().get("/order/get")
                .then().statusCode(400)
                .body(equalTo("Order id hasn't been a number"));
    }

    @Disabled
    @Test
    public void getMethod_orderDoesNotExist_shouldReturStatus404() {
        with().param("order_id", 999999)
                .when().body("/order/get")
                .then().statusCode(404)
                .body(equalTo(""));
    }

    @Disabled
    @Test
    public void getAllMethod_0Order_shouldReturn200() {
        deleteData();
        when().get("/order/getAll")
                .then().statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void getAllMethod_1Order_shouldReturn200() {
        deleteData();
        int orderId = addOrderAndGetId(ORDER_AND_ITEMS_1);
        when().get("/order/getAll")
                .then().statusCode(200)
                .body("", hasSize(1))
                .body("orderId", hasItem(orderId))
                .body("customer_id", hasItem(3))
                .body("orderDate", hasItem("2019-01-08"));


    }

}


