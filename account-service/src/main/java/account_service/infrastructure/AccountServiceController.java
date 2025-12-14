package account_service.infrastructure;

import account_service.application.AccountNotFoundException;
import account_service.application.AccountService;
import account_service.domain.Account;
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
 * Account Service Controller
 *
 */
public class AccountServiceController extends VerticleBase {

	private final int port;
	static Logger logger = Logger.getLogger("[AccountServiceController]");

	static final String API_VERSION = "v1";
	static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts";
	static final String ACCOUNT_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId";
	static final String CHECK_PWD_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId/check-pwd";
	static final String LOGIN_PATH = "/api/" + API_VERSION + "/accounts/:accountId/login";

	/* Health check endpoint */
	static final String HEALTH_CHECK_ENDPOINT = "/api/" + API_VERSION + "/health";

	/* Ref. to the application layer */
	private final AccountService accountService;

	public AccountServiceController(final AccountService service, final int port) {
		this.port = port;
		this.accountService = service;
	}

	public Future<?> start() {
		logger.info("Account Service initializing...");
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, ACCOUNTS_RESOURCE_PATH).handler(this::createAccount);
		router.route(HttpMethod.GET, ACCOUNT_RESOURCE_PATH).handler(this::getAccountInfo);
		router.route(HttpMethod.POST, CHECK_PWD_RESOURCE_PATH).handler(this::checkAccountPassword);
		router.route(HttpMethod.GET, HEALTH_CHECK_ENDPOINT).handler(this::healthCheckHandler);

		/* static files */

		router.route("/public/*").handler(StaticHandler.create());

		/* start the server */

		var fut = server.requestHandler(router).listen(this.port);

		fut.onSuccess(res -> {
			logger.info("Account Service ready - port: " + this.port);
		});

		return fut;
	}

	protected void healthCheckHandler(final RoutingContext context) {
		logger.info("Health check request " + context.currentRoute().getPath());
		final JsonObject reply = new JsonObject();
		reply.put("status", "UP");
		sendReply(context.response(), reply);
	}

	/* List of handlers mapping the API */

	/**
	 * 
	 * Register a new user
	 * 
	 * @param context
	 */
	protected void createAccount(final RoutingContext context) {
		logger.info("create a new account");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.info("Payload: " + userInfo);
			var userName = userInfo.getString("userName");
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				final Account account = this.accountService.registerUser(userName, password);
				reply.put("result", "ok");
				reply.put("accountId", account.getId().id());
				reply.put("loginLink", LOGIN_PATH.replace(":accountId", account.getId().id()));
				reply.put("accountLink", ACCOUNT_RESOURCE_PATH.replace(":accountId", account.getId().id()));
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}

	/**
	 * 
	 * Get account info
	 * 
	 * @param context
	 */
	protected void getAccountInfo(final RoutingContext context) {
		logger.info("get account info");
		var userId = context.pathParam("accountId");
		var reply = new JsonObject();
		try {
			var acc = this.accountService.getAccountInfo(userId);
			reply.put("result", "ok");
			var accJson = new JsonObject();
			accJson.put("userName", acc.getUserName());
			accJson.put("password", acc.getPassword());
			accJson.put("whenCreated", acc.getWhenCreated());
			reply.put("accountInfo", accJson);
			sendReply(context.response(), reply);
		} catch (AccountNotFoundException ex) {
			reply.put("result", "error");
			reply.put("error", "account-not-present");
			sendReply(context.response(), reply);
		} catch (Exception ex1) {
			sendError(context.response());
		}
	}

	/**
	 * 
	 * Get account info
	 * 
	 * @param context
	 */
	protected void checkAccountPassword(final RoutingContext context) {
		logger.info("check account password");
		context.request().handler(buf -> {
			var userId = context.pathParam("accountId");
			JsonObject userInfo = buf.toJsonObject();
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				var res = this.accountService.isValidPassword(userId, password);
				if (res) {
					reply.put("result", "valid-password");
				} else {
					reply.put("result", "invalid-password");
				}
				sendReply(context.response(), reply);
			} catch (AccountNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "User id does not exist");
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
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
