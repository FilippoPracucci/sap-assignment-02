package delivery_service.domain;

import common.ddd.ValueObject;

import java.util.Objects;

public record Address(String street, int number) implements ValueObject {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return number == address.number && Objects.equals(street, address.street);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, number);
    }
}
