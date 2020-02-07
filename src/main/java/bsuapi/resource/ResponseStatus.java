package bsuapi.resource;

import javax.ws.rs.core.Response;

// simplify older javax status implementation, and add missing needed codes
public enum ResponseStatus implements Response.StatusType
{
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable")
    ;

    private final int code;
    private final String reason;
    private Response.Status.Family family;

    ResponseStatus(final int statusCode, final String reasonPhrase)
    {
        this.code = statusCode;
        this.reason = reasonPhrase;
        switch(code/100) {
            case 1: this.family = Response.Status.Family.INFORMATIONAL; break;
            case 2: this.family = Response.Status.Family.SUCCESSFUL; break;
            case 3: this.family = Response.Status.Family.REDIRECTION; break;
            case 4: this.family = Response.Status.Family.CLIENT_ERROR; break;
            case 5: this.family = Response.Status.Family.SERVER_ERROR; break;
            default: this.family = Response.Status.Family.OTHER; break;
        }
    }

    @Override
    public int getStatusCode()
    {
        return code;
    }

    @Override
    public Response.Status.Family getFamily()
    {
        return family;
    }

    @Override
    public String getReasonPhrase()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return reason;
    }
}
