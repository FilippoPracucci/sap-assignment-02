import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CircuitBreakerTest extends Setup {

    private static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts";

    @Test
    public void testCircuitBreaker() {
        final int nSuccessfulConcurrentRequests = 5;
        final int nFailedConcurrentRequests = nSuccessfulConcurrentRequests + 2;
        try {
            this.createAndWaitThreads(nSuccessfulConcurrentRequests);
            assertFalse(this.isCircuitOpen());
            this.disconnectNetworkFromAccountService();
            this.createAndWaitThreads(nFailedConcurrentRequests);
            assertTrue(this.isCircuitOpen());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void createAndWaitThreads(final int nConcurrentRequests) {
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < nConcurrentRequests; i++) {
            threads.add(this.createThreadDoingPostRequest());
        }
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    private Thread createThreadDoingPostRequest() {
        return Thread.ofVirtual().start(() -> {
            try {
                doPost(API_GATEWAY_URI + ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                        "userName", "name",
                        "password", "1234"))
                );
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    private void disconnectNetworkFromAccountService() throws Exception {
        new ProcessBuilder("docker", "network", "disconnect",
                "shipping-on-the-air_shipping_on_the_air_network", "account-service-01")
                .inheritIO()
                .start()
                .waitFor();
    }

    private boolean isCircuitOpen() throws Exception {
        return ((int) this.getMetricValue("api_gateway_is_account_circuit_open")) == 1;
    }
}
