package bsuapi.test;

import bsuapi.resource.Util;
import bsuapi.resource.Config;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.json.JSONObject;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJsonResource implements AutoCloseable
{
    protected JSONObject j;

    public TestJsonResource (String filename)
    {
        try {
            this.j = Util.readResourceJSON(filename);
        } catch (Exception e) {
            fail("Couldn't load "+filename+".json: "+e.getMessage());
        }
    }

    public JSONObject getDoc()
    {
        return this.j;
    }

    public Object query(String jsonPointer)
    {
        return j.query(jsonPointer);
    }

    public String strQuery(String jsonPointer)
    {
        return this.query(jsonPointer).toString();
    }

    public UriInfo mockUriInfo(String namedParamSet)
    {
        UriInfo m = mock(UriInfo.class);
        MultivaluedMap<String, String> map = this.buildParams(namedParamSet);

        when(m.getQueryParameters(true)).thenReturn(map);
        when(m.getBaseUri()).thenReturn(URI.create("https://"+ Config.get("domain")));

        return m;
    }

    private MultivaluedMap<String, String> buildParams(String namedParamSet)
    {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl();

        JSONObject params = (JSONObject) j.query("/querystring/" + namedParamSet);
        for (Iterator<String> it = params.keys(); it.hasNext(); ) {
            String key = it.next();
            String val = params.get(key).toString();
            List<String> list = new ArrayList<>();
            list.add(val);
            map.put(key, list);
        }

        return map;
    }

    @Override
    public void close()
    {
        this.j = null;
    }
}
