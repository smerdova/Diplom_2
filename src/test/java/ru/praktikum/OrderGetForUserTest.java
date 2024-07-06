package ru.praktikum;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.praktikum.model.AuthRequest;
import ru.praktikum.model.OrderRequest;
import ru.praktikum.steps.AuthSteps;
import ru.praktikum.steps.IngredientSteps;
import ru.praktikum.steps.OrderSteps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static ru.praktikum.Configuration.ENABLE_TRACE_LOGS;

public class OrderGetForUserTest {
    private final Faker faker = new Faker();
    private AuthRequest authRequest;
    private String accessToken;

    @Before
    public void setup() {
        if (ENABLE_TRACE_LOGS) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }

        this.authRequest = new AuthRequest(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );

        accessToken = AuthSteps.register(authRequest)
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().body().path("accessToken");
    }

    @Test
    public void getOrderWithTokenReturnsSuccess() {
        List<String> ingredientIds = IngredientSteps.getList()
                .statusCode(200)
                .extract().path("data._id");
        Collections.shuffle(ingredientIds);

        OrderRequest orderRequest = new OrderRequest(ingredientIds.subList(0,2));
        String orderId = OrderSteps.create(orderRequest, accessToken)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue())
                .extract().body().path("order._id");

        OrderSteps.get(accessToken)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders[0]._id", equalTo(orderId))
                .body("orders[0].status", notNullValue())
                .body("orders[0].name", notNullValue())
                .body("orders[0].createdAt", notNullValue())
                .body("orders[0].updatedAt", notNullValue())
                .body("orders[0].number", notNullValue())
                .body("orders[0].ingredients", hasItems(
                        ingredientIds.get(0),
                        ingredientIds.get(1)))
                .body("total", notNullValue())
                .body("totalToday", notNullValue());

    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthSteps.delete(accessToken)
                    .statusCode(202);
        }
    }
}
