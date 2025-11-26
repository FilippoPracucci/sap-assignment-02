package delivery_service.application;

import delivery_service.domain.*;

/**
 * 
 * Representing a tracking session.
 * 
 * - Created when a logged user tracks a delivery.
 * - It acts as observer of events generated while tracking.
 * 
 */
public class TrackingSession implements DeliveryObserver {

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
			this.trackingSessionEventNotifier.shipped(this.trackingSessionId);
		} else if (ev instanceof TimeElapsed) {
			this.trackingSessionEventNotifier.timeElapsed(this.trackingSessionId, ((TimeElapsed) ev).time());
		} else if (ev instanceof Delivered) {
			this.trackingSessionEventNotifier.delivered(this.trackingSessionId);
		}
	}
		
	public void bindTrackingSessionEventNotifier(final TrackingSessionEventObserver trackingSessionEventNotifier) {
		this.trackingSessionEventNotifier = trackingSessionEventNotifier;
	}
	
	public TrackingSessionEventObserver getTrackingSessionEventNotifier() {
		return this.trackingSessionEventNotifier;
	}
}
