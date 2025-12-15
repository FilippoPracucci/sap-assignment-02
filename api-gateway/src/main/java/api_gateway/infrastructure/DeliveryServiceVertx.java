package api_gateway.infrastructure;

import api_gateway.application.DeliveryService;
import io.vertx.core.Vertx;

public interface DeliveryServiceVertx extends DeliveryService {

    /**
     *
     * Create an event channel to receive delivery events, asynchronously
     *
     * @param trackingSessionId
     * @param vertx
     */
    void createAnEventChannel(String trackingSessionId, Vertx vertx);
}
