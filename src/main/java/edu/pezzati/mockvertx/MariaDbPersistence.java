package edu.pezzati.mockvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class MariaDbPersistence extends AbstractVerticle {

    private JDBCClient mariaDb;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
	mariaDb = JDBCClient.createShared(vertx, new JsonObject());
	vertx.eventBus().consumer("persistence", this::messageHandler);
	super.start(startFuture);
    }

    private void messageHandler(Message<JsonObject> msg) {
	mariaDb.queryWithParams("select * from documents", new JsonArray(), h -> {
	    if (h.succeeded()) {
		msg.reply(h.result());
	    } else {
		msg.fail(500, h.cause().getMessage());
	    }
	});
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
	mariaDb.close();
	super.stop(stopFuture);
    }
}
