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
            "  \"customer_id\": 2,\n" +
            "  \"orderDate\": \"2019-05-08\"\n" +
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
        deleteData();
        ordersApp.stop();
    }


    @Test
    public void addMethodOrder_correctBody_shouldReturnStatus200() {
        with()
                .body(ORDER_AND_ITEMS_1)
                .when().post("/order/add")
                .then().statusCode(200)
                .body(startsWith("Order has been added"));
    }


    @Test
    public void addMethodOrder_inCorrectBody_shouldReturnStatus500() {
        with()
                .body(ORDER_AND_ITEMS_2)
                .when().post("/order/add")
                .then().statusCode(500)
                .body(equalTo("Internal error. Order hasn't been added"));
    }


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



    private int[] addOrderAndGetId(String json) {
        String responseText = with()
                .body(json)
                .when().post("/order/add")
                .then().statusCode(200).body(startsWith("Order has been added ="))
                .extract().body().asString();

        String id = responseText.substring(responseText.indexOf("=") + 1, responseText.indexOf("items") -1);
        String item = responseText.substring(responseText.indexOf("[")+1, responseText.indexOf("]"));
        String items[] = item.split(",");
        int[] itemsInt = new int[items.length+1];
        System.out.println("itemsInt.length = " + itemsInt.length);

        for (int i = 0; i <items.length; i++) {
            itemsInt[i] = Integer.parseInt(items[i].trim());
        }
        itemsInt[itemsInt.length-1] = Integer.parseInt(id);

        return itemsInt;

    }

    private void deleteData() {
        ordersApp.getRequestUrlMapper()
                .getOrderController()
                .getOrderStorage()
                .clearTablesOrderOrderItem();
    }


    @Test
    public void getMetchod_correctID_shouldReturnStatus200() {
        int[] orderIds = addOrderAndGetId(ORDER_AND_ITEMS_1);
        int orderId = orderIds[orderIds.length-1];

        with().param("order_id", orderId)
                .when().get("/order/get")
                .then().statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("customer_id", equalTo(3))
                .body("orderDate", equalTo("2019-01-08"));
    }


    @Test
    public void getMethod_noOrderIdParameter_ShouldReturnStatus500() {
        with().get("/order/get")
                .then().statusCode(400)
                .body(equalTo("Uncorrect request params"));
    }


    @Test
    public void getMethod_OrderIdAsText_shouldReturnStatus400() {
        with().param("order_id", "abc")
                .when().get("/order/get")
                .then().statusCode(400)
                .body(equalTo("Order id hasn't been a number"));
    }


    @Test
    public void getMethod_orderDoesNotExist_shouldReturStatus404() {
        with().param("order_id", 999999)
                .when().body("/order/get")
                .then().statusCode(404)
                .body(equalTo(""));
    }


    @Test
    public void getAllMethod_0Order_shouldReturn200() {

        when().get("/order/getAll")
                .then().statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void getAllMethod_1Order_shouldReturn200() {
        int[] orderIds = addOrderAndGetId(ORDER_AND_ITEMS_1);
        int orderId = orderIds[orderIds.length-1];


        when().get("/order/getAll")
                .then().statusCode(200)
                .body("", hasSize(1))
                .body("orderId", hasItem(orderId))
                .body("customer_id", hasItem(3))
                .body("orderDate", hasItem("2019-01-08"));

    }

    @Test
    public void getAllMethod_2Orders_shouldReturn200(){

        int[] orderIds1 = addOrderAndGetId(ORDER_AND_ITEMS_1);
        int[] orderIds2 = addOrderAndGetId(ORDER_AND_ITEMS_3);
        int orderId1 = orderIds1[orderIds1.length-1];
        int orderId2 = orderIds2[orderIds2.length-1];

        when().get("/order/getAll")
                .then().statusCode(200)
                .body("",hasSize(2))
                .body("orderId", hasItems(orderId1, orderId2))
                .body("customer_id", hasItems(3,2))
                .body("orderDate", hasItems("2019-01-08", "2019-05-08"));
    }

    @Test
    public void delMethod_ExistingOrderShouldReturn200(){
        int[] orderIds = addOrderAndGetId(ORDER_AND_ITEMS_1);
        int orderId = orderIds[orderIds.length-1];
        with().param("order_id", orderId)
                .when().delete("/order/del")
                .then().statusCode(200)
                .body( equalTo("Order has been deleted"));

    }

    @Test
    public void delMethod_noOrderIdParameter_shouldReturnStatus400(){
        when().delete("/order/del")
                .then().statusCode(400)
                .body(equalTo("Uncorrect request params"));
    }

    @Test
    public void delMethod_wrongTypeOfOrderIdParameter_shouldReturnStatus400(){
        with().param("order_id", "abdd")
                .when().delete("/order/del")
                .then().statusCode(400)
                .body(equalTo("Uncorrect order_id format"));
    }

    @Test
    public void delMethod_odrerIdDoesNotExist_shouldReturnStatus404(){
        with().param("order_id", 8000)
                .when().delete("/order/del")
                .then().statusCode(404)
                .body(equalTo("Order hasn't been found"));
    }

    @Test
    public void changeMethod_existingOrder_shouldReturnStatus200(){
        int[] orderIds1 = addOrderAndGetId(ORDER_AND_ITEMS_1);
        int orderId1 = orderIds1[orderIds1.length-1];

        String changeBody = "[{\n" +
                "    \"orderId\":" +
                orderId1 +
                ",\n" +
                "    \"customer_id\": 3,\n" +
                "    \"orderDate\": \"2019-04-09\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\":" +
                orderIds1[0] +
                ",\n" +
                "    \"book_id\": 1,\n" +
                "    \"ammount\": 450\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\":" +
                orderIds1[1] +
                ",\n" +
                "    \"book_id\": 2,\n" +
                "    \"ammount\": 550\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\":" +
                orderIds1[2] +
                ",\n" +
                "    \"book_id\": 3,\n" +
                "    \"ammount\": 550\n" +
                "  }\n" +
                "]";

        with().body(changeBody).
        when().put("/order/change")
                .then().statusCode(200)
                .body(equalTo("Order has been changed"));

    }

    @Test
    public void changeMethod_noExistingOrder_shouldReturnStatus404(){
        int orderId1 = 9999999;
        String changeBody = "[{\n" +
                "    \"orderId\":" +
                orderId1 +
                ",\n" +
                "    \"customer_id\": 3,\n" +
                "    \"orderDate\": \"2019-04-09\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\": 1,\n" +
                "    \"book_id\": 1,\n" +
                "    \"ammount\": 450\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\": 2,\n" +
                "    \"book_id\": 2,\n" +
                "    \"ammount\": 550\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\": 3,\n" +
                "    \"book_id\": 3,\n" +
                "    \"ammount\": 550\n" +
                "  }\n" +
                "]";

        with().body(changeBody)
                .when().put("/order/change")
                .then().statusCode(404)
                .body(equalTo("Order or item hasn't been found"));
    }

    @Test
    public void changeMethod_noOrderIdinBody_shouldReturnStatus500(){

        String changeBody = "[{\n" +
                "    \"orderId\":" +
                ",\n" +
                "    \"customer_id\": 3,\n" +
                "    \"orderDate\": \"2019-04-09\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\": 1,\n" +
                "    \"book_id\": 1,\n" +
                "    \"ammount\": 450\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\": 2,\n" +
                "    \"book_id\": 2,\n" +
                "    \"ammount\": 550\n" +
                "  },\n" +
                "  {\n" +
                "    \"item_id\": 3,\n" +
                "    \"book_id\": 3,\n" +
                "    \"ammount\": 550\n" +
                "  }\n" +
                "]";
        with().body(changeBody)
                .with().put("/order/change")
                .then().statusCode(500)
                .body(equalTo("Internal error. Order hasn't been changed"));

    }

}


