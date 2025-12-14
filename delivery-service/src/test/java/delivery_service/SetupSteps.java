package delivery_service;

import delivery_service.application.DeliveryEventStore;
import delivery_service.application.DeliveryServiceImpl;
import delivery_service.infrastructure.DeliveryServiceController;
import delivery_service.infrastructure.FileBasedDeliveryEventStore;
import delivery_service.infrastructure.Synchronizer;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SetupSteps {

    protected static final String DELIVERY_EVENT_STORE_FILE_NAME = "test_delivery_event_store.json";
    protected static final int DELIVERY_SERVICE_PORT = 9002;
    protected static final String DELIVERY_ENDPOINT = "http://localhost:" + DELIVERY_SERVICE_PORT + "/api/v1";
    protected static final String DELIVERIES_RESOURCE_PATH = DELIVERY_ENDPOINT + "/deliveries";

    @BeforeAll
    public static void setUp() {
        final DeliveryServiceImpl deliveryService = new DeliveryServiceImpl();
        final DeliveryEventStore deliveryEventStore = new FileBasedDeliveryEventStore(DELIVERY_EVENT_STORE_FILE_NAME);
        deliveryService.bindDeliveryRepository(deliveryEventStore);
        final DeliveryServiceController controller = new DeliveryServiceController(deliveryService,
                DELIVERY_SERVICE_PORT);
        final Synchronizer sync = new Synchronizer();
        Vertx.vertx()
                .deployVerticle(controller)
                .onSuccess((res) -> sync.notifySync());
        try {
            sync.awaitSync();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        //registerAccount("userName", "Secret#123");
    }

    /*private static void registerAccount(final String userName, final String password) {
        try {
            doPost(DELIVERIES_RESOURCE_PATH, new JsonObject(Map.of(
                    "userName", userName,
                    "password", password)
            ));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }*/

    protected static HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @AfterAll
    public static void resetTestEnvironment() {
        if (!new File(System.getProperty("user.dir") + File.separator + DELIVERY_EVENT_STORE_FILE_NAME).delete()) {
            Assertions.fail("Event store file not deleted");
        }
    }
}
