package delivery_service.infrastructure;

import common.hexagonal.Adapter;
import delivery_service.application.DeliveryServiceEventObserver;
import delivery_service.domain.Delivered;
import delivery_service.domain.DeliveryEvent;
import delivery_service.domain.Shipped;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

@Adapter
public class PrometheusDeliveryServiceObserver implements DeliveryServiceEventObserver {

    private final Counter nTotalDeliveriesCreated;
    private final Gauge nDeliveriesOnDelivery;
    private final Counter nDeliveriesDelivered;

    public PrometheusDeliveryServiceObserver(final int port) throws ObservabilityMetricServerException {
        JvmMetrics.builder().register();

        this.nTotalDeliveriesCreated = Counter.builder()
                .name("delivery_service_num_deliveries")
                .help("Total number of deliveries created")
                .register();

        this.nDeliveriesOnDelivery = Gauge.builder()
                .name("delivery_service_num_deliveries_on_delivery")
                .help("Number of deliveries on delivery")
                .register();

        this.nDeliveriesDelivered = Counter.builder()
                .name("delivery_service_num_deliveries_delivered")
                .help("Number of deliveries delivered")
                .register();
        try {
            HTTPServer.builder().port(port).buildAndStart();
        } catch (final IOException ex) {
            throw new ObservabilityMetricServerException();
        }
    }


    @Override
    public synchronized void notifyNewDeliveryCreated() {
        this.nTotalDeliveriesCreated.inc();

    }

    @Override
    public synchronized void notifyDeliveryEvent(final DeliveryEvent event) {
        if (event instanceof Shipped) {
            this.nDeliveriesOnDelivery.inc();
        } else if (event instanceof Delivered) {
            this.nDeliveriesOnDelivery.dec();
            this.nDeliveriesDelivered.inc();
        }
    }
}
