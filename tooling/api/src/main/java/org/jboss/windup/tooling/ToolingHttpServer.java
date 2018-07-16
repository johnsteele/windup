package org.jboss.windup.tooling;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.windup.tooling.vertx.VertxServer;
import org.jboss.windup.tooling.vertx.VertxService;

public class ToolingHttpServer implements ToolingServer {
	
	private static Logger LOG = Logger.getLogger(ToolingHttpServer.class.getName());

	@Inject
    private ExecutionBuilder executionBuilder;

	public static void main(String[] args) {
		ToolingHttpServer toolingServer = new ToolingHttpServer();
		toolingServer.startServer(0, "");
	}

	public void startServer(int port, String version) {
		System.out.println("Starting HTTP server...");
		try
        {
			executionBuilder.setVersion(version);
			VertxService vertxService = new VertxService();
			VertxServer server = new VertxServer(executionBuilder);
			vertxService.getVertx().deployVerticle(server);
		}
		catch (RemoteException e)
        {
            LOG.severe("Bootstrap error while create HTTP server.");
            e.printStackTrace();
        }
	}
}
