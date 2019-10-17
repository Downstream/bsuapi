package bsuapi.resource;

import bsuapi.behavior.Behavior;
import bsuapi.test.BaseJsonTest;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResponseTest extends BaseJsonTest {
    @BeforeClass
    public static void setUp() {
        BaseJsonTest.preLoadJsonResource("responseTestParams");
    }

    @Test
    public void integrationTestResponsePlainGeneric() {
        Response response = this.prepareResponse("tokenPlain");
        JSONObject preData = (JSONObject) this.query("/responseObject/plainGeneric");

        javax.ws.rs.core.Response result = response.plain(preData);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/str").toString(), "should handle anything");
        assertEquals(responseData.query("/obj/name").toString(), "value");
        assertEquals(responseData.query("/array/0"), 1);
        assertEquals(responseData.query("/array/1"), 2);
        assertEquals(responseData.query("/array/2"), 3);
    }

    @Test
    public void integrationTestResponseDataGeneric() {
        Response response = this.prepareResponse("tokenPlain");
        JSONObject preData = (JSONObject) this.query("/responseObject/plainGeneric");

        javax.ws.rs.core.Response result = response.data(preData, "Response Message");

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), true);
        assertEquals(responseData.query("/message").toString(), "Response Message");
        assertEquals(responseData.query("/data/str").toString(), "should handle anything");
        assertEquals(responseData.query("/data/obj/name").toString(), "value");
        assertEquals(responseData.query("/data/array/0"), 1);
        assertEquals(responseData.query("/data/array/1"), 2);
        assertEquals(responseData.query("/data/array/2"), 3);
    }

    @Test
    public void integrationTestResponseBadRequest() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.badRequest("Response Message");

        assertEquals(result.getStatus(), 406);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), false);
        assertEquals(responseData.query("/message").toString(), "Response Message");
    }

    @Test
    public void integrationTestResponseNotFoundMessage() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.notFound("Response Message");

        assertEquals(result.getStatus(), 404);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), false);
        assertEquals(responseData.query("/message").toString(), "Response Message");
    }

    @Test
    public void integrationTestResponseNotFound() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.notFound();

        assertEquals(result.getStatus(), 404);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), false);
        assertFalse(responseData.query("/message").toString().isEmpty());
    }

    @Test
    public void integrationTestResponseException() {
        Response response = this.prepareResponse("tokenPlain");

        Exception e = new Exception("Some Exception");

        javax.ws.rs.core.Response result = response.exception(e);

        assertEquals(result.getStatus(), 500);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), false);
        assertEquals(responseData.query("/message").toString(), "Some Exception");
        assertFalse(responseData.query("/data").toString().isEmpty());
        assertNotNull(responseData.query("/stack"));
    }

    @Test
    public void integrationTestResponseBehaviorNull() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.behavior(null);

        assertEquals(result.getStatus(), 404);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), false);
        assertFalse(responseData.query("/message").toString().isEmpty());
    }

    @Test
    public void integrationTestResponseBehavior() {
        Response response = this.prepareResponse("tokenPlain");

        JSONObject behaviorData = (JSONObject) this.query("/responseObject/plainGeneric");
        Behavior behavior = mock(Behavior.class);
        when(behavior.toJson()).thenReturn(behaviorData);
        when(behavior.getMessage()).thenReturn("Some Response Message");

        javax.ws.rs.core.Response result = response.behavior(behavior);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertEquals(responseData.query("/success"), true);
        assertEquals(responseData.query("/message").toString(), "Some Response Message");
        assertEquals(responseData.query("/data/str").toString(), "should handle anything");
        assertEquals(responseData.query("/data/obj/name").toString(), "value");
        assertEquals(responseData.query("/data/array/0"), 1);
        assertEquals(responseData.query("/data/array/1"), 2);
        assertEquals(responseData.query("/data/array/2"), 3);
    }

    @Test
    public void integrationTestResponseBuildUri() {
        Response response = this.prepareResponse("tokenPlain");

        assertEquals(response.buildUri("/any/path/at/all"), "https://bsu.downstreamlabs.com/bsuapi/any/path/at/all");
    }

    private Response prepareResponse(String namedParamSet)
    {
        Request request = this.mockRequest(namedParamSet);
        Response response = Response.prepare(request);

        verify(request).getQueryParameters();

        return response;
    }

    private Request mockRequest(String namedParamSet)
    {
        Request request = mock(Request.class);
        Map<String, String> map = this.buildParams(namedParamSet);

        when(request.getQueryParameters()).thenReturn(map);
        when(request.getBaseUri()).thenReturn("https://bsu.downstreamlabs.com");

        return request;
    }

    private Map<String, String> buildParams(String namedParamSet)
    {
        Map<String, String> map = (Map<String, String>) mock(Map.class);

        JSONObject params = (JSONObject) this.query("/querystring/" + namedParamSet);
        for (Iterator<String> it = params.keys(); it.hasNext(); ) {
            String key = it.next();
            String val = params.get(key).toString();

            when(map.getOrDefault(key, null)).thenReturn(val);
        }

        return map;
    }
}
