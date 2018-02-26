package edu.pezzati.mockvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class Persistence extends AbstractVerticle {

    private MongoClient mongo;
    private String collection;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
	mongo = getMongoClient(new JsonObject());
	vertx.eventBus().consumer("persistence", this::messageHandler);
	super.start(startFuture);
    }

    public MongoClient getMongoClient(JsonObject conf) {
	return MongoClient.createShared(vertx, conf);
    }

    private void messageHandler(Message<JsonObject> msg) {
	mongo.findOne("documents", msg.body(), new JsonObject(), h -> {
	    if (h.succeeded()) {
		msg.reply(JsonObject.mapFrom(h.result()));
	    } else {
		msg.fail(500, h.cause().getMessage());
	    }
	});
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
	mongo.close();
	super.stop(stopFuture);
    }

    public void getDocByName(Message<JsonObject> msg) {
	mongo.find(collection, msg.body(), h -> {
	    if (h.succeeded()) {
		msg.reply(h.result());
	    } else {
		msg.fail(500, h.cause().getMessage());
	    }
	});
    }
}
