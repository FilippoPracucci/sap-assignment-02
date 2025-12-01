package main.java.lobby_service.infrastructure;

import delivery_service.domain.*;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
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

    public static JsonObject toJson(final double weight, final Address startingPlace,
                              final Address destinationPlace, final Optional<Calendar> expectedShippingMoment) {
        final JsonObject obj = new JsonObject();
        obj.put("weight", weight);
        obj.put("startingPlace", new JsonObject(Map.of(
                "street", startingPlace.street(),
                "number", startingPlace.number())
        ));
        obj.put("destinationPlace", new JsonObject(Map.of(
                "street", destinationPlace.street(),
                "number", destinationPlace.number())
        ));
        expectedShippingMoment.ifPresent(time -> obj.put("expectedShippingMoment", new JsonObject(Map.of(
                "year", time.get(Calendar.YEAR),
                "month", time.get(Calendar.MONTH) + 1,
                "day", time.get(Calendar.DAY_OF_MONTH),
                "hours", time.get(Calendar.HOUR_OF_DAY),
                "minutes", time.get(Calendar.MINUTE))))
        );
        return obj;
    }
}
