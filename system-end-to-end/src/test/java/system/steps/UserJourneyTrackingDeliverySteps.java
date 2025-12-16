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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserJourneyTrackingDeliverySteps {

    private HttpResponse<String> response;
    private String userSessionId = "";
    private String trackingSessionId = "";

    /* Scenario: Successful delivery tracking */

    @And("I registered with username {string} and password {string}")
    public void iRegisteredWithUsernameAndPassword(final String userName, final String password) {
        try {
            this.response = SetupSteps.doPost(SetupSteps.ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                    "userName", userName,
                    "password", password)
            ));
            assertThat(this.response.statusCode()).isEqualTo(200);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @And("I logged in with userId {string} and password {string}")
    public void iLoggedInWithUserIdAndPassword(final String userId, final String password) {
        try {
            this.response = SetupSteps.doPost(SetupSteps.LOGIN_RESOURCE_PATH.replace(":accountId", userId),
                    new JsonObject(Map.of("password", password)));
            assertThat(this.response.statusCode()).isEqualTo(200);
            this.userSessionId = new JsonObject(this.response.body()).getString("sessionId");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @And("I created a delivery with weight {string} kg, starting place {string}, destination place {string} to ship immediately")
    public void iCreatedADeliveryWithWeightKgStartingPlaceDestinationPlaceToShipImmediately(
            final String weight,
            final String startingPlace,
            final String destinationPlace
    ) {
        try {
            this.response = SetupSteps.doPost(
                    SetupSteps.CREATE_DELIVERY_RESOURCE_PATH.replace(":sessionId", this.userSessionId),
                    SetupSteps.toJson(weight, startingPlace, destinationPlace)
            );
            assertThat(this.response.statusCode()).isEqualTo(200);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @When("I request to track the delivery with id {string}")
    public void iRequestToTrackTheDeliveryWithId(final String deliveryId) {
        try {
            this.response = SetupSteps.doPost(
                    SetupSteps.TRACK_DELIVERY_RESOURCE_PATH.replace(":sessionId", this.userSessionId),
                    new JsonObject(Map.of("deliveryId", deliveryId))
            );
            this.trackingSessionId = new JsonObject(this.response.body()).getString("trackingSessionId");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that the delivery has been tracked")
    public void iShouldSeeAConfirmationThatTheDeliveryHasBeenTracked() {
        assertThat(this.response.statusCode()).isEqualTo(200);
    }


    @When("I request to get the delivery status of the delivery {string}")
    public void iRequestToGetTheDeliveryStatusOfTheDelivery(final String deliveryId) {
        try {
            this.response = SetupSteps.doGet(
                    SetupSteps.TRACKING_RESOURCE_PATH.replace(":deliveryId", deliveryId)
                            .replace(":trackingSessionId", this.trackingSessionId)
            );
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should get the delivery status")
    public void iShouldGetTheDeliveryStatus() {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertTrue(new JsonObject(this.response.body()).containsKey("deliveryStatus"));
    }

    @When("I request to stop tracking the delivery with id {string}")
    public void iRequestToStopTrackingTheDeliveryWithId(final String deliveryId) {
        try {
            this.response = SetupSteps.doPost(
                    SetupSteps.STOP_TRACKING_RESOURCE_PATH.replace(":deliveryId", deliveryId)
                            .replace(":trackingSessionId", this.trackingSessionId),
                    new JsonObject()
            );
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that the tracking has been stopped")
    public void iShouldSeeAConfirmationThatTheTrackingHasBeenStopped() {
        assertThat(this.response.statusCode()).isEqualTo(200);
        SetupSteps.runDockerComposeDown();
    }
}
