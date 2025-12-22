package delivery_service.application;

import common.hexagonal.InBoundPort;
import delivery_service.domain.*;

import java.util.Calendar;
import java.util.Optional;

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
     * @param deliveryId the delivery id
     * @return the delivery details
     * @throws DeliveryNotFoundException
     */
	DeliveryDetail getDeliveryDetail(DeliveryId deliveryId) throws DeliveryNotFoundException;

	/**
	 *
	 * Get delivery detail.
	 *
	 * @param deliveryId the delivery id
	 * @return the delivery status
	 * @throws DeliveryNotFoundException
	 * @throws TrackingSessionNotFoundException
	 */
	DeliveryStatus getDeliveryStatus(DeliveryId deliveryId, String trackingSessionId)
			throws DeliveryNotFoundException, TrackingSessionNotFoundException;
		
	/**
	 * 
	 * Retrieve an existing tracking session.
	 * 
	 * @param trackingSessionId the tracking session id
	 * @return the tracking session
	 */
	TrackingSession getTrackingSession(String trackingSessionId) throws TrackingSessionNotFoundException;
	
	/**
	 * 
	 * Create a delivery -- called by a UserSession (logged-in user)
     *
	 * @return the id of the delivery
	 */
	DeliveryId createNewDelivery(double weight,
								 Address startingPlace,
								 Address destinationPlace,
								 Optional<Calendar> expectedShippingMoment);
	
	/**
	 * 
	 * Track a delivery -- called by a UserSession (logged-in user), creates a new TrackingSession
	 *
	 * @param deliveryId -- id of the delivery to be tracked
	 * @param observer -- observer of the events
	 * @return the tracking session
	 * @throws DeliveryNotFoundException
	 */
	TrackingSession trackDelivery(DeliveryId deliveryId, TrackingSessionEventObserver observer)
			throws DeliveryNotFoundException;

	/**
	 *
	 * Stop tracking a delivery -- called by a UserSession (logged-in user), delete the TrackingSession with the
	 * trackingSessionId given
	 *
	 * @param deliveryId -- id of the delivery to be tracked
	 * @param trackingSessionId -- id of the TrackingSession to be deleted
	 * @throws DeliveryNotFoundException
	 * @throws TrackingSessionNotFoundException
	 */
	void stopTrackingDelivery(DeliveryId deliveryId, String trackingSessionId) throws DeliveryNotFoundException,
			TrackingSessionNotFoundException;
}