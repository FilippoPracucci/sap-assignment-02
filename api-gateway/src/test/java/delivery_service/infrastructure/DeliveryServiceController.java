package delivery_service.infrastructure;

import api_gateway.application.DeliveryNotFoundException;
import api_gateway.application.TrackingSessionNotFoundException;
import api_gateway.domain.DeliveryId;
import api_gateway.domain.DeliveryStatus;
import api_gateway.infrastructure.DeliveryServiceVertx;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

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

	/* Health check endpoint */
	static final String HEALTH_CHECK_ENDPOINT = "/api/" + API_VERSION + "/health";
	
	/* Ref. to the application layer */
	private final DeliveryServiceVertx deliveryService;
	
	public DeliveryServiceController(final DeliveryServiceVertx deliveryService, final int port) {
		this.port = port;
		this.deliveryService = deliveryService;
	}

	public Future<?> start() {
		logger.info("Delivery Service initializing...");
		HttpServer server = vertx.createHttpServer();
				
		Router router = Router.router(vertx);
		router.route(HttpMethod.GET, DELIVERY_RESOURCE_PATH).handler(this::getDeliveryDetail);
		router.route(HttpMethod.POST, STOP_TRACKING_RESOURCE_PATH).handler(this::stopTrackingDelivery);
		router.route(HttpMethod.GET, TRACKING_RESOURCE_PATH).handler(this::getDeliveryStatus);
		router.route(HttpMethod.GET, HEALTH_CHECK_ENDPOINT).handler(this::healthCheckHandler);

		/* static files */
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		var fut = server
			.requestHandler(router)
			.listen(this.port);
		fut.onSuccess(res -> {
			logger.info("Delivery Service ready - port: " + this.port);
		});

		return fut;
	}

	protected void healthCheckHandler(final RoutingContext context) {
		logger.info("Health check request " + context.currentRoute().getPath());
		final JsonObject reply = new JsonObject();
		reply.put("status", "UP");
		sendReply(context.response(), reply);
	}

	/**
	 * 
	 * Get delivery detail
	 * 
	 * @param context
	 */
	protected void getDeliveryDetail(final RoutingContext context) {
		logger.info("get delivery detail");
		context.request().endHandler(h -> {
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			var reply = new JsonObject();
			try {
				reply.put("result", "ok");
				reply.put(
						"deliveryDetail",
						DeliveryJsonConverter.toJson(this.deliveryService.getDeliveryDetail(deliveryId))
				);
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				sendError(context.response());
			}
		});
	}

	protected void stopTrackingDelivery(final RoutingContext context) {
		logger.info("Stop tracking delivery request - " + context.currentRoute().getPath());
		context.request().endHandler(h -> {
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			final String trackingSessionId = context.pathParam("trackingSessionId");
			logger.info("Stop tracking delivery " + deliveryId.id());
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
		logger.info("GetDeliveryStatus request - " + context.currentRoute().getPath());
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
				deliveryJson.put("deliveryState", deliveryStatus.getState().getLabel());
				if (deliveryStatus.getTimeLeft().isPresent()) {
					deliveryJson.put("timeLeft", deliveryStatus.getTimeLeft().get().days() + " days left");
				}
				reply.put("deliveryStatus", deliveryJson);
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
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
