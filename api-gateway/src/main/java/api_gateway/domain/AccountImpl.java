package api_gateway.domain;

public record AccountImpl(UserId userId, String userName, String password, long whenCreated) implements Account {

    public AccountImpl(final UserId userId, final String userName, final String password) {
        this(userId, userName, password, 0);
    }

    @Override
    public String getUserName() {
        return this.userName;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public long getWhenCreated() {
        return this.whenCreated;
    }

    @Override
    public UserId getId() {
        return this.userId;
    }
}
