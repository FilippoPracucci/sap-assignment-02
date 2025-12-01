package delivery_service.infrastructure;

import delivery_service.application.DeliveryServiceImpl;
import delivery_service.infrastructure.DeliveryServiceController;
import delivery_service.infrastructure.FileBasedDeliveryRepository;
import io.vertx.core.Vertx;

/**
 * @author Bedeschi Federica   federica.bedeschi4@studio.unibo.it
 * @author Pracucci Filippo    filippo.pracucci@studio.unibo.it
 */

public class DeliveryServiceMain {

	static final int DELIVERY_SERVICE_PORT = 9002;

	public static void main(String[] args) {
		final var deliveryService = new DeliveryServiceImpl();
		deliveryService.bindDeliveryRepository(new FileBasedDeliveryRepository());
		Vertx.vertx().deployVerticle(new DeliveryServiceController(deliveryService, DELIVERY_SERVICE_PORT));
	}

}

