package delivery_service.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import delivery_service.domain.*;

import java.util.Collection;

/**
 * 
 * Delivery Repository
 * 
 */
@OutBoundPort
public interface DeliveryRepository extends Repository {

	DeliveryId getNextId();

	void addDelivery(Delivery delivery) throws InvalidDeliveryIdException, DeliveryAlreadyPresentException;

    boolean isPresent(DeliveryId deliveryId);

	Delivery getDelivery(DeliveryId deliveryId) throws DeliveryNotFoundException;

	void updateDeliveryState(DeliveryId deliveryId, DeliveryState deliveryState) throws DeliveryNotFoundException;

	Collection<Delivery> getAllDeliveries();
}
