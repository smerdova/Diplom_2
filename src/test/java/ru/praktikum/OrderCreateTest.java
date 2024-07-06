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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static ru.praktikum.Configuration.ENABLE_TRACE_LOGS;

public class OrderCreateTest {
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
    public void createOrderWithAuthReturnsSuccess() {
        List<String> ingredientIds = IngredientSteps.getList()
                .statusCode(200)
                .extract().path("data._id");
        Collections.shuffle(ingredientIds);

        OrderRequest orderRequest = new OrderRequest(ingredientIds.subList(0,2));
        OrderSteps.create(orderRequest, accessToken)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    public void createOrderWithoutAuthReturnsSuccess() {
        List<String> ingredientIds = IngredientSteps.getList()
                .statusCode(200)
                .extract().path("data._id");
        Collections.shuffle(ingredientIds);

        OrderRequest orderRequest = new OrderRequest(ingredientIds.subList(0,2));
        OrderSteps.create(orderRequest, "")
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    public void createOrderWithoutIngredientsFails() {
        OrderRequest orderRequest = new OrderRequest(new ArrayList<>());
        OrderSteps.create(orderRequest, accessToken)
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWrongIngredientsFails() {
        ArrayList<String> wrongIngredients = new ArrayList<>(1);
        wrongIngredients.add("123456789");
        OrderRequest orderRequest = new OrderRequest(wrongIngredients);
        OrderSteps.create(orderRequest, accessToken)
                .statusCode(500);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthSteps.delete(accessToken)
                    .statusCode(202);
        }
    }
}
