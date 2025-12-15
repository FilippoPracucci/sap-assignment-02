package api_gateway.infrastructure;

import api_gateway.application.*;
import api_gateway.domain.*;
import common.hexagonal.Adapter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.net.http.HttpResponse;
import java.util.Optional;

@Adapter
public class DeliveryServiceProxy extends HTTPSyncBaseProxy implements DeliveryServiceVertx {

    private final String wsAddress;
    private final int wsPort;

    public DeliveryServiceProxy(final String serviceAPIEndpoint, final String wsAddress, final int wsPort)  {
        super(serviceAPIEndpoint);
        this.wsAddress = wsAddress;
        this.wsPort = wsPort;
    }

    @Override
    public DeliveryDetail getDeliveryDetail(final DeliveryId deliveryId) throws DeliveryNotFoundException,
            ServiceNotAvailableException {
        final HttpResponse<String> response = doGet("/api/v1/deliveries/" + deliveryId.id());
        if (response.statusCode() == 200) {
            this.incrementSuccessfulRequests();
            final JsonObject responseBody = new JsonObject(response.body());
            if (responseBody.getString("result").equals("error")) {
                throw new DeliveryNotFoundException();
            }
            final JsonObject deliveryDetail = responseBody.getJsonObject("deliveryDetail");
            return new DeliveryDetailImpl(
                    deliveryId,
                    deliveryDetail.getNumber("weight").doubleValue(),
                    DeliveryJsonConverter.getAddress(deliveryDetail,"startingPlace"),
                    DeliveryJsonConverter.getAddress(deliveryDetail,"destinationPlace"),
                    DeliveryJsonConverter.getExpectedShippingMoment(deliveryDetail)
                            .orElseThrow(RuntimeException::new)
            );
        } else {
            this.incrementFailedRequests();
            throw new ServiceNotAvailableException();
        }
    }

    @Override
    public DeliveryStatus getDeliveryStatus(final DeliveryId deliveryId, final String trackingSessionId)
            throws DeliveryNotFoundException, TrackingSessionNotFoundException, ServiceNotAvailableException {
        final HttpResponse<String> response = doGet("/api/v1/deliveries/" + deliveryId.id()
                + "/" + trackingSessionId);
        if (response.statusCode() == 200) {
            this.incrementSuccessfulRequests();
            final JsonObject responseBody = new JsonObject(response.body());
            if (responseBody.getString("result").equals("error")) {
                if (responseBody.getString("error") != null
                        && responseBody.getString("error").equals("tracking-session-not-present")) {
                    throw new TrackingSessionNotFoundException();
                }
                throw new DeliveryNotFoundException();
            }
            final JsonObject deliveryStatus = responseBody.getJsonObject("deliveryStatus");
            return new DeliveryStatusImpl(
                    deliveryId,
                    DeliveryState.valueOfLabel(deliveryStatus.getString("deliveryState")),
                    deliveryStatus.containsKey("timeLeft")
                            ? Optional.of(new DeliveryTime(
                                Integer.parseInt(deliveryStatus.getString("timeLeft").split(" ")[0]), 0
                            ))
                            : Optional.empty()
            );
        } else {
            this.incrementFailedRequests();
            throw new ServiceNotAvailableException();
        }
    }

    @Override
    public void stopTrackingDelivery(final DeliveryId deliveryId, final String trackingSessionId)
            throws DeliveryNotFoundException, TrackingSessionNotFoundException, ServiceNotAvailableException {
        final HttpResponse<String> response = doPost("/api/v1/deliveries/" + deliveryId.id()
                + "/" + trackingSessionId + "/stop", new JsonObject());
        if (response.statusCode() == 200) {
            this.incrementSuccessfulRequests();
            final JsonObject responseBody = new JsonObject(response.body());
            if (responseBody.getString("result").equals("error")) {
                if (responseBody.getString("error") != null
                        && responseBody.getString("error").equals("Delivery does not exist")) {
                    throw new DeliveryNotFoundException();
                }
                throw new TrackingSessionNotFoundException();
            }
        } else {
            this.incrementFailedRequests();
            throw new ServiceNotAvailableException();
        }
    }

    @Override
    public void createAnEventChannel(final String trackingSessionId, final Vertx vertx) {
        var eventBus = vertx.eventBus();
        vertx.createWebSocketClient()
                .connect(this.wsPort, this.wsAddress, "/api/v1/events")
                .onSuccess(ws -> {
                    System.out.println("Connected!");

                    ws.textMessageHandler(msg -> eventBus.publish(trackingSessionId, msg));

                    /* first message */
                    final JsonObject obj = new JsonObject();
                    obj.put("trackingSessionId", trackingSessionId);
                    ws.writeTextMessage(obj.toString());
                })
                .onFailure(err -> eventBus.publish(trackingSessionId, "error"));
    }
}
