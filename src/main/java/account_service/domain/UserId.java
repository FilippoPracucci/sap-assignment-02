package account_service.domain;

import common.ddd.ValueObject;

public record UserId(String id) implements ValueObject {
}
