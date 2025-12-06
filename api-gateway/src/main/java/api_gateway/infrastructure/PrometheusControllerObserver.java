package api_gateway.infrastructure;

import common.hexagonal.Adapter;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

@Adapter
public class PrometheusControllerObserver implements ControllerObserver {

	private final Counter nTotalNumberOfRESTRequests;

    public PrometheusControllerObserver(final int port) throws ObservabilityMetricServerException {
		JvmMetrics.builder().register();
		
		this.nTotalNumberOfRESTRequests = Counter.builder()
				.name("api_gateway_num_rest_requests_total")
				.help("Total number of REST requests received")
				.register();

		try {
            HTTPServer.builder()
                    .port(port)
                    .buildAndStart();
		} catch (final IOException e) {
			throw new ObservabilityMetricServerException();
		}
	}

	@Override
	public void notifyNewRESTRequest() {
		this.nTotalNumberOfRESTRequests.inc();
	}
}