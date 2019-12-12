package bsuapi.resource;

import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.UriInfo;

public class RootResourceTest
{
    protected static TestCypherResource db;
    protected static TestJsonResource j;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        j = new TestJsonResource("requestTestParams");
    }

    @AfterClass
    public static void tearDown()
    {
        db.close();
        j.close();
    }

    @Test
    public void integrationTestHome() {
        UriInfo uriInfo = j.mockUriInfo("rootFilter");
        RootResource resource = new RootResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.home(uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertFalse(responseData.query("/title").toString().isEmpty());
        assertFalse(responseData.query("/summary").toString().isEmpty());
        assertNotNull(responseData.query("/schema"));
        assertNotNull(responseData.query("/topics/Artist"));
    }

    @Test
    public void integrationTestEmptyFilter() {
        UriInfo uriInfo = j.mockUriInfo("rootFilterEmpty");
        RootResource resource = new RootResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.home(uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertFalse(responseData.query("/title").toString().isEmpty());
        assertFalse(responseData.query("/summary").toString().isEmpty());
        assertNotNull(responseData.query("/schema"));
        assertNotNull(responseData.query("/topics/Artist"));
    }

    @Test
    public void integrationTestHomeMethodsList() {
        UriInfo uriInfo = j.mockUriInfo("tokenPlain");
        RootResource resource = new RootResource();
        javax.ws.rs.core.Response result = resource.home(uriInfo);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));
        JSONObject methods = (JSONObject) responseData.query("/methods");

        assertNotNull(responseData.query("/methods/root"));
        assertNotNull(responseData.query("/methods/related"));
        assertNotNull(responseData.query("/methods/topic-assets"));
        assertNotNull(responseData.query("/methods/search"));
    }
}
