package api_gateway.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Optional;
import java.util.logging.Logger;

import api_gateway.application.ServiceNotAvailableException;
import io.vertx.core.json.JsonObject;

abstract public class HTTPSyncBaseProxy {

	private static final double ERROR_RATE_THRESHOLD = 0.5;

	static Logger logger = Logger.getLogger("[HTTPSyncBaseProxy]");

	private final String serviceURI;
	private int nSuccessfulRequests = 0;
	private int nFailedRequests = 0;
	private boolean isCircuitClose = true;

	public HTTPSyncBaseProxy(final String serviceURI) {
		this.serviceURI = serviceURI;
	}

	protected HttpResponse<String> doPost(String uri, JsonObject body) throws ServiceNotAvailableException {
		return this.doRequest(uri, true, Optional.of(body));
    }

	protected HttpResponse<String> doGet(String uri) throws ServiceNotAvailableException {
		return this.doRequest(uri, false, Optional.empty());
	}

	private HttpResponse<String> doRequest(final String uri, final boolean isPost, final Optional<JsonObject> body)
			throws ServiceNotAvailableException{
		if (!this.isCircuitClose) {
			throw new ServiceNotAvailableException();
		}
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request;
		if (isPost) {
			request = HttpRequest.newBuilder().uri(URI.create(this.serviceURI + uri))
					.header("Accept", "application/json")
					.POST(BodyPublishers.ofString(body.toString()))
					.build();
		} else {
			request = HttpRequest.newBuilder().uri(URI.create(this.serviceURI + uri)).GET().build();
		}
		try {
			return client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (final IOException | InterruptedException e) {
			this.incrementFailedRequests();
			throw new ServiceNotAvailableException();
		}
	}

	protected void incrementSuccessfulRequests() {
		this.nSuccessfulRequests++;
		this.updateCircuitBreaker();
		logger.info(this.nSuccessfulRequests + " successful requests");
	}

	protected void incrementFailedRequests() {
		this.nFailedRequests++;
		this.updateCircuitBreaker();
		logger.info(this.nFailedRequests + " failed requests");
	}

	private boolean isErrorRateOverThreshold() {
		final int minRequests = 5;
		final int totalRequests = (this.nFailedRequests + this.nSuccessfulRequests);
		return (totalRequests > minRequests) && ((double) this.nFailedRequests / totalRequests) > ERROR_RATE_THRESHOLD;
	}

	private void updateCircuitBreaker() {
		this.isCircuitClose = !this.isErrorRateOverThreshold();
		if (!this.isCircuitClose) {
			Thread.ofVirtual().start(() -> {
				while (true) {
					try {
						logger.info("Start timeout");
						Thread.sleep(10_000);
					} catch (final InterruptedException e) {
						throw new RuntimeException(e);
					}
					logger.info("End timeout");
					if (this.isServiceHealthy()) {
						logger.info("Service healthy");
						this.closeCircuit();
						this.resetRequestsCounters();
						break;
					}
				}
            });
		}
	}

	private boolean isServiceHealthy() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.serviceURI + "/api/v1/health")).GET().build();
		try {
			client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (final IOException | InterruptedException e) {
			return false;
		}
		return true;
	}

	private synchronized void closeCircuit() {
		this.isCircuitClose = true;
	}

	private synchronized void resetRequestsCounters() {
		this.nSuccessfulRequests = 0;
		this.nFailedRequests = 0;
	}
}
