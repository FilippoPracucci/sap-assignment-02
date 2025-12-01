package lobby_service.application;

import common.hexagonal.OutBoundPort;
<<<<<<< HEAD:lobby-service/src/main/java/lobby_service/application/DeliveryService.java
import lobby_service.domain.Address;
=======
>>>>>>> 466e5073d17d5611ffb01d89e76984338036dad5:src/main/java/lobby_service/application/DeliveryService.java
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
