package bsuapi.resource;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class Request
{
    private UriInfo uriInfo;
    private Map<String, String> params;

    public Request(UriInfo uriInfo)
    {
        this.uriInfo = uriInfo;
        this.params = this.flattenMultiMap(uriInfo.getQueryParameters(true));
    }

    protected Map<String, String> flattenMultiMap(MultivaluedMap<String, String> multi)
    {
        Map<String, String> map = new HashMap<>();
        for (String key : multi.keySet())
        {
            map.put(key, multi.getFirst(key));
        }

        return map;
    }

    public String getParam (String param)
    {
        if (!this.params.containsKey(param)) {
            return null;
        }

        return this.params.get(param);
    }

    public Map<String, String> getQueryParameters ()
    {
        return this.params;
    }

    public String getBaseUri()
    {
        return "https://" + Config.get("domain") + Config.get("baseuri");
    }
}
