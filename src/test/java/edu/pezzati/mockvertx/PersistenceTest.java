package edu.pezzati.mockvertx;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import edu.pezzati.mockvertx.model.Doc;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.impl.MongoClientImpl;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest({ MongoClient.class })
public class PersistenceTest {

    private MongoClient mongo;
    private Persistence service;
    private JsonObject mongoConf;

    @Before
    public void initSingleTest(TestContext ctx) throws Exception {
	mongo = Mockito.mock(MongoClientImpl.class);
	mongoConf = new JsonObject().put("db_name", "persistence-test").put("connection_string",
		"mongodb://localhost:27017");
	service = Mockito.mock(Persistence.class);
	Mockito.when(service.getMongoClient(Mockito.any())).thenReturn(mongo);
	Vertx.vertx().deployVerticle(service, ctx.asyncAssertSuccess());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadSomeDocs(TestContext ctx) {
	Async async = ctx.async();
	Doc expected = new Doc();
	expected.setName("report");
	expected.setPreview("loremipsum");
	Message<JsonObject> msg = Mockito.mock(Message.class);
	Mockito.when(msg.body()).thenReturn(JsonObject.mapFrom(expected));
	List<JsonObject> result = new ArrayList<>();
	result.add(new JsonObject().put("name", "report").put("preview", "loremipsum"));
	AsyncResult<List<JsonObject>> asyncResult = Mockito.mock(AsyncResult.class);
	Mockito.when(asyncResult.succeeded()).thenReturn(true);
	Mockito.when(asyncResult.result()).thenReturn(result);
	Mockito.doAnswer(new Answer<AsyncResult<List<JsonObject>>>() {
	    @Override
	    public AsyncResult<List<JsonObject>> answer(InvocationOnMock arg0) throws Throwable {
		return asyncResult;
	    }
	}).when(mongo).findOne(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	Vertx.vertx().eventBus().send("persistence", mongoConf, msgh -> {
	    ctx.assertTrue(msgh.succeeded());
	    ctx.assertEquals(expected, msgh.result().body());
	    async.awaitSuccess();
	});
	async.await();
    }
}
