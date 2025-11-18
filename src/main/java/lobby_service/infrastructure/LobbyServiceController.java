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

import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
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
	static final String LOGIN_RESOURCE_PATH = 			"/api/" + API_VERSION + "/accounts/:accountId/login";
	static final String CREATE_DELIVERY_RESOURCE_PATH = 	"/api/" + API_VERSION + "/user-sessions/:sessionId/create-delivery";
	static final String TRACK_DELIVERY_RESOURCE_PATH = 		"/api/" + API_VERSION + "/user-sessions/:sessionId/track-delivery";

	static final String DELIVERY_SERVICE_URI = 	"/api/" + API_VERSION + "/deliveries";

	/* Ref. to the application layer */
	private final LobbyService lobbyService;
	
	public LobbyServiceController(final LobbyService service, final int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.lobbyService = service;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "Lobby Service initializing...");
		HttpServer server = vertx.createHttpServer();

		/* REST API routes */
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, LOGIN_RESOURCE_PATH).handler(this::login);
		router.route(HttpMethod.POST, CREATE_DELIVERY_RESOURCE_PATH).handler(this::createDelivery);
		router.route(HttpMethod.POST, TRACK_DELIVERY_RESOURCE_PATH).handler(this::trackDelivery);

		/* static files */
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		var fut = server
			.requestHandler(router)
			.listen(port);
		fut.onSuccess(res -> logger.log(Level.INFO, "Lobby Service ready - port: " + port));
		return fut;
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
		logger.log(Level.INFO, "Login request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			String userId = context.pathParam("accountId");
			String password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				String userSessionId = lobbyService.login(userId, password);
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
		logger.log(Level.INFO, "Create delivery request");
		context.request().handler(buf -> {
			final JsonObject deliveryDetailJson = buf.toJsonObject();
			String userSessionId = context.pathParam("sessionId");
			var reply = new JsonObject();
			try {
				final Optional<Calendar> targetTime = DeliveryJsonConverter.getTargetTime(deliveryDetailJson);
				if (targetTime.isPresent() && targetTime.get().toInstant().isBefore(Instant.now())) {
					reply.put("result", "error");
					reply.put("error", "past-target-time");
				} else {
					final DeliveryId deliveryId = this.lobbyService.createNewDelivery(
							userSessionId,
							deliveryDetailJson.getNumber("weight").doubleValue(),
							DeliveryJsonConverter.getAddress(deliveryDetailJson, "startingPlace"),
							DeliveryJsonConverter.getAddress(deliveryDetailJson, "destinationPlace"),
							targetTime
					);
					reply.put("result", "ok");
					reply.put("deliveryId", deliveryId.id());
					reply.put("deliveryLink", DELIVERY_SERVICE_URI + "/" + deliveryId.id());
					reply.put("trackDeliveryLink", TRACK_DELIVERY_RESOURCE_PATH.replace(":sessionId", userSessionId));
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
		logger.log(Level.INFO, "Track delivery request");
		context.request().handler(buf -> {
			final String userSessionId = context.pathParam("sessionId");
			final String deliveryId = buf.toJsonObject().getString("deliveryId");
			final JsonObject reply = new JsonObject();
			try {
				String trackingSessionId = this.lobbyService.trackDelivery(userSessionId, new DeliveryId(deliveryId));
				reply.put("result", "ok");
				reply.put("trackingSessionId", trackingSessionId);
				reply.put("trackingSessionLink", DELIVERY_SERVICE_URI + "/" + deliveryId + "/" + trackingSessionId);
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				ex.printStackTrace();
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
