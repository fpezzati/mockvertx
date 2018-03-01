package edu.pezzati.mockvertx;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;

public class MariaDbPersistence extends AbstractVerticle {

    private JDBCClient mariaDb;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
	mariaDb = JDBCClient.createShared(vertx, new JsonObject());
	vertx.eventBus().consumer("persistence", this::messageHandler);
	super.start(startFuture);
    }

    private void messageHandler(Message<JsonObject> msg) {
	mariaDb.getConnection(connH -> {
	    if (connH.succeeded()) {
		connH.result().queryWithParams("select * from documents", new JsonArray(), h -> {
		    System.out.println("handler h: " + h.toString());
		    if (h.succeeded()) {
			msg.reply(Json.encode(getJsonObjectsFromResultSet(h.result())));
		    } else {
			msg.fail(500, h.cause().getMessage());
		    }
		});
	    } else {
		msg.fail(500, connH.cause().getMessage());
	    }
	});
    }

    private List<JsonObject> getJsonObjectsFromResultSet(ResultSet result) {
	return result.getRows().stream().map(row -> {
	    JsonObject jOb = new JsonObject();
	    jOb.put("name", row.getString("name"));
	    jOb.put("preview", row.getString("preview"));
	    return jOb;
	}).collect(Collectors.toList());
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
	mariaDb.close();
	super.stop(stopFuture);
    }
}
