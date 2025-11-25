package lobby_service.application;

import common.ddd.Entity;
import lobby_service.domain.UserId;

/**
 * 
 * Representing a user session, created when a user logs in.
 * 
 */
public record UserSession(String sessionId, UserId userId) implements Entity<String> {

    @Override
    public String getId() {
        return this.sessionId;
    }
}
