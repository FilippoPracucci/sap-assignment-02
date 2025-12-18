import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Setup {

    private static final String METRICS_QUERY_URI = "http://localhost:9401/metrics?name[]=";
    protected final static String API_GATEWAY_URI = "http://localhost:8080";
    protected static final String API_VERSION = "v1";
    protected static final String HEALTH_CHECK_ENDPOINT = "/api/" + API_VERSION + "/health";

    @BeforeEach
    public void setup() {
        try {
            new ProcessBuilder("docker", "compose", "build")
                    .inheritIO()
                    .start()
                    .waitFor();
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
                statusCode = doGet(API_GATEWAY_URI + HEALTH_CHECK_ENDPOINT).statusCode();
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        } while (statusCode != 200);
        System.out.println("Docker compose up");
    }

    @AfterEach
    public void resetTestEnvironment() {
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

    protected HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> doGet(final String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected double getMetricValue(final String metricName) throws Exception {
        return Double.parseDouble(doGet(METRICS_QUERY_URI + metricName)
                .body()
                .lines()
                .toList()
                .getLast()
                .split(metricName)[1]
        );
    }
}
