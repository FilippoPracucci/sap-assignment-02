package lobby_service.application;

import lobby_service.application.UserSessionNotFoundException;
import lobby_service.domain.UserId;

import java.util.HashMap;
import java.util.logging.Logger;

public class UserSessions {
    static Logger logger = Logger.getLogger("[UserSessionDB]");

    private static final String USER_SESSION_PREFIX = "user-session-";

    private final HashMap<String, UserSession> userSessions;

    public UserSessions() {
        this.userSessions = new HashMap<>();
    }

    public String createSession(final UserId userId) {
        final String sessionId = USER_SESSION_PREFIX + this.userSessions.size();
        this.userSessions.put(sessionId, new UserSession(sessionId, userId));
        return sessionId;
    }

    public boolean isPresent(final String userSessionId) {
        return this.userSessions.containsKey(userSessionId);
    }

    public UserSession getSession(final String userSessionId) throws UserSessionNotFoundException {
        if (!this.isPresent(userSessionId)) {
            throw new UserSessionNotFoundException();
        }
        return this.userSessions.get(userSessionId);
    }
}
