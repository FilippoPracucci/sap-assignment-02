package delivery_service;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackDeliverySteps {

    private static final String DELIVERY_RESOURCE_PATH = "/:deliveryId";
    private static final String TRACK_RESOURCE_PATH =  DELIVERY_RESOURCE_PATH + "/track";
    private static final String TRACKING_RESOURCE_PATH = DELIVERY_RESOURCE_PATH + "/:trackingSessionId";

    private HttpResponse<String> response;
    private String trackingSessionId = "";
    private String deliveryId = "";

    /* Scenario: Successful delivery tracking */

    @Given("I am on delivery tracking page")
    public void iAmOnDeliveryTrackingPage() {
    }

    @And("I have created a delivery with id {string}")
    public void iHaveCreatedADeliveryWithId(final String deliveryId) {
        this.deliveryId = deliveryId;
    }

    @When("I track the delivery with id {string}")
    public void iTrackTheDeliveryWithId(final String deliveryId) {
        try {
            this.response = SetupSteps.doPost(
                    SetupSteps.DELIVERIES_RESOURCE_PATH + TRACK_RESOURCE_PATH.replace(":deliveryId", deliveryId),
                    new JsonObject()
            );
            final JsonObject responseBody = new JsonObject(this.response.body());
            this.trackingSessionId = responseBody.containsKey("trackingSessionId")
                    ? responseBody.getString("trackingSessionId") : "";
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void getDeliveryStatus() {
        try {
            this.response = this.doGet(SetupSteps.DELIVERIES_RESOURCE_PATH
                    + TRACKING_RESOURCE_PATH.replace(":deliveryId", this.deliveryId)
                    .replace(":trackingSessionId", this.trackingSessionId)
            );
            System.out.println(this.response.body());
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see the delivery state and the time left")
    public void iShouldSeeTheDeliveryStateAndTheTimeLeft() {
        this.getDeliveryStatus();
        assertTrue(new JsonObject(this.response.body()).containsKey("deliveryStatus"));
    }

    /* Scenario: Delivery tracking fails with invalid id */

    @And("I have not a delivery with id {string} in my list")
    public void iHaveNotADeliveryWithIdInMyList(final String deliveryId) {
        this.deliveryId = deliveryId;
    }

    @Then("I should see an error {string}")
    public void iShouldSeeAnError(final String message) {
        assertThat(new JsonObject(this.response.body()).getString("error")).isEqualTo(message);
    }

    @And("I should not see the delivery state and the time left")
    public void iShouldNotSeeTheDeliveryStateAndTheTimeLeft() {
        this.getDeliveryStatus();
        assertFalse(new JsonObject(this.response.body()).containsKey("deliveryStatus"));
    }

    private HttpResponse<String> doGet(final String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
