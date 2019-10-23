package bsuapi.resource;

import bsuapi.dbal.NodeType;
import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import static org.junit.Assert.*;

import javax.ws.rs.core.UriInfo;

public class RelatedResourceTest
{
    protected static TestJsonResource j;
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        j = new TestJsonResource("requestTestParams");
    }

    @AfterClass
    public static void tearDown() {
        db.close();
        j.close();
    }

    @Test
    public void integrationTestLimit() {
        UriInfo uriInfo = j.mockUriInfo("pageAndLimit");
        RelatedResource resource = new RelatedResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.apiRelated(NodeType.ARTIST.labelName(), "Edgar Degas", uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertNotNull(responseData.query("/data/related/Tag"));
        assertNull(responseData.query("/data/related/Classification"));
        assertEquals("Artist", responseData.query("/data/topic").toString());

    }
}
