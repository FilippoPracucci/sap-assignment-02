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
		this.trackingSessions = new HashMap<>();
	}
	
	public TrackingSession createSession() {
		final String id = TRACKING_SESSION_PREFIX + this.trackingSessions.size();
		final TrackingSession trackingSession = new TrackingSession(id);
		this.trackingSessions.put(id, trackingSession);
		return trackingSession;
	}

	public void removeSession(final String trackingSessionId) throws TrackingSessionNotFoundException {
		if (this.trackingSessions.remove(trackingSessionId) == null) {
			throw new TrackingSessionNotFoundException();
		}
	}

	public TrackingSession getSession(final String trackingSessionId) throws TrackingSessionNotFoundException {
		if (!this.isPresent(trackingSessionId)) {
			throw new TrackingSessionNotFoundException();
		}
		return this.trackingSessions.get(trackingSessionId);
	}

	public boolean isPresent(final String trackingSessionId) {
		return this.trackingSessions.get(trackingSessionId) != null;
	}
	
}
