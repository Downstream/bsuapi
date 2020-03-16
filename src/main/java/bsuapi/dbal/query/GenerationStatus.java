package bsuapi.dbal.query;

import org.json.JSONObject;

import java.time.Duration;

public interface GenerationStatus {
    public boolean isRunning();
    public int countActionsTotal();
    public int countActionsComplete();
    public Duration runtime();
    public String toString();
    public JSONObject statusReport();
}
