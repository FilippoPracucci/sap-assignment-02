package account_service.domain;

import common.ddd.Entity;

public interface Account extends Entity<UserId> {

    String getUserName();

    String getPassword();

    // void updatePassword(String password);

    long getWhenCreated();
}
