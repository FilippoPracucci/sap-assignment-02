package delivery_service.application;

import delivery_service.domain.*;

import java.util.logging.Logger;

/**
 * 
 * Representing a tracking session.
 * 
 * - Created when a logged user tracks a delivery.
 * - It acts as observer of events generated while tracking.
 * 
 */
public class TrackingSession implements DeliveryObserver {

	static Logger logger = Logger.getLogger("[Tracking Session]");
	private final String trackingSessionId;
	private TrackingSessionEventObserver trackingSessionEventNotifier;
	
	public TrackingSession(final String trackingSessionId) {
		this.trackingSessionId = trackingSessionId;
	}
	
	public String getId() {
		return this.trackingSessionId;
	}

	public void notifyDeliveryEvent(final DeliveryEvent ev) {
		if (ev instanceof Shipped) {
			trackingSessionEventNotifier.shipped(trackingSessionId);
		} else if (ev instanceof Delivered) {
			trackingSessionEventNotifier.delivered(trackingSessionId);
		}
	}
		
	public void bindTrackingSessionEventNotifier(final TrackingSessionEventObserver trackingSessionEventNotifier) {
		this.trackingSessionEventNotifier = trackingSessionEventNotifier;
	}
	
	public TrackingSessionEventObserver getTrackingSessionEventNotifier() {
		return this.trackingSessionEventNotifier;
	}
	
	private void log(String msg) {
		//System.out.println("[ player " + userId.id() + " in game " + game.getId() + " ] " + msg);
	}
}
