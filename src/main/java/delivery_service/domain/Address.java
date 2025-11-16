package delivery_service.domain;

import common.ddd.ValueObject;

public record Address(String street, int number) implements ValueObject {
}
