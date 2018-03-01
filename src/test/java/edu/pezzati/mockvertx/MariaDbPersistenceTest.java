package edu.pezzati.mockvertx;

import java.util.ArrayList;
import java.util.List;

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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest({ JDBCClient.class })
public class MariaDbPersistenceTest {

    private JDBCClient mariaDb;
    private Vertx vertx;

    @Before
    public void initSingleTest(TestContext ctx) throws Exception {
	vertx = Vertx.vertx();
	mariaDb = Mockito.mock(JDBCClient.class);
	PowerMockito.mockStatic(JDBCClient.class);
	PowerMockito.when(JDBCClient.createShared(Mockito.any(), Mockito.any())).thenReturn(mariaDb);
	vertx.deployVerticle(MariaDbPersistence.class, new DeploymentOptions(), ctx.asyncAssertSuccess());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadSomeDocs(TestContext ctx) throws Exception {
	Doc expected = new Doc();
	expected.setName("report");
	expected.setPreview("loremipsum");
	List<JsonObject> list = new ArrayList<JsonObject>();
	list.add(JsonObject.mapFrom(expected));
	ResultSet resultSet = Mockito.mock(ResultSet.class);
	Mockito.when(resultSet.getRows()).thenReturn(list);
	AsyncResult<ResultSet> asyncResultResultSet = Mockito.mock(AsyncResult.class);
	Mockito.when(asyncResultResultSet.succeeded()).thenReturn(true);
	Mockito.when(asyncResultResultSet.result()).thenReturn(resultSet);
	SQLConnection sqlConnection = Mockito.mock(SQLConnection.class);
	Mockito.doAnswer(new Answer<AsyncResult<ResultSet>>() {
	    @Override
	    public AsyncResult<ResultSet> answer(InvocationOnMock arg0) throws Throwable {
		((Handler<AsyncResult<ResultSet>>) arg0.getArgument(2)).handle(asyncResultResultSet);
		return null;
	    }
	}).when(sqlConnection).queryWithParams(Mockito.any(), Mockito.any(), Mockito.any());
	AsyncResult<SQLConnection> sqlConnectionResult = Mockito.mock(AsyncResult.class);
	Mockito.when(sqlConnectionResult.succeeded()).thenReturn(true);
	Mockito.when(sqlConnectionResult.result()).thenReturn(sqlConnection);
	Mockito.doAnswer(new Answer<AsyncResult<SQLConnection>>() {
	    @Override
	    public AsyncResult<SQLConnection> answer(InvocationOnMock arg0) throws Throwable {
		((Handler<AsyncResult<SQLConnection>>) arg0.getArgument(0)).handle(sqlConnectionResult);
		return null;
	    }
	}).when(mariaDb).getConnection(Mockito.any());
	Async async = ctx.async();
	vertx.eventBus().send("persistence", new JsonObject(), msgh -> {
	    if (msgh.failed()) {
		System.out.println(msgh.cause().getMessage());
	    }
	    ctx.assertTrue(msgh.succeeded());
	    JsonArray res = new JsonArray((String) msgh.result().body());
	    ctx.assertEquals(expected, Json.decodeValue(res.getJsonObject(0).toString(), Doc.class));
	    async.complete();
	});
	async.await();
    }
}
