package bsuapi.resource;

import bsuapi.test.BaseJsonTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RootResourceTest extends BaseJsonTest {
    @BeforeClass
    public static void setUp() {
        BaseJsonTest.preLoadJsonResource("requestTestParams");
    }

    @Test
    public void integrationTestHome() {
        UriInfo uriInfo = this.mockUriInfo("tokenPlain");
        RootResource resource = new RootResource();
        javax.ws.rs.core.Response result = resource.home(uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertEquals(responseData.get("requestToken").toString(), "mytoken");
        assertFalse(responseData.query("/title").toString().isEmpty());
        assertFalse(responseData.query("/summary").toString().isEmpty());
        assertNotNull(responseData.query("/schema"));
        assertNotNull(responseData.query("/topics"));
    }

    @Test
    public void integrationTestHomeMethodsList() {
        UriInfo uriInfo = this.mockUriInfo("tokenPlain");
        RootResource resource = new RootResource();
        javax.ws.rs.core.Response result = resource.home(uriInfo);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));
        JSONArray methods = (JSONArray) responseData.query("/methods");

        assertTrue(methods.length() >= 2);
    }

    private UriInfo mockUriInfo(String namedParamSet)
    {
        UriInfo m = mock(UriInfo.class);
        MultivaluedMap<String, String> map = this.buildParams(namedParamSet);

        when(m.getQueryParameters(true)).thenReturn(map);
        when(m.getBaseUri()).thenReturn(URI.create("https://bsu.downstreamlabs.com"));

        return m;
    }

    private MultivaluedMap<String, String> buildParams(String namedParamSet)
    {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();

        JSONObject params = (JSONObject) this.query("/querystring/" + namedParamSet);
        for (Iterator<String> it = params.keys(); it.hasNext(); ) {
            String key = it.next();
            String val = params.get(key).toString();
            List<String> list = new ArrayList<>();
            list.add(val);
            map.put(key, list);
        }

        return map;
    }
}
