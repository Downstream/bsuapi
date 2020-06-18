package bsuapi.dbal.script;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.resource.Util;
import bsuapi.service.ScriptOverseer;
import bsuapi.service.ScriptStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

abstract public class CypherScriptAbstract implements ScriptStatus, Runnable
{
    protected static final long COMPLETED_LOCK_TIME = TimeUnit.MINUTES.toMillis(10);
    protected Instant startTime;
    protected Instant completeTime;
    protected CypherScript script;
    protected ArrayList<CypherQuery> commands;
    protected JSONArray results;
    protected Cypher c;
    protected int countCompleted = 0;
    protected boolean halt = false;
    protected boolean booting = true;
    protected boolean failed = false;

    abstract public void handleCommandResult(CypherQuery command) throws CypherException;
    abstract protected ArrayList<CypherQuery> commandLoader() throws Exception;

    protected void boot(Cypher c, CypherScript script)
    {
        this.c = c;
        this.script = script;
        this.commands = new ArrayList<>();
        this.results = new JSONArray();

        try {
            this.commands = this.commandLoader();
        } catch (Exception e) {
            this.booting = false;
            this.failed = true;
            this.results.put(e);
        }
    }

    public void run()
    {
        if (!this.booting && !this.isReady()) {
            RuntimeException e = new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" not in ready state. "+ this.stateHash());
            this.results.put(e);
            this.failed = true;
            return;
        }

        if (!this.booting && this.isRunning()) {
            RuntimeException e = new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" while a matching Thread was still active. "+ this.stateHash());
            this.results.put(e);
            this.failed = true;
            return;
        }

        this.booting = false;

        this.startTime = Instant.now();

        for (CypherQuery command : this.commands) {
            if (this.halt) {
                this.results.put("HALTED");
                ScriptOverseer.endIn(this.script.name(), CypherScriptFile.COMPLETED_LOCK_TIME);
                return;
            }

            try {
                this.handleCommandResult(command);
            } catch (CypherException e) {
                this.results.put(e.getCause().getMessage());
                this.results.put(e);
                this.failed = true;
            }
        }

        this.completeTime = Instant.now();
        ScriptOverseer.endIn(this.script.name(), CypherScriptFile.COMPLETED_LOCK_TIME);
    }

    public boolean isReady(){ return !this.halt && this.startTime == null; }

    public boolean isRunning() { return null != this.startTime; }

    public boolean isComplete() { return (null != this.startTime && null != this.completeTime); }

    public boolean isFailed() { return this.failed; }

    public int countActionsTotal() { return this.commands.size(); }

    public int countActionsComplete() { return this.countCompleted; }

    public String toString() { return "CypherScriptFile: " + this.script.name(); }

    public CypherScript getScript() { return this.script; }

    public String stateHash() {
        return String.valueOf
            ( this.isReady() ? '1' : '0') +
            ( this.isRunning() ? '1' : '0') +
            ( this.isComplete() ? '1' : '0') +
            ( this.isFailed() ? '1' : '0')
        ;
    }

    public Duration runtime()
    {
        if (this.startTime == null) {
            return Duration.ZERO;
        }

        if (this.completeTime != null) {
            return Duration.between(this.startTime, this.completeTime);
        }

        return Duration.between(this.startTime, Instant.now());
    }

    public void end()
    {
        this.startTime = null;
        this.completeTime = null;
    }

    public JSONObject statusReport()
    {
        JSONObject status = new JSONObject();
        status.put("script", this.toString());
        status.put("countActionsTotal", this.countActionsTotal());
        status.put("countActionsComplete", this.countActionsComplete());
        status.put("results", this.results);

        status.put("ready", this.isReady());
        status.put("running", this.isRunning());
        status.put("complete", this.isComplete());

        if (this.isRunning()) {
            status.put("runTime", Util.durationDisplayFormat(this.runtime()));
            status.put("action", "running");
        }

        if (this.isComplete()) {
            status.put("runTime", Util.durationDisplayFormat(this.runtime()));
            status.put("action", "completed");
            status.put("next", "Command available again "+ (CypherScriptAbstract.COMPLETED_LOCK_TIME/1000) +" seconds after it completed.");
        }

        if (this.isReady() || this.booting) {
            status.put("runTime", Util.durationDisplayFormat(Duration.ZERO));
            status.put("action", "starting");
        }

        if (this.isFailed()) {
            status.put("error", "An error occurred during execution. Check the results list for a detailed error message. "+ this.stateHash());
            status.put("action", "failed");
            status.put("runTime", Util.durationDisplayFormat(ScriptOverseer.endIn(this.script.name(), CypherScriptAbstract.COMPLETED_LOCK_TIME)));
        }

        if (!status.has("action")) {
            status.put("action", "readying");
            status.put("next", "Will start once ready. If you're reading this, it's probably already running.");
        }

        return status;
    }
}
