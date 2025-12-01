package account_service.domain;

public class AccountImpl implements Account {
    private final UserId userId;
    private final String userName;
    private final String password;
    private final long whenCreated;

    public AccountImpl(final UserId userId, final String userName, final String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.whenCreated = System.currentTimeMillis();
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
