package delivery_service.domain;

import common.ddd.ValueObject;

import java.util.Objects;

public record DeliveryId(String id) implements ValueObject {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryId that = (DeliveryId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
