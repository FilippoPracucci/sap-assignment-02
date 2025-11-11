package delivery_service.application;

import common.hexagonal.InBoundPort;
import delivery_service.domain.*;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * Interface of the Delivery Service at the application layer
 * 
 */
@InBoundPort
public interface DeliveryService {

	/**
     * 
     * Get delivery detail.
     * 
     * @param deliveryId
     * @return
     * @throws DeliveryNotFoundException
     */
	Delivery getDeliveryDetail(DeliveryId deliveryId) throws DeliveryNotFoundException;
		
	/**
	 * 
	 * Retrieve an existing tracking session.
	 * 
	 * @param trackingSessionId
	 * @return
	 */
	TrackingSession getTrackingSession(String trackingSessionId);
	
	/**
	 * 
	 * Create a delivery -- called by a UserSession (logged in user)
     *
	 * @return the id of the delivery
	 */
	DeliveryId createNewDelivery(double weight,
								 Address startingPlace,
								 Address destinationPlace,
								 Calendar expectedShippingDate);
	
	/**
	 * 
	 * Track a delivery -- called by a UserSession (logged in user), creates a new TrackingSession
	 *
	 * @param deliveryId -- id of the delivery to be tracked
	 * @param observer -- observer of the events
	 * @return the tracking session
	 * @throws InvalidTrackingException
	 */
	TrackingSession trackDelivery(DeliveryId deliveryId, TrackingSessionEventObserver observer)
			throws InvalidTrackingException;
    
}