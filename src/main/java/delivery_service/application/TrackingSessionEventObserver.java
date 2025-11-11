package delivery_service.application;

import common.hexagonal.OutBoundPort;

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
	 * @param trackingSessionId
	 */
	void enableEventNotification(String trackingSessionId);

	/**
	 *
	 * Notify that the delivery has been shipped
	 *
	 * @param trackingSessionId
	 */
	void shipped(String trackingSessionId);

	/**
	 *
	 * Notify that the delivery has been delivered
	 *
	 * @param trackingSessionId
	 */
	void delivered(String trackingSessionId);
}
