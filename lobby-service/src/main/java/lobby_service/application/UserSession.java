package main.java.lobby_service.application;

import lobby_service.domain.UserId;

/**
 * 
 * Representing a user session, created when a user logs in.
 * 
 */
public record UserSession(String sessionId, UserId userId) {

}
