package lobby_service.application;

import common.hexagonal.InBoundPort;
import delivery_service.domain.Address;
import lobby_service.domain.DeliveryId;

import java.util.Calendar;
import java.util.Optional;

/**
 * 
 * Interface of the Lobby Service at the application layer
 * 
 */
@InBoundPort
public interface LobbyService {

	String login(String userName, String password) throws LoginFailedException;
	
	DeliveryId createNewDelivery(String userSessionId, double weight, Address startingPlace, Address destinationPlace,
								 Optional<Calendar> expectedShippingMoment) throws CreateDeliveryFailedException;
	
	String trackDelivery(String userSessionId, DeliveryId deliveryId) throws TrackDeliveryFailedException;
    
}
