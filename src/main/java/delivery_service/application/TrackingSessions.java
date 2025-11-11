package delivery_service.application;

import common.ddd.Repository;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * 
 * Tracking sessions.
 * 
 */
public class TrackingSessions implements Repository {
	static Logger logger = Logger.getLogger("[TrackingSessionRepo]");

	private static final String TRACKING_SESSION_PREFIX = "tracking-session-";
	private final HashMap<String, TrackingSession> trackingSessions;

	public TrackingSessions() {
		trackingSessions = new HashMap<>();
	}
	
	public TrackingSession createSession() {
		final String id = TRACKING_SESSION_PREFIX + this.trackingSessions.size();
		final TrackingSession trackingSession = new TrackingSession(id);
		trackingSessions.put(id, trackingSession);
		return trackingSession;
	}

	public TrackingSession getTrackingSession(final String trackingSessionId) {
		return trackingSessions.get(trackingSessionId);
	}
	
}
