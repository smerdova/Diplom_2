package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.Configuration;
import ru.praktikum.model.AuthRequest;

import static io.restassured.RestAssured.given;

public abstract class IngredientSteps {
    @Step("Отправляем GET запрос на /api/ingredients")
    public static ValidatableResponse getList() {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .when()
                .get("/api/ingredients")
                .then();
    }
}
