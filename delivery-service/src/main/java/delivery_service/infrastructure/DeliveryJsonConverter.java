package main.java.delivery_service.infrastructure;

import delivery_service.domain.*;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class DeliveryJsonConverter {

    public static Delivery fromJson(final JsonObject json) {
        return new DeliveryImpl(
                new DeliveryId(json.getString("deliveryId")),
                json.getNumber("weight").doubleValue(),
                getAddress(json, "startingPlace"),
                getAddress(json, "destinationPlace"),
                getExpectedShippingMoment(json),
                DeliveryState.valueOfLabel(json.getString("state"))
        );
    }

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

    public static JsonObject toJson(final DeliveryDetail deliveryDetail, final Optional<DeliveryState> deliveryState) {
        final JsonObject obj = new JsonObject();
        obj.put("deliveryId", deliveryDetail.getId().id());
        obj.put("weight", deliveryDetail.weight());
        obj.put("startingPlace", new JsonObject(Map.of(
                "street", deliveryDetail.startingPlace().street(),
                "number", deliveryDetail.startingPlace().number())
        ));
        obj.put("destinationPlace", new JsonObject(Map.of(
                "street", deliveryDetail.destinationPlace().street(),
                "number", deliveryDetail.destinationPlace().number())
        ));
        obj.put("expectedShippingMoment", new JsonObject(Map.of(
                "year", deliveryDetail.expectedShippingMoment().get(Calendar.YEAR),
                "month", deliveryDetail.expectedShippingMoment().get(Calendar.MONTH) + 1,
                "day", deliveryDetail.expectedShippingMoment().get(Calendar.DAY_OF_MONTH),
                "hours", deliveryDetail.expectedShippingMoment().get(Calendar.HOUR_OF_DAY),
                "minutes", deliveryDetail.expectedShippingMoment().get(Calendar.MINUTE))
        ));
        deliveryState.ifPresent(state -> obj.put("state", state.getLabel()));
        return obj;
    }
}
