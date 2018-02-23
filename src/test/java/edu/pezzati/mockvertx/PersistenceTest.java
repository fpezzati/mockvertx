package edu.pezzati.mockvertx;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import edu.pezzati.mockvertx.model.Doc;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PersistenceTest {

    @Mock
    MongoClient mongo;
    @InjectMocks
    Persistence service;

    @Before
    public void initSingleTest(TestContext ctx) {
	MockitoAnnotations.initMocks(this);
    }

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
	}).when(mongo.find(Mockito.any(), Mockito.any(), Mockito.any()));
	Vertx.vertx().deployVerticle(service);
	async.await();
    }
}
