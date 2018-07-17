package org.jboss.windup.tooling.vertx;

import org.jboss.windup.tooling.ExecutionBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

@SuppressWarnings("deprecation")
public class VertxServer extends AbstractVerticle {
	
	public static final String SERVER_BUS = "rhamt.server.*";
	public static final String CLIENT_BUS = "rhamt.client.*";

	private ExecutionBuilder executionBuilder;
	private Analysis analysis;

	public VertxServer(ExecutionBuilder executionBuilder) {
		this.executionBuilder = executionBuilder;
	}

	@Override
	public void start() throws Exception {

		System.out.println("VertxServer starting HTTP server...");
		
		Router router = Router.router(vertx);

		BridgeOptions opts = new BridgeOptions().addInboundPermitted(new PermittedOptions().setAddressRegex(SERVER_BUS))
				.addOutboundPermitted(new PermittedOptions().setAddressRegex(CLIENT_BUS));

		SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
		router.route("/eventbus/*").handler(ebHandler);

		EventBus eb = vertx.eventBus();

		eb.consumer(SERVER_BUS).handler(message -> {
			eb.publish(CLIENT_BUS, "Hello client. This is your server. I got the message");
		});

		router.route().handler(BodyHandler.create());
		router.post("/start").handler(this::start);
		router.post("/stop/:id").handler(this::stop);

		vertx.createHttpServer().requestHandler(router::accept).listen(8080, r -> {
			if (r.succeeded()) {
				System.out.println("VertxServer starting HTTP server...");
			}
			else {
				System.err.println("VertxServer HTTP server FAILED to start...");
			}
		});
	}

	private void start(RoutingContext routingContext) {
		System.out.println("analyze...");
		HttpServerResponse response = routingContext.response();
		String id = routingContext.request().getParam("id");
		JsonObject data = new JsonObject();
		data.put("attempting to analyze", id);
		if (analysis != null && !analysis.isComplete()) {
			data.put("Cannot start analysis. Previous analysis still in progress", id);
			response.putHeader("content-type", "application/json").end(data.encodePrettily());
			return;
		}
		System.out.println("analyzing: " + id);
		DeploymentOptions options = new DeploymentOptions().setWorker(true);
		this.analysis = new Analysis(executionBuilder, id);
		vertx.deployVerticle(analysis, options, (e) -> {
			System.out.println("analysis worker deployed.");
			data.put("analsysisDeployed", id);
			response.putHeader("content-type", "application/json").end(data.encodePrettily());
			System.out.println("analysis beginning now.");
			analysis.analyze();
			System.out.println("analysis done.");
		});
		System.out.println("finished setting up anslysis worker. waiting for it to be deployed...");
	}

	private void stop(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		JsonObject result = new JsonObject();
		String id = routingContext.request().getParam("id");
		if (analysis != null && analysis.isAnalysis(id)) {
			result.put("stopping", id);
			System.out.println("attempting to stop analsyis.");
			analysis.dispose();
			System.out.println("analysis disposed.");
		}
		else {
			result.put("anslysisDeosntExist", id);
		}
		response.putHeader("content-type", "application/json").end(result.encodePrettily());
	}
}