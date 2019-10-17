package bsuapi.resource;

import bsuapi.test.BaseJsonTest;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RequestTest extends BaseJsonTest {

    @BeforeClass
    public static void setUp()
    {
        BaseJsonTest.preLoadJsonResource("requestTestParams");
    }

    @Test
    public void testRequestEmptyParams()
    {
        Request request = new Request(this.mockUriInfo("empty"));

        assertNull(request.getParam("anything"));
        assertNull(request.getParam("requestToken"));
    }

    @Test
    public void testRequestToken()
    {
        Request request = new Request(this.mockUriInfo("tokenPlain"));

        assertEquals(request.getParam("requestToken"), "mytoken");
    }

    @Test
    public void testRequestComplexToken()
    {
        Request request = new Request(this.mockUriInfo("tokenComplex"));

        assertEquals(request.getParam("requestToken"), "XI+zZ/RV#aO Ze8B!@w1 AV8)bP\\I1(gdPPz7 z2g:j3; I F00*25o1$YWI ~s6u1`FBK&KT% axc3aR Ko5#TF5=V");
    }

    @Test
    public void testRequestGetQueryParametersEmpty()
    {
        Request request = new Request(this.mockUriInfo("empty"));
        Map params = request.getQueryParameters();

        assertNull(request.getParam("anything"));
        assertNull(request.getParam("requestToken"));
        assertEquals(params.size(), 0);
    }

    @Test
    public void testRequestGetQueryParametersMixed()
    {
        Request request = new Request(this.mockUriInfo("mixed"));
        Map params = request.getQueryParameters();

        assertEquals(params.get("a"), "123");
        assertEquals(params.get("b"), "abc");
        assertEquals(params.size(), 2);
    }

    @Test
    public void testRequestGetBaseUri()
    {
        Request request = new Request(this.mockUriInfo("empty"));

        assertEquals(request.getBaseUri(), "https://bsu.downstreamlabs.com");
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
        MultivaluedMap<String, String> map = (MultivaluedMap<String, String>) mock(MultivaluedMap.class);
        Set<String> keys = new HashSet<>();

        JSONObject params = (JSONObject) this.query("/querystring/" + namedParamSet);
        for (Iterator<String> it = params.keys(); it.hasNext(); ) {
            String key = it.next();
            String val = params.get(key).toString();

            keys.add(key);
            when(map.getFirst(key)).thenReturn(val);
        }

        when(map.keySet()).thenReturn(keys);

        return map;
    }
}
