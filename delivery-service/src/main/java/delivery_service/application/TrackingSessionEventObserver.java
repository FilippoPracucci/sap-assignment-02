package delivery_service.application;

import common.hexagonal.OutBoundPort;
import delivery_service.domain.DeliveryTime;

/**
 * 
 * This is an outbound port to notify events to users
 * tracking a delivery
 * 
 */
@OutBoundPort
public interface TrackingSessionEventObserver {

	/**
	 * 
	 * Enable the dispatching of events
	 * 
	 * This occurs when the observer is ready to receive the events
	 * 
	 * @param trackingSessionId the tracking session id
	 */
	void enableEventNotification(String trackingSessionId);

	/**
	 *
	 * Notify that the delivery has been shipped
	 *
	 * @param trackingSessionId the tracking session id
	 */
	void shipped(String trackingSessionId);

	/**
	 *
	 * Notify that the delivery has been delivered
	 *
	 * @param trackingSessionId the tracking session id
	 */
	void delivered(String trackingSessionId);

	/**
	 *
	 * Notify that time elapsed while delivering
	 *
	 * @param trackingSessionId the tracking session id
	 */
	void timeElapsed(String trackingSessionId, DeliveryTime timeElapsed);
}
