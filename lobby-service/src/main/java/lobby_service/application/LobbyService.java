package lobby_service.application;

import common.hexagonal.InBoundPort;
import lobby_service.domain.Address;
import lobby_service.domain.DeliveryId;
import lobby_service.domain.UserId;

import java.util.Calendar;
import java.util.Optional;

/**
 * 
 * Interface of the Lobby Service at the application layer
 * 
 */
@InBoundPort
public interface LobbyService {

	String login(UserId userId, String password) throws LoginFailedException;
	
	DeliveryId createNewDelivery(String userSessionId, double weight, Address startingPlace, Address destinationPlace,
								 Optional<Calendar> expectedShippingMoment) throws CreateDeliveryFailedException;
	
	String trackDelivery(String userSessionId, DeliveryId deliveryId) throws TrackDeliveryFailedException;
    
}
