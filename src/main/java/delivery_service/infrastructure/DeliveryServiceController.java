package delivery_service.infrastructure;

import delivery_service.application.*;
import delivery_service.domain.*;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
*
* Delivery Service controller
*
*/
public class DeliveryServiceController extends VerticleBase  {

	private final int port;
	static Logger logger = Logger.getLogger("[Delivery Service Controller]");

	static final String API_VERSION = "v1";
	static final String DELIVERIES_RESOURCE_PATH = "/api/" + API_VERSION + "/deliveries";
	static final String DELIVERY_RESOURCE_PATH =  DELIVERIES_RESOURCE_PATH +   "/:deliveryId";
	static final String TRACK_RESOURCE_PATH =  DELIVERY_RESOURCE_PATH +   "/track";
	static final String TRACKING_RESOURCE_PATH = DELIVERY_RESOURCE_PATH + "/:trackingSessionId";
	static final String STOP_TRACKING_RESOURCE_PATH = TRACKING_RESOURCE_PATH + "/stop";
	
	/* Ref. to the application layer */
	private final DeliveryService deliveryService;
	
	public DeliveryServiceController(final DeliveryService deliveryService, final int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.deliveryService = deliveryService;
	}

	public Future<?> start() {
		logger.log(Level.INFO, "Delivery Service initializing...");
		HttpServer server = vertx.createHttpServer();
				
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, DELIVERIES_RESOURCE_PATH).handler(this::createNewDelivery);
		router.route(HttpMethod.GET, DELIVERY_RESOURCE_PATH).handler(this::getDeliveryDetail);
		router.route(HttpMethod.POST, TRACK_RESOURCE_PATH).handler(this::trackDelivery);
		router.route(HttpMethod.POST, STOP_TRACKING_RESOURCE_PATH).handler(this::stopTrackingDelivery);
		router.route(HttpMethod.GET, TRACKING_RESOURCE_PATH).handler(this::getDeliveryStatus);
		this.handleEventSubscription(server, "/api/events");

		/* static files */
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		var fut = server
			.requestHandler(router)
			.listen(this.port);
		fut.onSuccess(res -> {
			logger.log(Level.INFO, "Delivery Service ready - port: " + this.port);
		});

		return fut;
	}

	/**
	 * 
	 * Create a New Delivery - by users logged in (with a UserSession)
	 * 
	 * @param context
	 */
	protected void createNewDelivery(final RoutingContext context) {
		logger.log(Level.INFO, "CreateNewDelivery request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {
			final JsonObject deliveryDetailJson = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + deliveryDetailJson);
			var reply = new JsonObject();
			try {	// TODO add immediate time, check date > now
				final DeliveryId deliveryId = this.deliveryService.createNewDelivery(
						deliveryDetailJson.getNumber("weight").doubleValue(),
						DeliveryJsonConverter.getAddress(deliveryDetailJson, "startingPlace"),
						DeliveryJsonConverter.getAddress(deliveryDetailJson, "destinationPlace"),
						DeliveryJsonConverter.getTargetTime(deliveryDetailJson)
				);
				reply.put("result", "ok");
				reply.put("deliveryId", deliveryId.id());
				reply.put("deliveryLink", DELIVERY_RESOURCE_PATH.replace(":deliveryId", deliveryId.id()));
				reply.put("trackDeliveryLink", TRACK_RESOURCE_PATH.replace(":deliveryId", deliveryId.id()));
				sendReply(context.response(), reply);
			} catch (final Exception ex) {
				logger.log(Level.SEVERE, ex.getMessage());
				sendError(context.response());
			}
		});		
	}

	/**
	 * 
	 * Get delivery detail
	 * 
	 * @param context
	 */
	protected void getDeliveryDetail(final RoutingContext context) {
		logger.log(Level.INFO, "get delivery detail");
		context.request().endHandler(h -> {
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			var reply = new JsonObject();
			try {
				reply.put("result", "ok");
				reply.put(
						"deliveryDetail",
						DeliveryJsonConverter.toJson(
								this.deliveryService.getDeliveryDetail(deliveryId),
								Optional.empty()
						)
				);
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "delivery-not-present");
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				sendError(context.response());
			}
		});
	}
	
	/**
	 * 
	 * Track a Delivery - by user logged in (with a UserSession)
	 * 
	 * It creates a TrackingSession
	 * 
	 * @param context
	 */
	protected void trackDelivery(final RoutingContext context) {
		logger.log(Level.INFO, "TrackDelivery request - " + context.currentRoute().getPath());
		context.request().endHandler(h -> {
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			logger.log(Level.INFO, "Track delivery " + deliveryId.id());
			var reply = new JsonObject();
			try {
				final TrackingSession trackingSession = this.deliveryService.trackDelivery(deliveryId,
						new VertxTrackingSessionEventObserver(vertx.eventBus()));
				reply.put("trackingSessionId", trackingSession.getId());
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}

	protected void stopTrackingDelivery(final RoutingContext context) {
		logger.log(Level.INFO, "Stop tracking delivery request - " + context.currentRoute().getPath());
		context.request().endHandler(h -> {
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			final String trackingSessionId = context.pathParam("trackingSessionId");
			logger.log(Level.INFO, "Stop tracking delivery " + deliveryId.id());
			var reply = new JsonObject();
			try {
				this.deliveryService.stopTrackingDelivery(deliveryId, trackingSessionId);
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException | TrackingSessionNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}
	
	/**
	 * 
	 * Get delivery status - by users tracking a delivery (with a TrackingSession)
	 * 
	 * @param context
	 */
	protected void getDeliveryStatus(final RoutingContext context) {
		logger.log(Level.INFO, "GetDeliveryStatus request - " + context.currentRoute().getPath());
		context.request().endHandler(h -> {
			final JsonObject reply = new JsonObject();
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			final String trackingSessionId = context.pathParam("trackingSessionId");
			try {
				final DeliveryStatus deliveryStatus = this.deliveryService.getDeliveryStatus(deliveryId,
						trackingSessionId);
				reply.put("result", "ok");
				final JsonObject deliveryJson = new JsonObject();
				deliveryJson.put("deliveryId", deliveryId.id());
				deliveryJson.put("deliveryStatus", deliveryStatus.getState().toString());
				if (deliveryStatus.isTimeLeftAvailable()) {
					deliveryJson.put("timeLeft", deliveryStatus.getTimeLeft().days() + " days left");
				}
				reply.put("deliveryStatus", deliveryJson);
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "delivery-not-present");
				sendReply(context.response(), reply);
			} catch (final TrackingSessionNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "tracking-session-not-present");
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				logger.info(ex.getClass().toString());
				sendError(context.response());
			}
		});
	}


	/* Handling subscribers using web sockets */
	
	protected void handleEventSubscription(final HttpServer server, final String path) {
		server.webSocketHandler(webSocket -> {
			if (webSocket.path().equals(path)) {
				logger.log(Level.INFO, "New subscription accepted.");

				/*
				 *
				 * Receiving a first message including the id of the delivery
				 * to observe
				 *
				 */
				webSocket.textMessageHandler(openMsg -> {
					logger.log(Level.INFO, "For delivery: " + openMsg);
					JsonObject obj = new JsonObject(openMsg);
					final String trackingSessionId = obj.getString("trackingSessionId");


					/*
					 * Subscribing events on the event bus to receive
					 * events concerning the delivery, to be notified
					 * to the frontend using the websocket
					 *
					 */
					EventBus eventBus = this.vertx.eventBus();

					eventBus.consumer(trackingSessionId, msg -> {
						final JsonObject event = (JsonObject) msg.body();
						logger.log(Level.INFO, "Event: " + event.encodePrettily());
						webSocket.writeTextMessage(event.encodePrettily());
					});

					try {
						final TrackingSession trackingSession = this.deliveryService.getTrackingSession(trackingSessionId);
						trackingSession.getTrackingSessionEventNotifier().enableEventNotification(trackingSessionId);
					} catch (final TrackingSessionNotFoundException e) {
						throw new RuntimeException(e);
					}
				});
			}
		});
	}
	
	/* Aux methods */

	private void sendReply(final HttpServerResponse response, final JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}
	
	private void sendError(final HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}


}
