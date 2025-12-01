package main.java.delivery_service.domain;

import common.ddd.ValueObject;

public record DeliveryTime(int days, int hours) implements ValueObject {

    public DeliveryTime add(final DeliveryTime time) {
        final int hoursAdded = this.hours + time.hours;
        return new DeliveryTime(this.days + time.days + (hoursAdded / 24), hoursAdded % 24);
    }

    public DeliveryTime sub(final DeliveryTime time) {
        final int hoursSubtracted = this.hours - time.hours;
        final int newDays = Math.max(this.days - time.days - (hoursSubtracted < 0 ? 1 : 0), 0);
        return new DeliveryTime(newDays, (newDays == 0 && hoursSubtracted < 0) ? 0 : (hoursSubtracted + 24) % 24);
    }

    @Override
    public String toString() {
        return this.days + " days" + " and " + this.hours + " hours";
    }
}
