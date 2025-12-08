package lobby_service.infrastructure;

import lobby_service.application.LobbyService;
import lobby_service.application.LoginFailedException;
import lobby_service.domain.DeliveryId;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lobby_service.domain.TimeConverter;
import lobby_service.domain.UserId;

import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Logger;

/**
*
* Lobby Service controller
*
*/
public class LobbyServiceController extends VerticleBase  {

	private final int port;
	static Logger logger = Logger.getLogger("[LobbyController]");

	static final String API_VERSION = "v1";
	static final String USER_SESSIONS_RESOURCE_PATH = "/api/" + API_VERSION + "/user-sessions";
	static final String LOGIN_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId/login";
	static final String CREATE_DELIVERY_RESOURCE_PATH = "/api/" + API_VERSION + "/user-sessions/:sessionId/create-delivery";
	static final String TRACK_DELIVERY_RESOURCE_PATH = 	"/api/" + API_VERSION + "/user-sessions/:sessionId/track-delivery";

	static final String DELIVERY_SERVICE_URI = "/api/" + API_VERSION + "/deliveries";

	/* Health check endpoint */
	static final String HEALTH_CHECK_ENDPOINT = "/api/" + API_VERSION + "/health";

	/* Ref. to the application layer */
	private final LobbyService lobbyService;
	
	public LobbyServiceController(final LobbyService service, final int port) {
		this.port = port;
		this.lobbyService = service;

	}

	public Future<?> start() {
		logger.info("Lobby Service initializing...");
		HttpServer server = vertx.createHttpServer();

		/* REST API routes */
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, LOGIN_RESOURCE_PATH).handler(this::login);
		router.route(HttpMethod.POST, CREATE_DELIVERY_RESOURCE_PATH).handler(this::createDelivery);
		router.route(HttpMethod.POST, TRACK_DELIVERY_RESOURCE_PATH).handler(this::trackDelivery);
		router.route(HttpMethod.GET, HEALTH_CHECK_ENDPOINT).handler(this::healthCheckHandler);

		/* static files */
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		var fut = server
			.requestHandler(router)
			.listen(port);
		fut.onSuccess(res -> logger.info("Lobby Service ready - port: " + port));
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
	 * Login a user
	 * 
	 * It creates a User Session
	 * 
	 * @param context
	 */
	protected void login(final RoutingContext context) {
		logger.info("Login request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.info("Payload: " + userInfo);
			String userId = context.pathParam("accountId");
			String password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				String userSessionId = lobbyService.login(new UserId(userId), password);
				reply.put("result", "ok");
				var createPath = CREATE_DELIVERY_RESOURCE_PATH.replace(":sessionId", userSessionId);
				var trackPath = TRACK_DELIVERY_RESOURCE_PATH.replace(":sessionId", userSessionId);
				reply.put("sessionId", userSessionId);
				reply.put("sessionLink", USER_SESSIONS_RESOURCE_PATH + "/" + userSessionId);
				reply.put("createDeliveryLink", createPath);
				reply.put("trackDeliveryLink", trackPath);
				sendReply(context.response(), reply);
			} catch (LoginFailedException ex) {
				reply.put("result", "login-failed");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}

	/**
	 * 
	 * Create a delivery
	 * 
	 * @param context
	 */
	protected void createDelivery(final RoutingContext context) {
		logger.info("Create delivery request");
		context.request().handler(buf -> {
			final JsonObject deliveryDetailJson = buf.toJsonObject();
			String userSessionId = context.pathParam("sessionId");
			var reply = new JsonObject();
			try {
				final Optional<Calendar> expectedShippingMoment =
						DeliveryJsonConverter.getExpectedShippingMoment(deliveryDetailJson);
				if (expectedShippingMoment.isPresent() && TimeConverter.getZonedDateTime(expectedShippingMoment.get())
						.isBefore(TimeConverter.getNowAsZonedDateTime())) {
					reply.put("result", "error");
					reply.put("error", "past-shipping-moment");
				} else {
					final DeliveryId deliveryId = this.lobbyService.createNewDelivery(
							userSessionId,
							deliveryDetailJson.getNumber("weight").doubleValue(),
							DeliveryJsonConverter.getAddress(deliveryDetailJson, "startingPlace"),
							DeliveryJsonConverter.getAddress(deliveryDetailJson, "destinationPlace"),
							expectedShippingMoment
					);
					reply.put("result", "ok");
					reply.put("deliveryId", deliveryId.id());
					reply.put("deliveryLink", DELIVERY_SERVICE_URI + "/" + deliveryId.id());
					reply.put("trackDeliveryLink",
							TRACK_DELIVERY_RESOURCE_PATH.replace(":sessionId", userSessionId));
				}
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} 			
		});
	}

	/**
	 * 
	 * Track a delivery
	 * 
	 * @param context
	 */
	protected void trackDelivery(RoutingContext context) {
		logger.info("Track delivery request");
		context.request().handler(buf -> {
			final String userSessionId = context.pathParam("sessionId");
			final String deliveryId = buf.toJsonObject().getString("deliveryId");
			final JsonObject reply = new JsonObject();
			try {
				String trackingSessionId = this.lobbyService.trackDelivery(userSessionId, new DeliveryId(deliveryId));
				reply.put("result", "ok");
				reply.put("trackingSessionId", trackingSessionId);
				reply.put("trackingSessionLink", DELIVERY_SERVICE_URI + "/" + deliveryId + "/" + trackingSessionId);
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				logger.severe(ex.getMessage());
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} 			
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
