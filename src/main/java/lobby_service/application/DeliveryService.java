package lobby_service.application;

import common.hexagonal.OutBoundPort;
import delivery_service.domain.Address;
import lobby_service.domain.*;

import java.util.Calendar;

@OutBoundPort
public interface DeliveryService {

	DeliveryId createNewDelivery(double weight, Address startingPlace, Address destinationPlace, Calendar targetTime)
			throws CreateDeliveryFailedException, ServiceNotAvailableException;
	
	String trackDelivery(DeliveryId deliveryId) throws TrackDeliveryFailedException, ServiceNotAvailableException;
	    
}
