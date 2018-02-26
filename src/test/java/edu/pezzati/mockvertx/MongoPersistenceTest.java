package edu.pezzati.mockvertx;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import edu.pezzati.mockvertx.model.Doc;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest({ MongoClient.class })
public class MongoPersistenceTest {

    private MongoClient mongo;
    private Vertx vertx;

    @Before
    public void initSingleTest(TestContext ctx) throws Exception {
	vertx = Vertx.vertx();
	mongo = Mockito.mock(MongoClient.class);
	System.out.println("MongoClient mock: " + mongo.toString());
	PowerMockito.mockStatic(MongoClient.class);
	PowerMockito.when(MongoClient.createShared(Mockito.any(), Mockito.any())).thenReturn(mongo);
	vertx.deployVerticle(MongoPersistence.class, new DeploymentOptions(), ctx.asyncAssertSuccess());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadSomeDocs(TestContext ctx) {
	Doc expected = new Doc();
	expected.setName("report");
	expected.setPreview("loremipsum");
	Message<JsonObject> msg = Mockito.mock(Message.class);
	System.out.println("Message<JsonObject> mock: " + msg.toString());
	Mockito.when(msg.body()).thenReturn(JsonObject.mapFrom(expected));
	JsonObject result = new JsonObject().put("name", "report").put("preview", "loremipsum");
	AsyncResult<JsonObject> asyncResult = Mockito.mock(AsyncResult.class);
	System.out.println("AsyncResult<List<JsonObject>> mock: " + asyncResult.toString());
	Mockito.when(asyncResult.succeeded()).thenReturn(true);
	Mockito.when(asyncResult.result()).thenReturn(result);
	Mockito.doAnswer(new Answer<AsyncResult<JsonObject>>() {
	    @Override
	    public AsyncResult<JsonObject> answer(InvocationOnMock arg0) throws Throwable {
		((Handler<AsyncResult<JsonObject>>) arg0.getArgument(3)).handle(asyncResult);
		return null;
	    }
	}).when(mongo).findOne(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	Async async = ctx.async();
	vertx.eventBus().send("persistence", new JsonObject(), msgh -> {
	    if (msgh.failed()) {
		System.out.println(msgh.cause().getMessage());
	    }
	    ctx.assertTrue(msgh.succeeded());
	    ctx.assertEquals(expected, Json.decodeValue(msgh.result().body().toString(), Doc.class));
	    async.complete();
	});
	async.await();
    }
}
