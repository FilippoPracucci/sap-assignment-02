package delivery_service.infrastructure;

import io.vertx.core.json.JsonObject;
import lobby_service.domain.Address;

import java.util.Calendar;
import java.util.Optional;

public class DeliveryJsonConverter {

    public static Address getAddress(final JsonObject json, final String key) {
        return new Address(
                json.getJsonObject(key).getString("street"),
                json.getJsonObject(key).getNumber("number").intValue()
        );
    }

    public static Optional<Calendar> getExpectedShippingMoment(final JsonObject json) {
        return json.containsKey("expectedShippingMoment")
                ? Optional.of(new Calendar.Builder().setDate(
                        json.getJsonObject("expectedShippingMoment").getNumber("year").intValue(),
                        json.getJsonObject("expectedShippingMoment").getNumber("month").intValue() - 1,
                        json.getJsonObject("expectedShippingMoment").getNumber("day").intValue()
                ).setTimeOfDay(
                        json.getJsonObject("expectedShippingMoment").getNumber("hours").intValue(),
                        json.getJsonObject("expectedShippingMoment").getNumber("minutes").intValue(),
                        0
                ).build())
                : Optional.empty();
    }
}
