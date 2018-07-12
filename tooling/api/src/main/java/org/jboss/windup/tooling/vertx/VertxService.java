package org.jboss.windup.tooling.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class VertxService {
	private Vertx vertx;
	private EventBus eventBus;

	public VertxService() {
		this.vertx = Vertx.vertx();
		this.eventBus = vertx.eventBus();
	}

	public Vertx getVertx() {
		return this.vertx;
	}

	public EventBus getEventBus() {
		return this.eventBus;
	}
}