package lobby_service.infrastructure;

import lobby_service.application.*;
import io.vertx.core.Vertx;
import lobby_service.infrastructure.AccountServiceProxy;
import lobby_service.infrastructure.DeliveryServiceProxy;
import lobby_service.infrastructure.LobbyServiceController;

/**
 * @author Bedeschi Federica   federica.bedeschi4@studio.unibo.it
 * @author Pracucci Filippo    filippo.pracucci@studio.unibo.it
 */

public class LobbyServiceMain {

	static final int LOBBY_SERVICE_PORT = 9001;
	static final String ACCOUNT_SERVICE_URI = "http://localhost:9000";
	static final String DELIVERY_SERVICE_URI = "http://localhost:9002";

	public static void main(String[] args) {
		
		final var lobby = new LobbyServiceImpl();
		final AccountService accountService =  new AccountServiceProxy(ACCOUNT_SERVICE_URI);
		final DeliveryService deliveryService =  new DeliveryServiceProxy(DELIVERY_SERVICE_URI);

		lobby.bindAccountService(accountService);
		lobby.bindDeliveryService(deliveryService);
		
		Vertx.vertx().deployVerticle(new LobbyServiceController(lobby, LOBBY_SERVICE_PORT));
	}

}

