package api_gateway.infrastructure;

import io.vertx.core.Vertx;
import api_gateway.application.*;

/**
 *
 * API Gateway for the Shipping in the Air case study
 *
 */
public class APIGatewayMain {

    static final int BACKEND_PORT = 8080;

    /* addresses to be used when using a manual deployment */

    /*static final String ACCOUNT_SERVICE_ADDRESS = "http://localhost:9000";
    static final String LOBBY_SERVICE_ADDRESS = "http://localhost:9001";
    static final String DELIVERY_SERVICE_ADDRESS = "http://localhost:9002";

    static final String DELIVERY_SERVICE_WS_ADDRESS = "localhost";
    static final int DELIVERY_SERVICE_WS_PORT = 9002;*/

    /* addresses to be used when deploying with Docker */

    static final String ACCOUNT_SERVICE_ADDRESS = "http://account-service:9000";
    static final String LOBBY_SERVICE_ADDRESS = "http://lobby-service:9001";
    static final String DELIVERY_SERVICE_ADDRESS = "http://delivery-service:9002";

    static final String DELIVERY_SERVICE_WS_ADDRESS = "delivery-service";
    static final int DELIVERY_SERVICE_WS_PORT = 9002;

    public static void main(String[] args) {

        final AccountService accountService = new AccountServiceProxy(ACCOUNT_SERVICE_ADDRESS);
        final LobbyService lobbyService = new LobbyServiceProxy(LOBBY_SERVICE_ADDRESS);
        final DeliveryService deliveryService = new DeliveryServiceProxy(DELIVERY_SERVICE_ADDRESS,
                DELIVERY_SERVICE_WS_ADDRESS, DELIVERY_SERVICE_WS_PORT);

        var server = new APIGatewayController(accountService, lobbyService, deliveryService, BACKEND_PORT);
        Vertx.vertx().deployVerticle(server);
    }
}

