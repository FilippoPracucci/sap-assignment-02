package delivery_service.infrastructure;

import common.hexagonal.Adapter;
import delivery_service.application.TrackingSessionEventObserver;
import delivery_service.domain.DeliveryTime;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * This is implementation of a tracking session delivery event observer,
 * based on Vert.x 
 * 
 * It dispatches the observed events on the Event Bus,
 * so that the game service controller could consume it and
 * send to the players, connected using a web socket.
 * 
 * It is an adaption of the TrackingSessionEventObserver port
 */
@Adapter
public class VertxTrackingSessionEventObserver implements TrackingSessionEventObserver {
	static Logger logger = Logger.getLogger("[VertxEventNotifierAdapter]");

	private final EventBus eventBus;
	private final List<JsonObject> eventBuffer;
	private boolean channelOnBusReady;
	
	public VertxTrackingSessionEventObserver(final EventBus eventBus) {
		this.eventBus = eventBus;
		this.eventBuffer = new LinkedList<>();
		this.channelOnBusReady = false;
	}
	
	public void enableEventNotification(final String trackingSessionId) {
		this.channelOnBusReady = true;
		for (var ev: this.eventBuffer) {
			this.eventBus.publish(trackingSessionId, ev);
		}
		this.eventBuffer.clear();
	}

	@Override
	public void shipped(final String trackingSessionId) {
		logger.info("package shipped for " + trackingSessionId);
		final JsonObject shippedEvent = new JsonObject();
		shippedEvent.put("event", "shipped");
		if (this.channelOnBusReady) {
			this.eventBus.publish(trackingSessionId, shippedEvent);
		} else {
			this.eventBuffer.add(shippedEvent);
		}
	}

	@Override
	public void delivered(final String trackingSessionId) {
		logger.info("package delivered for " + trackingSessionId);
		final JsonObject deliveredEvent = new JsonObject();
		deliveredEvent.put("event", "delivered");
		if (this.channelOnBusReady) {
			this.eventBus.publish(trackingSessionId, deliveredEvent);
		} else {
			this.eventBuffer.add(deliveredEvent);
		}
	}

	@Override
	public void timeElapsed(final String trackingSessionId, final DeliveryTime timeElapsed) {
		logger.info("Elapsed " + timeElapsed + " for " + trackingSessionId);
		final JsonObject timeElapsedEvent = new JsonObject();
		timeElapsedEvent.put("event", "time-elapsed");
		timeElapsedEvent.put("timeElapsed", timeElapsed.toString());
		if (this.channelOnBusReady) {
			this.eventBus.publish(trackingSessionId, timeElapsedEvent);
		} else {
			this.eventBuffer.add(timeElapsedEvent);
		}
	}
}
