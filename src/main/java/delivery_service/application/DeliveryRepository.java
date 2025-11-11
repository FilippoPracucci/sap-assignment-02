package delivery_service.application;

import account_service.application.AccountAlreadyPresentException;
import account_service.application.InvalidAccountIdException;
import account_service.domain.UserId;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import delivery_service.domain.*;

/**
 * 
 * Delivery Repository
 * 
 */
@OutBoundPort
public interface DeliveryRepository extends Repository {

	DeliveryId getNextId();

	void addDelivery(Delivery delivery) throws InvalidDeliveryIdException, DeliveryAlreadyPresentException;;
	
	boolean isPresent(DeliveryId deliveryId);

	Delivery getDelivery(DeliveryId deliveryId) throws DeliveryNotFoundException;
}
