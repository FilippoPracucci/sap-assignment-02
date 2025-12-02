package api_gateway.domain;

import common.ddd.ValueObject;

public record DeliveryTime(int days, int hours) implements ValueObject {

    @Override
    public String toString() {
        return this.days + " days" + " and " + this.hours + " hours";
    }
}
