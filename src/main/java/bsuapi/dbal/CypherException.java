package bsuapi.dbal;

public class CypherException extends Exception
{
    public CypherException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CypherException(String message)
    {
        super(message);
    }
}
