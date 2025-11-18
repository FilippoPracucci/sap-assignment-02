package lobby_service.infrastructure;

import delivery_service.domain.*;
import io.vertx.core.json.JsonObject;

import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

public class DeliveryJsonConverter {

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

    public static JsonObject toJson(final double weight, final Address startingPlace,
                              final Address destinationPlace, final Calendar targetTime) {
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
        obj.put("targetTime", new JsonObject(Map.of(
                "year", targetTime.get(Calendar.YEAR),
                "month", targetTime.get(Calendar.MONTH) + 1,
                "day", targetTime.get(Calendar.DAY_OF_MONTH),
                "hours", targetTime.get(Calendar.HOUR_OF_DAY),
                "minutes", targetTime.get(Calendar.MINUTE))
        ));
        return obj;
    }
}
