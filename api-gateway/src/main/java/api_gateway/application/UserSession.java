package api_gateway.application;

import api_gateway.domain.UserId;

/**
 * 
 * Representing a user session, created when a user logs in.
 * 
 */
public record UserSession(String sessionId, UserId userId) {

}
