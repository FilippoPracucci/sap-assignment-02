package lobby_service.domain;

public interface User extends Entity<UserId> {

    String getUserName();

    String getPassword();
}
