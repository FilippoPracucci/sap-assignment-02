import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceTest {

    private final static String API_GATEWAY_URI = "http://localhost:8080";
    protected static final String API_VERSION = "v1";
    protected static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts";
    static final String HEALTH_CHECK_ENDPOINT = "/api/" + API_VERSION + "/health";

    private static final String METRICS_QUERY_URI = "http://localhost:9401/metrics?name[]=";

    @BeforeAll
    public static void setup() {
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

    @Test
    public void testAvgResponseTime() {
        final int nConcurrentRequests = 1000;
        final int responseTimeThresholdInMs = 100;
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < nConcurrentRequests; i++) {
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    doPost(API_GATEWAY_URI + ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                            "userName", "name",
                            "password", "1234"))
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        try {
            threads.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            double nRequests = this.getMetricValue("api_gateway_num_rest_requests_total");
            double totalResponseTimeInMs = this.getMetricValue("api_gateway_request_response_time_ms_total");
            double averageResponseTimeInMs = (totalResponseTimeInMs / nRequests);
            assertEquals(nConcurrentRequests, nRequests);
            assertTrue(averageResponseTimeInMs <= responseTimeThresholdInMs);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @AfterAll
    public static void resetTestEnvironment() {
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

    private double getMetricValue(final String metricName) throws Exception {
        return Double.parseDouble(doGet(METRICS_QUERY_URI + metricName)
                .body()
                .lines()
                .toList()
                .getLast()
                .split(metricName)[1]
        );
    }

    private static HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> doGet(final String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
