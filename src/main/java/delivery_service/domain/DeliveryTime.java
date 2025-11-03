package delivery_service.domain;

import common.ddd.ValueObject;

import java.util.Objects;

public record DeliveryTime(int days) implements ValueObject {

    public DeliveryTime add(final DeliveryTime time) {
        return new DeliveryTime(this.days + time.days);
    }

    public DeliveryTime sub(final DeliveryTime time) {
        return new DeliveryTime(Math.max(this.days - time.days, 0));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryTime that = (DeliveryTime) o;
        return Objects.equals(days, that.days);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(days);
    }
}
