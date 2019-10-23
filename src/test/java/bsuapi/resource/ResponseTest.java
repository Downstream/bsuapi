package bsuapi.resource;

import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResponseTest
{
    protected static TestJsonResource j;

    @BeforeClass
    public static void setUp() {
        j = new TestJsonResource("responseTestParams");
    }

    @AfterClass
    public static void tearDown()
    {
        j.close();
    }

    @Test
    public void integrationTestResponsePlainGeneric() {
        Response response = this.prepareResponse("tokenPlain");
        JSONObject preData = (JSONObject) j.query("/responseObject/plainGeneric");

        javax.ws.rs.core.Response result = response.plain(preData);

        assertEquals(200, result.getStatus());

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertEquals("should handle anything", responseData.query("/str").toString());
        assertEquals("value", responseData.query("/obj/name").toString());
        assertEquals(1, responseData.query("/array/0"));
        assertEquals(2,responseData.query("/array/1"));
        assertEquals(3, responseData.query("/array/2"));
    }

    @Test
    public void integrationTestResponseDataGeneric() {
        Response response = this.prepareResponse("tokenPlain");
        JSONObject preData = (JSONObject) j.query("/responseObject/plainGeneric");

        javax.ws.rs.core.Response result = response.data(preData, "Response Message");

        assertEquals(200, result.getStatus());

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertTrue((Boolean) responseData.query("/success"));
        assertEquals("Response Message",responseData.query("/message").toString());
        assertEquals("should handle anything", responseData.query("/data/str").toString());
        assertEquals("value", responseData.query("/data/obj/name").toString());
        assertEquals(1, responseData.query("/data/array/0"));
        assertEquals(2, responseData.query("/data/array/1"));
        assertEquals(3, responseData.query("/data/array/2"));
    }

    @Test
    public void integrationTestResponseBadRequest() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.badRequest("Response Message");

        assertEquals(406, result.getStatus());

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertFalse((Boolean) responseData.query("/success"));
        assertEquals("Response Message", responseData.query("/message").toString());
    }

    @Test
    public void integrationTestResponseNotFoundMessage() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.notFound("Response Message");

        assertEquals(404, result.getStatus());

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertFalse((Boolean) responseData.query("/success"));
        assertEquals("Response Message", responseData.query("/message").toString());
    }

    @Test
    public void integrationTestResponseNotFound() {
        Response response = this.prepareResponse("tokenPlain");

        javax.ws.rs.core.Response result = response.notFound();

        assertEquals(404, result.getStatus());

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertFalse((Boolean) responseData.query("/success"));
        assertFalse(responseData.query("/message").toString().isEmpty());
    }

    @Test
    public void integrationTestResponseException() {
        Response response = this.prepareResponse("tokenPlain");

        Exception e = new Exception("Some Exception");

        javax.ws.rs.core.Response result = response.exception(e);

        assertEquals(500, result.getStatus());

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertFalse((Boolean) responseData.query("/success"));
        assertEquals("Some Exception", responseData.query("/message").toString());
        assertFalse(responseData.query("/data").toString().isEmpty());
        assertNotNull(responseData.query("/stack"));
    }

    @Test
    public void integrationTestResponseBuildUri() {
        Response response = this.prepareResponse("tokenPlain");

        assertEquals("https://bsu.downstreamlabs.com/bsuapi/any/path/at/all", response.buildUri("/any/path/at/all"));
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
        Map<String, String> map = new HashMap<>();

        JSONObject params = (JSONObject) j.query("/querystring/" + namedParamSet);
        for (Iterator<String> it = params.keys(); it.hasNext(); ) {
            String key = it.next();
            String val = params.get(key).toString();
            map.put(key, val);
        }

        return map;
    }
}
