package delivery_service.infrastructure;

import delivery_service.domain.*;
import io.vertx.core.json.JsonObject;

import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

public class DeliveryJsonConverter {

    public static Delivery fromJson(final JsonObject json) {
        return new DeliveryImpl(
                new DeliveryId(json.getString("deliveryId")),
                json.getNumber("weight").doubleValue(),
                getAddress(json, "startingPlace"),
                getAddress(json, "destinationPlace"),
                getTargetTime(json),
                DeliveryState.valueOfLabel(json.getString("state"))
        );
    }

    public static Address getAddress(final JsonObject json, final String key) {
        return new Address(
                json.getJsonObject(key).getString("street"),
                json.getJsonObject(key).getNumber("number").intValue()
        );
    }

    public static Calendar getTargetTime(final JsonObject json) {
        return new Calendar.Builder().setDate(
                json.getJsonObject("targetTime").getNumber("year").intValue(),
                json.getJsonObject("targetTime").getNumber("month").intValue() - 1,
                json.getJsonObject("targetTime").getNumber("day").intValue()
        ).setTimeOfDay(
                json.getJsonObject("targetTime").getNumber("hours").intValue(),
                json.getJsonObject("targetTime").getNumber("minutes").intValue(),
                0
        ).build();
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
        obj.put("targetTime", new JsonObject(Map.of(
                "year", deliveryDetail.expectedShippingDate().get(Calendar.YEAR),
                "month", deliveryDetail.expectedShippingDate().get(Calendar.MONTH) + 1,
                "day", deliveryDetail.expectedShippingDate().get(Calendar.DAY_OF_MONTH),
                "hours", deliveryDetail.expectedShippingDate().get(Calendar.HOUR_OF_DAY),
                "minutes", deliveryDetail.expectedShippingDate().get(Calendar.MINUTE))
        ));
        deliveryState.ifPresent(state -> obj.put("state", state.getLabel()));
        return obj;
    }
}
