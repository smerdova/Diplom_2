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

public class UserPatchTest {
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
    public void patchEmailWithTokenReturnsSuccess() {
        String newEmail = faker.internet().emailAddress();
        AuthSteps.patchUser(new AuthRequest(newEmail, null, null), accessToken)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(this.request.getName()));
    }

    @Test
    public void patchNameWithTokenReturnsSuccess() {
        String newName = faker.name().firstName();
        AuthSteps.patchUser(new AuthRequest(null, null, newName), accessToken)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(this.request.getEmail()))
                .body("user.name", equalTo(newName));
    }

    @Test
    public void patchEmailWithoutTokenFails() {
        String newEmail = faker.internet().emailAddress();
        AuthSteps.patchUser(new AuthRequest(newEmail, null, null), "")
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    public void patchNameWithoutTokenFails() {
        String newName = faker.name().firstName();
        AuthSteps.patchUser(new AuthRequest(null, null, newName), "")
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthSteps.delete(accessToken)
                    .statusCode(202);
        }
    }
}
