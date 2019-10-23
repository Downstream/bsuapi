package bsuapi.resource;

import bsuapi.test.TestJsonResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class RequestTest {
    protected static TestJsonResource j;

    @BeforeClass
    public static void setUp() {
        j = new TestJsonResource("requestTestParams");
    }

    @AfterClass
    public static void tearDown()
    {
        j.close();
    }

    @Test
    public void testRequestEmptyParams()
    {
        Request request = new Request(j.mockUriInfo("empty"));

        assertNull(request.getParam("anything"));
        assertNull(request.getParam("requestToken"));
    }

    @Test
    public void testRequestToken()
    {
        Request request = new Request(j.mockUriInfo("tokenPlain"));

        assertEquals("mytoken", request.getParam("requestToken"));
    }

    @Test
    public void testRequestComplexToken()
    {
        Request request = new Request(j.mockUriInfo("tokenComplex"));

        assertEquals("XI+zZ/RV#aO Ze8B!@w1 AV8)bP\\I1(gdPPz7 z2g:j3; I F00*25o1$YWI ~s6u1`FBK&KT% axc3aR Ko5#TF5=V", request.getParam("requestToken"));
    }

    @Test
    public void testRequestGetQueryParametersEmpty()
    {
        Request request = new Request(j.mockUriInfo("empty"));
        Map params = request.getQueryParameters();

        assertNull(request.getParam("anything"));
        assertNull(request.getParam("requestToken"));
        assertEquals(0, params.size());
    }

    @Test
    public void testRequestGetQueryParametersMixed()
    {
        Request request = new Request(j.mockUriInfo("mixed"));
        Map params = request.getQueryParameters();

        assertEquals("123", params.get("a"));
        assertEquals("abc", params.get("b"));
        assertEquals(2, params.size());
    }

    @Test
    public void testRequestGetBaseUri()
    {
        Request request = new Request(j.mockUriInfo("empty"));

        assertEquals("https://" + Config.get("domain") + Config.get("baseuri"), request.getBaseUri());
    }
}
