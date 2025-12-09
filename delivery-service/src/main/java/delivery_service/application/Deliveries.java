package delivery_service.application;

import common.ddd.Repository;
import delivery_service.domain.Delivery;
import delivery_service.domain.DeliveryId;

import java.util.HashMap;
import java.util.logging.Logger;

public class Deliveries implements Repository {
	static Logger logger = Logger.getLogger("[DeliveriesRepo]");

	private final HashMap<DeliveryId, Delivery> deliveries;

	public Deliveries() {
		this.deliveries = new HashMap<>();
	}

	public void addDelivery(final Delivery delivery) {
		this.deliveries.put(delivery.getId(), delivery);
	}

	public Delivery getDelivery(final DeliveryId deliveryId) throws DeliveryNotFoundException {
		if (!this.isPresent(deliveryId)) {
			throw new DeliveryNotFoundException();
		}
		return this.deliveries.get(deliveryId);
	}

	public boolean isPresent(final DeliveryId deliveryId) {
		return this.deliveries.get(deliveryId) != null;
	}
}
