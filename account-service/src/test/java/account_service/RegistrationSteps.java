package account_service;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationSteps {

    private String currentPage = "";
    private HttpResponse<String> response;
	
    /* Scenario: Successful registration */
    
    @Given("I am on the registration page")
    public void iAmOnTheRegistrationPage() {
        this.currentPage = "registration";
    }

    @Given("I have not an account")
    public void iHaveNotAnAccount() {
    }

    @When("I create an account with a username {string} and a valid password {string}")
    public void iCreateAnAccountWithAUsernameAndAValidPassword(final String username, final String pwd) {
        assertThat(this.currentPage).isEqualTo("registration");
        try {
             this.response = SetupSteps.doPost(SetupSteps.ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                    "userName", username,
                    "password", pwd)
            ));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that my account has been created and receive my identifier")
    public void iShouldSeeConfirmationAndReceiveMyIdentifier() {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertTrue(new JsonObject(this.response.body()).containsKey("accountId"));
    }
}
