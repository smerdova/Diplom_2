package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.Configuration;
import ru.praktikum.model.AuthRequest;

import static io.restassured.RestAssured.given;

public abstract class AuthSteps {
    @Step("Отправляем POST запрос на /api/auth/register")
    public static ValidatableResponse register(AuthRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .body(request)
                .when()
                .post("/api/auth/register")
                .then();
    }

    @Step("Отправляем POST запрос на /api/auth/login")
    public static ValidatableResponse login(AuthRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then();
    }

    @Step("Отправляем PATCH запрос на /api/auth/user")
    public static ValidatableResponse patchUser(AuthRequest request, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .header("Authorization", accessToken)
                .body(request)
                .when()
                .patch("/api/auth/user")
                .then();
    }

    @Step("Отправляем DELETE запрос на /api/auth/user")
    public static ValidatableResponse delete(String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Configuration.BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then();
    }
}
