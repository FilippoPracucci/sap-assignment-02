package delivery_service.domain;

import common.ddd.ValueObject;

public record DeliveryTime(int days, int hours) implements ValueObject {

    private static final int HOURS_IN_DAY = 24;

    public DeliveryTime add(final DeliveryTime time) {
        final int hoursAdded = this.hours + time.hours;
        return new DeliveryTime(this.days + time.days + (hoursAdded / HOURS_IN_DAY), hoursAdded % HOURS_IN_DAY);
    }

    public DeliveryTime sub(final DeliveryTime time) {
        final int hoursSubtracted = this.hours - time.hours;
        final int newDays = Math.max(this.days - time.days - (hoursSubtracted < 0 ? 1 : 0), 0);
        return new DeliveryTime(
                newDays,
                (newDays == 0 && hoursSubtracted < 0) ? 0 : (hoursSubtracted + HOURS_IN_DAY) % HOURS_IN_DAY
        );
    }

    public int toHours() {
        return this.days * HOURS_IN_DAY + this.hours;
    }

    @Override
    public String toString() {
        return this.days + " days" + " and " + this.hours + " hours";
    }
}
