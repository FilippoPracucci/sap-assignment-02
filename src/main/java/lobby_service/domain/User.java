package lobby_service.domain;

import common.ddd.Entity;

public interface User extends Entity<UserId> {

    String getUserName();

    String getPassword();
}
