package api_gateway.infrastructure;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.Optional;

import api_gateway.application.*;
import api_gateway.domain.Address;
import api_gateway.domain.DeliveryId;
import api_gateway.domain.UserId;
import common.hexagonal.Adapter;
import io.vertx.core.json.JsonObject;

/**
 * 
 * Proxy for LobbyService, using synch HTTP 
 * 
 */
@Adapter
public class LobbyServiceProxy extends HTTPSyncBaseProxy implements LobbyService {

	private final String serviceURI;
	
	public LobbyServiceProxy(final String serviceAPIEndpoint)  {
		this.serviceURI = serviceAPIEndpoint;
	}

	@Override
	public String login(final UserId userId, final String password) throws LoginFailedException,
			ServiceNotAvailableException {
		try {
			final JsonObject requestBody = new JsonObject();
			requestBody.put("password", password);
			final HttpResponse<String> response = doPost( this.serviceURI + "/api/v1/accounts/" + userId.id()
							+ "/login", requestBody);
			if (response.statusCode() == 200) {
				final JsonObject responseBody = new JsonObject(response.body());
				if (responseBody.getString("result").equals("login-failed")) {
					throw new LoginFailedException();
				}
				return responseBody.getString("sessionId");
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (final IOException | InterruptedException e) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public DeliveryId createNewDelivery(final String userSessionId, final double weight, final Address startingPlace,
										final Address destinationPlace, final Optional<Calendar> expectedShippingMoment)
			throws CreateDeliveryFailedException, ServiceNotAvailableException {
		try {
			final JsonObject requestBody = DeliveryJsonConverter.toJson(weight, startingPlace, destinationPlace,
					expectedShippingMoment);
			final HttpResponse<String> response = doPost( this.serviceURI + "/api/v1/user-sessions/"
							+ userSessionId + "/create-delivery", requestBody);
			if (response.statusCode() == 200) {
				final JsonObject responseBody = new JsonObject(response.body());
				if (responseBody.getString("result").equals("error")) {
					throw new CreateDeliveryFailedException(responseBody.getString("error"));
				}
				return new DeliveryId(responseBody.getString("deliveryId"));
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (final IOException | InterruptedException e) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public String trackDelivery(final String userSessionId, final DeliveryId deliveryId)
			throws TrackDeliveryFailedException, ServiceNotAvailableException {
		try {
			final JsonObject requestBody = new JsonObject();
			requestBody.put("deliveryId", deliveryId.id());
			final HttpResponse<String> response = doPost( this.serviceURI + "/api/v1/user-sessions/"
					+ userSessionId + "/track-delivery", requestBody);
			if (response.statusCode() == 200) {
				final JsonObject responseBody = new JsonObject(response.body());
				if (responseBody.getString("result").equals("error")) {
					throw new TrackDeliveryFailedException(responseBody.getString("error"));
				}
				return responseBody.getString("trackingSessionId");
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (final IOException | InterruptedException e) {
			throw new ServiceNotAvailableException();
		}
	}
}
