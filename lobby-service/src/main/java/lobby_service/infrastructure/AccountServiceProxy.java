package main.java.lobby_service.infrastructure;

import common.hexagonal.Adapter;
import lobby_service.application.AccountService;
import lobby_service.application.ServiceNotAvailableException;
import lobby_service.application.UserNotFoundException;
import io.vertx.core.json.JsonObject;
import lobby_service.domain.UserId;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

@Adapter
public class AccountServiceProxy implements AccountService {

	private final String serviceURI;
	
	public AccountServiceProxy(final String serviceAPIEndpoint)  {
		this.serviceURI = serviceAPIEndpoint;		
	}
	
	@Override
	public boolean isValidPassword(UserId userId, String password) throws UserNotFoundException, ServiceNotAvailableException {
		HttpClient client = HttpClient.newHttpClient();

		JsonObject body = new JsonObject();
		body.put("password", password);

		String isValidReq = serviceURI + "/api/v1/accounts/" + userId.id() + "/check-pwd";
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(isValidReq))
				.header("Accept", "application/json")
				.POST(BodyPublishers.ofString(body.toString()))
				.build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException | IOException e) {
            throw new ServiceNotAvailableException();
        }
        System.out.println("Response Code: " + response.statusCode());

		if (response.statusCode() == 200) {
            return switch (new JsonObject(response.body()).getString("result")) {
                case "valid-password" -> true;
                case "invalid-password" -> false;
                default -> throw new UserNotFoundException();
            };
		} else {
			System.out.println("POST request failed: " + response.body());
			return false;
		}
	}
}
