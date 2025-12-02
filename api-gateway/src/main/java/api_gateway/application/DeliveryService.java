package api_gateway.application;

import api_gateway.domain.DeliveryDetail;
import api_gateway.domain.DeliveryId;
import api_gateway.domain.DeliveryStatus;
import common.hexagonal.OutBoundPort;
import io.vertx.core.Vertx;

/**
 * 
 * Interface of the Delivery Service at the application layer
 * 
 */
@OutBoundPort
public interface DeliveryService {

	/**
     * 
     * Get delivery detail.
     * 
     * @param deliveryId
     * @return
     * @throws DeliveryNotFoundException
     */
	DeliveryDetail getDeliveryDetail(DeliveryId deliveryId) throws DeliveryNotFoundException,
			ServiceNotAvailableException;

	/**
	 *
	 * Get delivery detail.
	 *
	 * @param deliveryId
	 * @return
	 * @throws DeliveryNotFoundException
	 * @throws TrackingSessionNotFoundException
	 */
	DeliveryStatus getDeliveryStatus(DeliveryId deliveryId, String trackingSessionId)
			throws DeliveryNotFoundException, TrackingSessionNotFoundException, ServiceNotAvailableException;

	/**
	 *
	 * Stop tracking a delivery -- called by a UserSession (logged in user), delete the TrackingSession with the
	 * trackingSessionId given
	 *
	 * @param deliveryId -- id of the delivery to be tracked
	 * @param trackingSessionId -- id of the TrackingSession to be deleted
	 * @throws DeliveryNotFoundException
	 * @throws TrackingSessionNotFoundException
	 */
	void stopTrackingDelivery(DeliveryId deliveryId, String trackingSessionId) throws DeliveryNotFoundException,
			TrackingSessionNotFoundException, ServiceNotAvailableException;

	/**
	 *
	 * Create an event channel to receive delivery events, asynchronously
	 *
	 * @param trackingSessionId
	 * @param vertx
	 */
	void createAnEventChannel(String trackingSessionId, Vertx vertx);
}