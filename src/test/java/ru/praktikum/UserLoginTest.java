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

public class UserLoginTest {
    private final Faker faker = new Faker();

    private AuthRequest request;
    private String accessToken;

    @Before
    public void setup() {
        if (ENABLE_TRACE_LOGS) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }

        this.request = new AuthRequest(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );

        accessToken = AuthSteps.register(request)
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().body().path("accessToken");
    }

    @Test
    public void loginUserReturnsSuccess() {
        AuthSteps.login(request)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(this.request.getEmail()))
                .body("user.name", equalTo(this.request.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    public void loginUserWithoutEmailFails() {
        request.setEmail(null);
        AuthSteps.login(request)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void loginUserWithoutPasswordFails() {
        request.setPassword(null);
        AuthSteps.login(request)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void loginUserWithWrongEmailFails() {
        request.setEmail(faker.internet().emailAddress());
        AuthSteps.login(request)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void loginUserWithWrongPasswordFails() {
        request.setPassword(faker.internet().password());
        AuthSteps.login(request)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthSteps.delete(accessToken)
                    .statusCode(202);
        }
    }
}
