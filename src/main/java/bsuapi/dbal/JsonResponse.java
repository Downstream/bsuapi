package bsuapi.dbal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.string.UTF8;
import javax.ws.rs.core.Response;

public class JsonResponse {
    public static Response SERVER_ERROR (JSONObject response)
    {
        return JsonResponse.build(Response.Status.INTERNAL_SERVER_ERROR, response);
    }

    public static Response NOT_ACCEPTABLE (JSONObject response)
    {
        return JsonResponse.build(Response.Status.NOT_ACCEPTABLE, response);
    }

    public static Response NOT_FOUND (JSONObject response)
    {
        return JsonResponse.build(Response.Status.NOT_FOUND, response);
    }

    public static Response OK (JSONObject response)
    {
        return JsonResponse.build(Response.Status.OK, response);
    }

    public static Response data (JSONObject data, String message)
    {
        JSONObject res = JsonResponse.responseObject(true, message);
        res.put("data", data);
        return JsonResponse.OK(res);
    }

    public static Response exception (Exception e)
    {
        JSONObject res = JsonResponse.responseObject(false, e.getMessage());
        res.put("data", e.toString());
        res.put("stack", JsonResponse.exceptionStack(e));
        return JsonResponse.SERVER_ERROR(res);
    }

    public static Response badRequest (String reason)
    {
        JSONObject res = JsonResponse.responseObject(false, reason);
        return JsonResponse.NOT_ACCEPTABLE(res);
    }

    public static Response notFound (String message)
    {
        JSONObject res = JsonResponse.responseObject(false, message);
        return JsonResponse.NOT_FOUND(res);
    }

    public static Response notFound ()
    {
        return JsonResponse.notFound("Requested resource not found. No matching indexed node for that value. Isn't art an emotional abstraction? Just imagine it.");
    }

    private static JSONObject responseObject(Boolean success, String message)
    {
        JSONObject res = new JSONObject();
        res.put("success", success);
        res.put("message", message);
        return res;
    }

    private static Response build(Response.Status status, JSONObject response)
    {
        return Response.status( status ).entity( UTF8.encode(response.toString()) ).build();
    }

    private static JSONObject exceptionStack(Exception e)
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
