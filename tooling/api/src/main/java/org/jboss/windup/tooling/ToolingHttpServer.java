package org.jboss.windup.tooling;

import org.jboss.windup.tooling.vertx.VertxServer;
import org.jboss.windup.tooling.vertx.VertxService;

public class ToolingHttpServer implements ToolingServer {

	public static void main(String[] args) {
		ToolingHttpServer toolingServer = new ToolingHttpServer();
		toolingServer.startServer(0, "");
	}

	public void startServer(int port, String version) {
		VertxService vertxService = new VertxService();
		VertxServer server = new VertxServer();
		vertxService.getVertx().deployVerticle(server);
	}
}
