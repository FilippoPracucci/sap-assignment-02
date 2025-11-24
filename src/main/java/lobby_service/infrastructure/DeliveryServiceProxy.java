package lobby_service.infrastructure;

import common.hexagonal.Adapter;
import delivery_service.domain.Address;
import io.vertx.core.json.JsonObject;
import lobby_service.application.*;
import lobby_service.domain.DeliveryId;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.Optional;

@Adapter
public class DeliveryServiceProxy implements DeliveryService {

    private final String serviceURI;

    public DeliveryServiceProxy(final String serviceAPIEndpoint)  {
        this.serviceURI = serviceAPIEndpoint;
    }

    @Override
    public DeliveryId createNewDelivery(final double weight, final Address startingPlace,
                                        final Address destinationPlace, final Optional<Calendar> targetTime)
            throws CreateDeliveryFailedException, ServiceNotAvailableException {
        HttpClient client = HttpClient.newHttpClient();
        final String deliveryResourceEndpoint = serviceURI + "/api/v1/deliveries";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deliveryResourceEndpoint))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        DeliveryJsonConverter.toJson(weight, startingPlace, destinationPlace, targetTime).toString()
                ))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Code: " + response.statusCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CreateDeliveryFailedException();
        }

        if (response.statusCode() != 200) {
            System.out.println("POST request failed: " + response.body());
            throw new ServiceNotAvailableException();
        }
        final JsonObject responseBody = new JsonObject(response.body());
        if (responseBody.getString("result").equals("error")) {
            throw new CreateDeliveryFailedException("Invalid shipping time: " + responseBody.getString("error"));
        }
        return new DeliveryId(responseBody.getString("deliveryId"));
    }

    @Override
    public String trackDelivery(final DeliveryId deliveryId) throws TrackDeliveryFailedException, ServiceNotAvailableException {
        final HttpClient client = HttpClient.newHttpClient();
        final JsonObject body = new JsonObject();
        body.put("deliveryId", deliveryId.id());

        final String trackDeliveryResourceEndpoint = serviceURI + "/api/v1/deliveries/" + deliveryId.id() + "/track";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trackDeliveryResourceEndpoint))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Code: " + response.statusCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TrackDeliveryFailedException();
        }

        if (response.statusCode() != 200) {
            System.out.println("POST request failed: " + response.body());
            throw new ServiceNotAvailableException();
        }
        final JsonObject responseBody = new JsonObject(response.body());
        if (responseBody.getString("result").equals("error")) {
            throw new TrackDeliveryFailedException(responseBody.getString("error"));
        }
        return responseBody.getString("trackingSessionId");
    }
}
