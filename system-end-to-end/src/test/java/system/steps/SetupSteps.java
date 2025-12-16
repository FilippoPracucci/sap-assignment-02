package system.steps;

import io.cucumber.java.BeforeAll;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SetupSteps {

    private final static String API_GATEWAY_URI = "http://localhost:8080";

    /* for account */
    protected static final String API_VERSION = "v1";
    protected static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts";
    protected static final String ACCOUNT_RESOURCE_PATH = ACCOUNTS_RESOURCE_PATH + "/:accountId";

    /* for lobby */
    protected static final String LOGIN_RESOURCE_PATH = ACCOUNT_RESOURCE_PATH + "/login";
    protected static final String USER_SESSIONS_RESOURCE_PATH = "/api/" + API_VERSION + "/user-sessions";
    protected static final String CREATE_DELIVERY_RESOURCE_PATH = USER_SESSIONS_RESOURCE_PATH + "/:sessionId/create-delivery";
    protected static final String TRACK_DELIVERY_RESOURCE_PATH = USER_SESSIONS_RESOURCE_PATH + "/:sessionId/track-delivery";

    /* for delivery */
    protected static final String DELIVERIES_RESOURCE_PATH = "/api/" + API_VERSION + "/deliveries";
    protected static final String DELIVERY_RESOURCE_PATH =  DELIVERIES_RESOURCE_PATH +   "/:deliveryId";
    protected static final String TRACKING_RESOURCE_PATH = DELIVERY_RESOURCE_PATH + "/:trackingSessionId";
    protected static final String STOP_TRACKING_RESOURCE_PATH = TRACKING_RESOURCE_PATH + "/stop";

    /* Health check endpoint */
    static final String HEALTH_CHECK_ENDPOINT = "/api/" + API_VERSION + "/health";

    @BeforeAll
    public static void setUp() {
		try {
            new ProcessBuilder("docker", "compose", "build")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected static HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_GATEWAY_URI + uri))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected static HttpResponse<String> doGet(final String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_GATEWAY_URI + uri)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected static JsonObject toJson(final String weight, final String startingPlace, final String destinationPlace) {
        final JsonObject obj = new JsonObject();
        obj.put("weight", Double.parseDouble(weight));
        final String[] startingStreet = startingPlace.split(", ");
        obj.put("startingPlace", new JsonObject(Map.of(
                "street", startingStreet[0],
                "number", Integer.parseInt(startingStreet[1]))
        ));
        final String[] destinationStreet = destinationPlace.split(", ");
        obj.put("destinationPlace", new JsonObject(Map.of(
                "street", destinationStreet[0],
                "number", Integer.parseInt(destinationStreet[1]))
        ));
        return obj;
    }

    protected static void runDockerComposeUp() {
        try {
            new ProcessBuilder("docker", "compose", "up", "--detach")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // give time docker compose up do start containers
        int statusCode = 0;
        do {
            try {
                statusCode = SetupSteps.doGet(SetupSteps.HEALTH_CHECK_ENDPOINT).statusCode();
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        } while (statusCode != 200);
        System.out.println("Docker compose up");
    }

    protected static void runDockerComposeDown() {
        try {
            new ProcessBuilder("docker", "compose", "down")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Docker compose down");
    }
}
