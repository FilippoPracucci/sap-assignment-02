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
import java.util.Map;

@Adapter
public class DeliveryServiceProxy implements DeliveryService {

    private final String serviceURI;

    public DeliveryServiceProxy(final String serviceAPIEndpoint)  {
        this.serviceURI = serviceAPIEndpoint;
    }

    @Override
    public DeliveryId createNewDelivery(final double weight, final Address startingPlace,
                                        final Address destinationPlace, final Calendar targetTime)
            throws CreateDeliveryFailedException, ServiceNotAvailableException {
        HttpClient client = HttpClient.newHttpClient();
        JsonObject body = new JsonObject();
        body.put("weight", weight);
        body.put("startingPlace", new JsonObject(Map.of(
                "street", startingPlace.street(),
                "number", startingPlace.number())
        ));
        body.put("destinationPlace", new JsonObject(Map.of(
                "street", destinationPlace.street(),
                "number", destinationPlace.number())
        ));
        body.put("targetTime", new JsonObject(Map.of(
                "year", targetTime.get(Calendar.YEAR),
                "month", targetTime.get(Calendar.MONTH),
                "day", targetTime.get(Calendar.DAY_OF_MONTH))
        ));

        final String deliveryResourceEndpoint = serviceURI + "/api/v1/deliveries";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deliveryResourceEndpoint))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
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
            throw new CreateDeliveryFailedException();
        }
        return new DeliveryId(responseBody.getString("deliveryId"));
    }

    @Override
    public String trackDelivery(DeliveryId deliveryId) throws TrackDeliveryFailedException, ServiceNotAvailableException {
        return "";
    }

	/*
	@Override
	public String joinGame(UserId userId, String gameId, TTTSymbol symbol) throws InvalidJoinGameException, JoinGameFailedException, ServiceNotAvailableException {
	    HttpClient client = HttpClient.newHttpClient();
        JsonObject body = new JsonObject();
        body.put("userId", userId.id());
        body.put("symbol", symbol.equals(TTTSymbol.X) ? "X" : "O");
        		
        String joinGameEndpoint = serviceURI + "/api/v1/games/" + gameId + "/join";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(joinGameEndpoint))
                .header("Accept", "application/json")
                .POST(BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = null;
        try {
        	response = client.send(request, HttpResponse.BodyHandlers.ofString());
        	System.out.println("Response Code: " + response.statusCode());
        } catch (Exception ex) {
        	ex.printStackTrace();
        	throw new JoinGameFailedException();
        }
        if (response.statusCode() == 200) {
            JsonObject json = new JsonObject(response.body());	                               
            var res = json.getString("result");
            if (res.equals("ok")) {
				var playerSessionId = json.getString("playerSessionId");
				return playerSessionId;
 		    } else if (res.equals("error")) {
 		    	throw new InvalidJoinGameException();
            } else {
            	throw new JoinGameFailedException();
            }
        } else {
            System.out.println("POST request failed: " + response.body());
			throw new ServiceNotAvailableException();
        }
	}*/
	


}
