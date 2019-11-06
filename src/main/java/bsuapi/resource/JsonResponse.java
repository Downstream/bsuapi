package bsuapi.resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.string.UTF8;
import javax.ws.rs.core.Response;

public class JsonResponse {
    public static Response SERVER_ERROR (JSONObject response)
    {
        return JsonResponse.build(ResponseStatus.INTERNAL_SERVER_ERROR, response);
    }

    public static Response NOT_ACCEPTABLE (JSONObject response)
    {
        return JsonResponse.build(ResponseStatus.NOT_ACCEPTABLE, response);
    }

    public static Response NOT_FOUND (JSONObject response)
    {
        return JsonResponse.build(ResponseStatus.NOT_FOUND, response);
    }

    public static Response NO_CONTENT (JSONObject response)
    {
        return JsonResponse.build(ResponseStatus.NO_CONTENT, response);
    }

    public static Response NOT_IMPLEMENTED (JSONObject response)
    {
        return JsonResponse.build(ResponseStatus.NOT_IMPLEMENTED, response);
    }

    public static Response OK (JSONObject response)
    {
        return JsonResponse.build(ResponseStatus.OK, response);
    }

    public static JSONObject responseObject(Boolean success, String message)
    {
        JSONObject res = new JSONObject();
        res.put("success", success);
        res.put("message", message);
        return res;
    }

    private static Response build(ResponseStatus status, JSONObject response)
    {
        return Response.status( status ).entity( UTF8.encode(response.toString(4)) ).build();
    }

    public static JSONObject exceptionStack(Exception e)
    {
        JSONObject result = new JSONObject();
        JSONArray stack;
        Throwable cause = e;
        do {
            stack = new JSONArray();
            for(StackTraceElement trace : cause.getStackTrace()) {
                stack.put(trace.getFileName() +"["+ trace.getLineNumber() +"] "+ trace.getClass().getSimpleName() +"."+ trace.getMethodName() +"()");
            }
            result.put(cause.getClass().getSimpleName(), stack);
        } while(null != (cause = e.getCause()));

        return result;
    }
}
