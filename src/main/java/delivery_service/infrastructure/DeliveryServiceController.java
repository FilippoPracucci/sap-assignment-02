package delivery_service.infrastructure;

import com.fasterxml.jackson.core.JsonParser;
import delivery_service.application.InvalidTrackingException;
import delivery_service.application.DeliveryNotFoundException;
import delivery_service.application.DeliveryService;
import delivery_service.application.TrackingSession;
import delivery_service.domain.Address;
import delivery_service.domain.DeliveryDetail;
import delivery_service.domain.DeliveryId;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Calendar;
import java.util.Map;
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
			JsonObject deliveryDetail = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + deliveryDetail);
			var reply = new JsonObject();
			try {
				final DeliveryId deliveryId = this.deliveryService.createNewDelivery(
						deliveryDetail.getNumber("weight").doubleValue(),
						new Address(
								deliveryDetail.getJsonObject("startingPlace").getString("street"),
								deliveryDetail.getJsonObject("startingPlace").getNumber("number").intValue()
						),
						new Address(
								deliveryDetail.getJsonObject("destinationPlace").getString("street"),
								deliveryDetail.getJsonObject("destinationPlace").getNumber("number").intValue()
						),
						new Calendar.Builder().setDate(
								deliveryDetail.getJsonObject("targetTime").getNumber("year").intValue(),
								deliveryDetail.getJsonObject("targetTime").getNumber("month").intValue(),
								deliveryDetail.getJsonObject("targetTime").getNumber("day").intValue()
						).build()
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
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			var reply = new JsonObject();
			try {
				final DeliveryDetail deliveryDetail = this.deliveryService.getDeliveryDetail(deliveryId);
				reply.put("result", "ok");
				var deliveryJson = new JsonObject();
				deliveryJson.put("deliveryId", deliveryId.id());
				deliveryJson.put("weight", deliveryDetail.weight());
				deliveryJson.put("startingPlace", new JsonObject(Map.of(
						"street", deliveryDetail.startingPlace().street(),
						"number", deliveryDetail.startingPlace().number())
				));
				deliveryJson.put("destinationPlace", new JsonObject(Map.of(
						"street", deliveryDetail.destinationPlace().street(),
						"number", deliveryDetail.destinationPlace().number())
				));
				deliveryJson.put("targetTime", new JsonObject(Map.of(
						"year", deliveryDetail.expectedShippingDate().get(Calendar.YEAR),
						"month", deliveryDetail.expectedShippingDate().get(Calendar.MONTH),
						"day", deliveryDetail.expectedShippingDate().get(Calendar.DAY_OF_MONTH))
				));
				reply.put("deliveryDetail", deliveryJson);
				sendReply(context.response(), reply);
			} catch (final DeliveryNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "delivery-not-present");
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				sendError(context.response());
			}
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
		context.request().handler(buf -> {
			final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
			logger.log(Level.INFO, "Track delivery " + deliveryId.id());
			var reply = new JsonObject();
			try {
				final TrackingSession trackingSession = this.deliveryService.trackDelivery(deliveryId,
						new VertxTrackingSessionEventObserver(vertx.eventBus()));
				reply.put("trackingSessionId", trackingSession.getId());
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (final InvalidTrackingException ex) {
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
		context.request().handler(buf -> {
			var  reply = new JsonObject();
			try {
				final DeliveryId deliveryId = new DeliveryId(context.pathParam("deliveryId"));
				final String trackingSessionId = context.pathParam("trackingSessionId");
				//this.deliveryService.getDeliveryStatus(deliveryId, trackingSessionId);
				/*var ps = gameService.getPlayerSession(playerSessionId);
				ps.makeMove(x, y);				
				reply.put("result", "accepted");
				var gameId = context.pathParam("gameId");
				var movePath = PLAYER_MOVE_RESOURCE_PATH
						.replace(":gameId",gameId)
						.replace(":playerSessionId",ps.getId());
				reply.put("moveLink", movePath);
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId);
				sendReply(context.response(), reply);*/
			/*} catch (InvalidMoveException ex) {
				reply.put("result", "invalid-move");
				sendReply(context.response(), reply);	*/
			} catch (Exception ex1) {
				reply.put("result", ex1.getMessage());
				try {
					sendReply(context.response(), reply);
				} catch (Exception ex2) {
					sendError(context.response());
				}				
			}
		});
	}


	/* Handling subscribers using web sockets */
	
	protected void handleEventSubscription(HttpServer server, String path) {
		server.webSocketHandler(webSocket -> {
			logger.log(Level.INFO, "New TTT subscription accepted.");

			/* 
			 * 
			 * Receiving a first message including the id of the game
			 * to observe 
			 * 
			 */
			webSocket.textMessageHandler(openMsg -> {
				logger.log(Level.INFO, "For game: " + openMsg);
				JsonObject obj = new JsonObject(openMsg);
				String playerSessionId = obj.getString("playerSessionId");
				
				
				/* 
				 * Subscribing events on the event bus to receive
				 * events concerning the game, to be notified 
				 * to the frontend using the websocket
				 * 
				 */
				EventBus eb = vertx.eventBus();
				
				eb.consumer(playerSessionId, msg -> {
					JsonObject ev = (JsonObject) msg.body();
					logger.log(Level.INFO, "Event: " + ev.encodePrettily());
					webSocket.writeTextMessage(ev.encodePrettily());
				});
				
				/*var ps = gameService.getPlayerSession(playerSessionId);
				var en = ps.getPlayerSessionEventNotifier();
				en.enableEventNotification(playerSessionId);*/
								
			});
		});
	}
	
	/* Aux methods */
	

	private void sendReply(HttpServerResponse response, JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}
	
	private void sendError(HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}


}
