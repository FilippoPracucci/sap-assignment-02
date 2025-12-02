package api_gateway.domain;

import common.ddd.Entity;

public interface Account extends Entity<UserId> {

    String getUserName();

    String getPassword();

    long getWhenCreated();
}
