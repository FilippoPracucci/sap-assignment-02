package api_gateway.infrastructure;

import api_gateway.application.AccountNotFoundException;
import api_gateway.domain.Account;
import api_gateway.domain.AccountImpl;
import common.hexagonal.Adapter;
import io.vertx.core.json.JsonObject;
import api_gateway.application.AccountService;
import api_gateway.application.ServiceNotAvailableException;
import api_gateway.domain.UserId;

import java.io.IOException;
import java.net.http.HttpResponse;

@Adapter
public class AccountServiceProxy extends HTTPSyncBaseProxy implements AccountService {

	private final String serviceURI;
	
	public AccountServiceProxy(final String serviceAPIEndpoint)  {
		this.serviceURI = serviceAPIEndpoint;		
	}

	@Override
	public Account registerUser(final String userName, final String password) throws ServiceNotAvailableException {
		try {
			final JsonObject requestBody = new JsonObject();
			requestBody.put("userName", userName);
			requestBody.put("password", password);

			final HttpResponse<String> response = doPost( this.serviceURI + "/api/v1/accounts", requestBody);

			if (response.statusCode() == 200) {
				final JsonObject responseBody = new JsonObject(response.body());
				return new AccountImpl(new UserId(responseBody.getString("accountId")), userName, password);
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (final IOException | InterruptedException e) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public Account getAccountInfo(final UserId userId) throws AccountNotFoundException, ServiceNotAvailableException {
		try {
			final HttpResponse<String> response = doGet( this.serviceURI + "/api/v1/accounts/" + userId.id());
			if (response.statusCode() == 200) {
				final JsonObject responseBody = new JsonObject(response.body());
				if (responseBody.getString("result").equals("error")) {
					throw new AccountNotFoundException();
				}
				final JsonObject accountInfo = responseBody.getJsonObject("accountInfo");
				return new AccountImpl(userId, accountInfo.getString("userName"),
						accountInfo.getString("password"), accountInfo.getNumber("whenCreated").longValue());
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (final IOException | InterruptedException e) {
			throw new ServiceNotAvailableException();
		}
	}
}
