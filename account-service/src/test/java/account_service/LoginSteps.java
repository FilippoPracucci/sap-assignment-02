package account_service;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class LoginSteps {

    private String currentPage = "";
    private HttpResponse<String> response;

    private static final int ACCOUNT_SERVICE_PORT = 9000;
    private static final String ACCOUNT_ENDPOINT = "http://localhost:" + ACCOUNT_SERVICE_PORT + "/api/v1";
    static final String CHECK_PWD_RESOURCE_PATH = ACCOUNT_ENDPOINT + "/accounts/:accountId/check-pwd";

    /* Scenario: Successful login */

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        this.currentPage = "login";
    }

    @Given("I have an account")
    public void iHaveAnAccount() {
    }

    private void login(final String userId, final String pwd) {
        try {
            this.response = SetupSteps.doPost(
                    CHECK_PWD_RESOURCE_PATH.replace(":accountId", userId),
                    new JsonObject(Map.of("password", pwd))
            );
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @When("I login with my userId {string} and my password {string}")
    public void iLoginWithMyUserIdAndMyPassword(final String userId, final String pwd) {
        assertThat(this.currentPage).isEqualTo("login");
        this.login(userId, pwd);
    }

    @Then("I should access to the system for delivering packages")
    public void iShouldAccessToTheSystemForDeliveringPackages() {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertThat(new JsonObject(this.response.body()).getString("result")).isEqualTo("valid-password");
    }

    /* Scenario: Login fails without having an account */

    @When("I login with userId {string} and the password {string}")
    public void iLoginWithUserIdAndThePassword(final String userId, final String pwd) {
        assertThat(this.currentPage).isEqualTo("login");
        this.login(userId, pwd);
    }

    @Then("I should see an error {string}")
    public void iShouldSeeAnError(final String message) {
        assertThat(new JsonObject(this.response.body()).getString("error")).isEqualTo(message);
    }

    @And("I should not access to the system")
    public void iShouldNotAccessToTheSystem() {
        assertThat(this.currentPage).isEqualTo("login");
    }

    /* Scenario: Login fails with wrong password */

    @And("I have an account with userId {string} and password {string}")
    public void iHaveAnAccountWithUserIdAndPassword(final String userId, final String pwd) {
        this.login(userId, pwd);
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertThat(new JsonObject(this.response.body()).getString("result")).isEqualTo("valid-password");
    }

    @Then("I should see a message {string}")
    public void iShouldSeeAMessage(final String message) {
        final String responseMessage = new JsonObject(this.response.body()).getString("result");
        final String messageToShow = responseMessage.equals("valid-password") ? "Correct password" : "Wrong password";
        assertThat(messageToShow).isEqualTo(message);
    }
}
