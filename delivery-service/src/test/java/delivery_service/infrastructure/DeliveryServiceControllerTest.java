package delivery_service.infrastructure;

import delivery_service.application.DeliveryService;
import delivery_service.application.DeliveryServiceMock;
import delivery_service.domain.Address;
import delivery_service.domain.DeliveryDetailImpl;
import delivery_service.domain.DeliveryId;
import delivery_service.domain.TimeConverter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DeliveryServiceControllerTest {

    private static final int SUCCESS_STATUS_CODE = 200;
    private static final String STATUS_CODE_ERROR_MESSAGE = "Status code should be 200";
    private DeliveryServiceController deliveryServiceController;
    private Vertx vertx;

    private static final int DELIVERY_SERVICE_PORT = 9002;
    private static final String DELIVERY_SERVICE_ADDRESS = "http://localhost:" + DELIVERY_SERVICE_PORT + "/api/v1";
    private static final String DELIVERIES_RESOURCE_PATH = DELIVERY_SERVICE_ADDRESS + "/deliveries";
    private static final String DELIVERY_RESOURCE_PATH =  DELIVERIES_RESOURCE_PATH +   "/:deliveryId";
    private static final String TRACK_RESOURCE_PATH =  DELIVERY_RESOURCE_PATH +   "/track";
    private static final String TRACKING_RESOURCE_PATH = DELIVERY_RESOURCE_PATH + "/:trackingSessionId";
    private static final String STOP_TRACKING_RESOURCE_PATH = TRACKING_RESOURCE_PATH + "/stop";
    private static final String HEALTH_CHECK_ENDPOINT = DELIVERY_SERVICE_ADDRESS + "/health";

    @BeforeEach
    public void setUp() {
        final Synchronizer sync = new Synchronizer();
        this.vertx = Vertx.vertx();
        final DeliveryService deliveryService = new DeliveryServiceMock();
        this.deliveryServiceController = new DeliveryServiceController(deliveryService, DELIVERY_SERVICE_PORT);
        this.vertx
                .deployVerticle(this.deliveryServiceController)
                .onSuccess((res) -> sync.notifySync());
        try {
            sync.awaitSync();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testDeliveryCreation() {
        try {
            final JsonObject body = DeliveryJsonConverter.toJson(
                    new DeliveryDetailImpl(
                            new DeliveryId("delivery-0"),
                            10.0,
                            new Address("Via Emilia", 1),
                            new Address("Via Roma", 20),
                            TimeConverter.getNowAsCalendar()
                    )
            );
            body.remove("deliveryId");
            body.remove("expectedShippingMoment");
            final HttpResponse<String> res = doPost(DELIVERIES_RESOURCE_PATH, body);
            assertEquals(SUCCESS_STATUS_CODE, res.statusCode(), STATUS_CODE_ERROR_MESSAGE);
            assertEquals("delivery-0", new JsonObject(res.body()).getString("deliveryId"));
        } catch (Exception ex) {
            fail("Delivery creation failed.");
        }
    }

    @Test
    public void testGetDeliveryDetail() {
        try {
            final HttpResponse<String> res = doGet(
                    DELIVERY_RESOURCE_PATH.replace(":deliveryId", "delivery-0")
            );
            assertEquals(SUCCESS_STATUS_CODE, res.statusCode(), STATUS_CODE_ERROR_MESSAGE);
        } catch (Exception ex) {
            fail("Get delivery detail failed.");
        }
    }

    @Test
    public void testTrackDelivery() {
        try {
            final HttpResponse<String> res = doPost(
                    TRACK_RESOURCE_PATH.replace(":deliveryId", "delivery-0"),
                    new JsonObject()
            );
            assertEquals(SUCCESS_STATUS_CODE, res.statusCode(), STATUS_CODE_ERROR_MESSAGE);
            assertEquals("tracking-session-0", new JsonObject(res.body()).getString("trackingSessionId"));
        } catch (Exception ex) {
            fail("Track delivery failed.");
        }
    }

    @Test
    public void testStopTrackingDelivery() {
        try {
            final HttpResponse<String> res = doPost(
                    STOP_TRACKING_RESOURCE_PATH
                            .replace(":deliveryId", "delivery-0")
                            .replace(":trackingSessionId", "tracking-session-0"),
                    new JsonObject()
            );
            assertEquals(SUCCESS_STATUS_CODE, res.statusCode(), STATUS_CODE_ERROR_MESSAGE);
        } catch (Exception ex) {
            fail("Stop tracking delivery failed.");
        }
    }

    @Test
    public void testGetDeliveryStatus() {
        try {
            final HttpResponse<String> res = doGet(TRACKING_RESOURCE_PATH
                    .replace(":deliveryId", "delivery-0")
                    .replace("trackingSessionId", "tracking-session-0")
            );
            assertEquals(SUCCESS_STATUS_CODE, res.statusCode(), STATUS_CODE_ERROR_MESSAGE);
        } catch (Exception ex) {
            fail("Get delivery status failed.");
        }
    }

    @Test
    public void testHealthCheck() {
        try {
            final HttpResponse<String> res = doGet(HEALTH_CHECK_ENDPOINT);
            assertEquals(SUCCESS_STATUS_CODE, res.statusCode(), STATUS_CODE_ERROR_MESSAGE);
            assertEquals("UP", new JsonObject(res.body()).getString("status"));
        } catch (Exception ex) {
            fail("Health check failed.");
        }
    }

    @AfterEach
    public void tearDown() {
        this.vertx.undeploy(this.deliveryServiceController.deploymentID());
    }

    private HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        return this.doRequest(uri, true, Optional.of(body));
    }

    private HttpResponse<String> doGet(final String uri) throws Exception {
        return this.doRequest(uri, false, Optional.empty());
    }

    private HttpResponse<String> doRequest(final String uri, final boolean isPost, final Optional<JsonObject> body)
            throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        if (isPost) {
            request = HttpRequest.newBuilder().uri(URI.create(uri))
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(body.orElse(new JsonObject()).toString()))
                    .build();
        } else {
            request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        }
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
