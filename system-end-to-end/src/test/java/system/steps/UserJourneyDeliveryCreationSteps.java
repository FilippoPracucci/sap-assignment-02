package system.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;

public class UserJourneyDeliveryCreationSteps {

    private HttpResponse<String> response;
    private String userSessionId = "";

    /* Scenario: Successful user registration and delivery creation */

    @Given("The system is running")
    public void theSystemIsRunning() {
        SetupSteps.runDockerComposeUp();
        try {
            this.response = SetupSteps.doGet(SetupSteps.HEALTH_CHECK_ENDPOINT);
            assertThat(this.response.statusCode()).isEqualTo(200);
            assertThat(new JsonObject(this.response.body()).getString("status")).isEqualTo("UP");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @And("The system has no users with id {string}")
    public void theSystemHasNoUsersWithId(final String userId) {
        try {
            this.response = SetupSteps.doGet(SetupSteps.ACCOUNT_RESOURCE_PATH.replace(":accountId", userId));
            assertThat(this.response.statusCode()).isEqualTo(200);
            assertThat(new JsonObject(this.response.body()).getString("result")).isEqualTo("error");
            assertThat(new JsonObject(this.response.body()).getString("error"))
                    .isEqualTo("Account not present");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @And("The system has no deliveries with id {string}")
    public void theSystemHasNoDeliveriesWithId(final String deliveryId) {
        try {
            this.response = SetupSteps.doGet(SetupSteps.DELIVERY_RESOURCE_PATH.replace(":deliveryId", deliveryId));
            assertThat(this.response.statusCode()).isEqualTo(200);
            assertThat(new JsonObject(this.response.body()).getString("result")).isEqualTo("error");
            assertThat(new JsonObject(this.response.body()).getString("error"))
                    .isEqualTo("Delivery does not exist");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @When("I register with a username {string} and a valid password {string}")
    public void iRegisterWithAUsernameAndAValidPassword(final String userName, final String password) {
        try {
            this.response = SetupSteps.doPost(SetupSteps.ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                    "userName", userName,
                    "password", password)
            ));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that my account was created and receive my identifier {string}")
    public void iShouldSeeAConfirmationThatMyAccountWasCreatedAndReceiveMyIdentifier(final String userId) {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertThat(new JsonObject(this.response.body()).getString("accountId")).isEqualTo(userId);
    }

    @When("I login with userId {string} and password {string}")
    public void iLoginWithUserIdAndPassword(final String userId, final String password) {
        try {
            this.response = SetupSteps.doPost(SetupSteps.LOGIN_RESOURCE_PATH.replace(":accountId", userId),
                    new JsonObject(Map.of("password", password)));
            this.userSessionId = new JsonObject(this.response.body()).getString("sessionId");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that I logged in")
    public void iShouldSeeAConfirmationThatILoggedIn() {
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @When("I request to create a delivery with weight {string} kg, starting place {string}, destination place {string} to ship immediately")
    public void iRequestToCreateADeliveryWithWeightKgStartingPlaceDestinationPlaceToShipImmediately(
            final String weight,
            final String startingPlace,
            final String destinationPlace
    ) {
        try {
            this.response = SetupSteps.doPost(
                    SetupSteps.CREATE_DELIVERY_RESOURCE_PATH.replace(":sessionId", this.userSessionId),
                    SetupSteps.toJson(weight, startingPlace, destinationPlace)
            );
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that the delivery has been created and receive its identifier {string}")
    public void iShouldSeeAConfirmationThatTheDeliveryHasBeenCreatedAndReceiveItsIdentifier(final String deliveryId) {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertThat(new JsonObject(this.response.body()).getString("deliveryId")).isEqualTo(deliveryId);
    }

    @When("I request to get the delivery detail of the delivery {string}")
    public void iRequestToGetTheDeliveryDetailOfTheDelivery(final String deliveryId) {
        try {
            this.response = SetupSteps.doGet(SetupSteps.DELIVERY_RESOURCE_PATH.replace(":deliveryId", deliveryId));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should get the delivery detail with weight {string} kg, starting place {string}, destination place {string}")
    public void iShouldGetTheDeliveryDetailWithWeightKgStartingPlaceDestinationPlace(
            final String weight,
            final String startingPlace,
            final String destinationPlace
    ) {
        assertThat(this.response.statusCode()).isEqualTo(200);
        final JsonObject deliveryDetail = new JsonObject(this.response.body()).getJsonObject("deliveryDetail");
        final String[] startingStreet = startingPlace.split(", ");
        final String[] destinationStreet = destinationPlace.split(", ");
        assertAll(
                () -> assertThat(deliveryDetail.getNumber("weight").doubleValue()).isEqualTo(Double.parseDouble(weight)),
                () -> assertThat(deliveryDetail.getJsonObject("startingPlace"))
                        .isEqualTo(new JsonObject(Map.of(
                                "street", startingStreet[0],
                                "number", Integer.parseInt(startingStreet[1])
                        ))),
                () -> assertThat(deliveryDetail.getJsonObject("destinationPlace"))
                        .isEqualTo(new JsonObject(Map.of(
                                "street", destinationStreet[0],
                                "number", Integer.parseInt(destinationStreet[1])
                        )))
        );
        SetupSteps.runDockerComposeDown();
    }
}
