package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.Configuration;
import ru.praktikum.model.OrderRequest;

import static io.restassured.RestAssured.given;

public abstract class OrderSteps {
    @Step("Отправляем POST запрос на /api/orders")
    public static ValidatableResponse create(OrderRequest request, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .header("Authorization", accessToken)
                .body(request)
                .when()
                .post("/api/orders")
                .then();
    }

    @Step("Отправляем GET запрос на /api/orders")
    public static ValidatableResponse get(String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders")
                .then();
    }
}
