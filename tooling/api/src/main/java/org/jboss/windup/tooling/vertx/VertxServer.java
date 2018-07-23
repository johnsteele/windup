package org.jboss.windup.tooling.vertx;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.jboss.windup.tooling.ExecutionBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

@SuppressWarnings("deprecation")
public class VertxServer extends AbstractVerticle {
	
	public static final String SERVER_BUS = "rhamt.server";
	public static final String CLIENT_BUS = "rhamt.client";

	private ExecutionBuilder executionBuilder;
	private int port;
	private Analysis analysis;

	public VertxServer(ExecutionBuilder executionBuilder, int port) {
		this.executionBuilder = executionBuilder;
		this.port = port;
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
			System.out.println("Server Received: " + message);
			//eb.send(CLIENT_BUS, "Hello client. This is your server. I got the message: " + message.toString());

			System.out.println("server parsing data: " + message.body().toString());
			System.out.println("server parsing data: " + message.body().toString());

			JsonObject data = (JsonObject)message.body();
			boolean starting = data.getBoolean("start") != null;

			System.out.println("server starting: " + starting);

			this.stopAnalysis(message).thenAccept((result) -> {
				if (starting && result) {
					analyze(data);
				}
			});
		});

		router.route().handler(BodyHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(port, r -> {
			if (r.succeeded()) {
				System.out.println("rhamt server listening on " + port);
			}
			else {
				System.err.println("VertxServer HTTP server FAILED to start...");
			}
		});
	}

	private CompletableFuture<Boolean> stopAnalysis(Message<Object> msg) {
		CompletableFuture<Boolean> f = new CompletableFuture<Boolean>();
		System.out.println("server attempting to check if analysis is in progress");
		this.stopAnalysis().thenAccept((result) -> {
			if (!result) {
				System.out.println("server unable to stop previous analysis.");
				msg.fail(-1, "server msg: cannot start analysis. unable to stop previous analysis.2");
			}
			else {
				System.out.println("#we stopped previous analysis. starting analysis now!!!");
				msg.reply(true);
			}
			f.complete(result);
		});
		return f;
	}


	private CompletableFuture<Boolean> stopAnalysis() {
		CompletableFuture<Boolean> f = new CompletableFuture<Boolean>();
		System.out.println("server checking if analysis is in progress");
		if (this.analysis != null && vertx.deploymentIDs().contains(analysis.deploymentID())) {
			System.out.println("server removing previous analysis.");
			vertx.undeploy(this.analysis.deploymentID(), (result) -> {
				if (result.succeeded()) {
					System.out.println("server stopped previous analysis.");
					f.complete(true);
					this.analysis = null;
				}
				else {
					System.out.println("server was unable to stop previous analysis.");
					f.complete(false);
				}
			});
		}
		else {
			System.out.println("server does not have an analysis currently in progress.");
			f.complete(true);
		}
		return f;
	}

	private void analyze(JsonObject data) {
		//vertx.eventBus().publish(CLIENT_BUS, "Analysis will begin in a moment.");
		
		DeploymentOptions options = new DeploymentOptions().setWorker(true);

		Set<String> input = new HashSet<String>();
		for (Iterator<Object> iter = data.getJsonArray("input").iterator(); iter.hasNext();) {
			String i = ((JsonObject)iter.next()).getString("location");
			input.add(i);
			System.out.println("Analyzing input: " + i);
		}

		String output = data.getString("output");
		System.out.println("Using output: " + output);
		
		//input.add("/Users/johnsteele/Desktop/demos/demo");
		//String output = "/Users/johnsteele/Desktop/demos/demo/out";

		this.analysis = new Analysis(CLIENT_BUS, executionBuilder);
		vertx.deployVerticle(analysis, options, (e) -> {
			System.out.println("analysis worker deployed.");
			System.out.println("analysis beginning now.");
			analysis.analyze(input, output);
			System.out.println("analysis done.");
			vertx.undeploy(this.analysis.deploymentID());
			System.out.println("anaysis verticle undeployed");
			analysis = null;
		});
		System.out.println("finished setting up analysis worker. waiting for it to be deployed...");
	}
}