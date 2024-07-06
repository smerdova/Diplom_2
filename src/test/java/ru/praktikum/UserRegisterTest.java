package ru.praktikum;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.praktikum.model.AuthRequest;
import ru.praktikum.steps.AuthSteps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static ru.praktikum.Configuration.ENABLE_TRACE_LOGS;

public class UserRegisterTest {

    private AuthRequest request;

    private String accessToken;

    @Before
    public void setup() {
        if (ENABLE_TRACE_LOGS) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }

        Faker faker = new Faker();
        this.request = new AuthRequest(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );
    }

    @Test
    public void registerUserReturnsSuccess() {
        accessToken = AuthSteps.register(request)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(this.request.getEmail()))
                .body("user.name", equalTo(this.request.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().body().path("accessToken");
    }

    @Test
    public void registerTwoTheSameUsersFails() {
        accessToken = AuthSteps.register(request)
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().body().path("accessToken");

        AuthSteps.register(request)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    public void registerUserWithoutEmailFails() {
        request.setEmail(null);
        AuthSteps.register(request)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    public void registerUserWithoutPasswordFails() {
        request.setPassword(null);
        AuthSteps.register(request)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    public void createCourierWithoutNameFails() {
        request.setName(null);
        AuthSteps.register(request)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthSteps.delete(accessToken)
                    .statusCode(202);
        }
    }
}
