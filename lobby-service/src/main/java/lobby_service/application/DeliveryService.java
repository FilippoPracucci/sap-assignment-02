package main.java.lobby_service.application;

import common.hexagonal.OutBoundPort;
import delivery_service.domain.Address;
import lobby_service.application.CreateDeliveryFailedException;
import lobby_service.application.ServiceNotAvailableException;
import lobby_service.application.TrackDeliveryFailedException;
import lobby_service.domain.*;

import java.util.Calendar;
import java.util.Optional;

@OutBoundPort
public interface DeliveryService {

	DeliveryId createNewDelivery(double weight, Address startingPlace,Address destinationPlace,
								 Optional<Calendar> expectedShippingMoment)
			throws CreateDeliveryFailedException, ServiceNotAvailableException;
	
	String trackDelivery(DeliveryId deliveryId) throws TrackDeliveryFailedException, ServiceNotAvailableException;
	    
}
