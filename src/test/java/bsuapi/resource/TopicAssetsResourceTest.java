package bsuapi.resource;

import bsuapi.dbal.NodeType;
import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class TopicAssetsResourceTest
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
        UriInfo uriInfo = j.mockUriInfo("tokenPlain");
        TopicAssetsResource resource = new TopicAssetsResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.apiAssets(NodeType.ARTIST.labelName(), "Edgar Degas", uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals("mytoken", responseData.get("requestToken").toString());
        assertNotNull(responseData.query("/data/assets/0"));
        assertEquals("Edgar+Degas", responseData.query("/data/node/keyEncoded").toString());
        assertEquals("334323", responseData.query("/data/assets/0/id").toString());
    }
}
