package org.jboss.windup.tooling;

import java.util.logging.Logger;

import org.jboss.windup.tooling.vertx.VertxServer;
import org.jboss.windup.tooling.vertx.VertxService;

public class ToolingHttpServer implements ToolingServer {
	
	private static Logger LOG = Logger.getLogger(ToolingHttpServer.class.getName());

	public static void main(String[] args) {
		ToolingHttpServer toolingServer = new ToolingHttpServer();
		toolingServer.startServer(0, "");
	}

	public void startServer(int port, String version) {
		LOG.info("Starting HTTP Server...");

		VertxService vertxService = new VertxService();
		VertxServer server = new VertxServer();
		vertxService.getVertx().deployVerticle(server);
	}
}
