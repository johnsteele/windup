package org.jboss.windup.tooling.vertx;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.windup.tooling.ExecutionBuilder;

import io.vertx.core.AbstractVerticle;
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
	
	private static Logger LOG = Logger.getLogger(VertxServer.class.getName());

	private static final String SERVER_BUS = "rhamt.server";
	private static final String CLIENT_BUS = "rhamt.client";

	private ExecutionBuilder executionBuilder;

	public VertxServer(ExecutionBuilder executionBuilder) {
		this.executionBuilder = executionBuilder;
	}

	@Override
	public void start() throws Exception {

		System.out.println("VertxServer starting HTTP server...");
		
		Router router = Router.router(vertx);

		BridgeOptions opts = new BridgeOptions().addInboundPermitted(new PermittedOptions().setAddress(SERVER_BUS))
				.addOutboundPermitted(new PermittedOptions().setAddress(CLIENT_BUS));

		SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
		router.route("/eventbus/*").handler(ebHandler);

		EventBus eb = vertx.eventBus();

		eb.consumer(SERVER_BUS).handler(message -> {
			eb.publish(CLIENT_BUS, "Hello client. This is your server. I got the message");
		});

		router.route().handler(BodyHandler.create());
		router.post("/start").handler(this::start);
		router.post("/stop").handler(this::stop);
		router.post("/analyze").handler(this::analyze);

		vertx.createHttpServer().requestHandler(router::accept).listen(8080, r -> {
			if (r.succeeded()) {
				LOG.info("VertxServer starting HTTP server...");
			}
			else {
				LOG.info("VertxServer HTTP server FAILED to start...");
			}
		});
	}

	private void start(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		JsonObject data = new JsonObject();
		data.put("status", "started");
		response.putHeader("content-type", "application/json").end(data.encodePrettily());
	}

	private void stop(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		JsonObject data = new JsonObject();
		data.put("status", "stopped");
		response.putHeader("content-type", "application/json").end(data.encodePrettily());
	}

	private void analyze(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();

		Set<String> input = new HashSet<String>();
		input.add("/Users/johnsteele/Desktop/demos/demo");

		try {
			executionBuilder.setInput(input);
			executionBuilder.setOutput("/Users/johnsteele/Desktop/demos/demo/out");
			executionBuilder.setProgressMonitor(new ProgressMonitor());
			executionBuilder.setOption(IOptionKeys.sourceModeOption, true);
		}
		catch (RemoteException e) {
			LOG.severe("Server `RemoteException` error while performing analysis.");
            e.printStackTrace();
		}
		

		JsonObject data = new JsonObject();
		data.put("status", "analyzed");
		response.putHeader("content-type", "application/json").end(data.encodePrettily());
	}
}