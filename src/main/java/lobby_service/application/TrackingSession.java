package lobby_service.application;

import common.ddd.ValueObject;
import lobby_service.domain.DeliveryId;

/**
 * 
 * Representing a tracking session, created when a user logs in.
 * 
 */
public record TrackingSession(String sessionId, String userSessionId, DeliveryId deliveryId) implements ValueObject {
}
