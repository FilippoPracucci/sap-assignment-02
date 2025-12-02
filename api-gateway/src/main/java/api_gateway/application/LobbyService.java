package api_gateway.application;

import api_gateway.domain.Address;
import api_gateway.domain.DeliveryId;
import api_gateway.domain.UserId;
import common.hexagonal.OutBoundPort;

import java.util.Calendar;
import java.util.Optional;

/**
 * 
 * Interface of the Lobby Service at the application layer
 * 
 */
@OutBoundPort
public interface LobbyService {

	String login(UserId userId, String password) throws LoginFailedException, ServiceNotAvailableException;
	
	DeliveryId createNewDelivery(String userSessionId, double weight, Address startingPlace, Address destinationPlace,
								 Optional<Calendar> expectedShippingMoment)
			throws CreateDeliveryFailedException, ServiceNotAvailableException;
	
	String trackDelivery(String userSessionId, DeliveryId deliveryId) throws TrackDeliveryFailedException,
			ServiceNotAvailableException;
    
}
