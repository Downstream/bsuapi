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

public class CypherScriptFile implements Runnable, ScriptStatus
{
    private static final long COMPLETED_LOCK_TIME = TimeUnit.MINUTES.toMillis(10);
    private Instant startTime;
    private Instant completeTime;
    private int countCompleted;
    private String filename;
    private ArrayList<CypherQuery> commands;
    private JSONArray results;
    private Cypher c;
    private Thread thread;
    private boolean halt;
    private boolean booting;

    public static CypherScriptFile go(CypherScript scriptFile)
    throws Exception
    {
        String filename = scriptFile.filename();
        ScriptStatus existing = ScriptOverseer.get(filename);
        if (existing instanceof CypherScriptFile) {
            return (CypherScriptFile) existing;
        }

        return new CypherScriptFile(scriptFile.filename());
    }

    public static CypherScriptFile go(String filename)
            throws Exception
    {
        ScriptStatus existing = ScriptOverseer.get(filename);
        if (existing instanceof CypherScriptFile) {
            return (CypherScriptFile) existing;
        }

        return new CypherScriptFile(filename);
    }

    private CypherScriptFile(String filename)
    throws Exception
    {
        this.countCompleted = 0;
        this.halt = false;
        this.booting = false;
        this.filename = filename;
        String sourceFileData = Util.readResourceFile(this.filename);

        this.commands = new ArrayList<>();
        this.results = new JSONArray();

        for (String cmd : sourceFileData.trim().split(";")) {
            cmd = cmd.trim();
            if (cmd.length() < 5) {continue;}
            this.commands.add(new CypherScriptFileCommand(cmd));
        }

        ScriptOverseer.ready(this.filename, this);
    }

    public void exec(Cypher c)
    throws CypherException, RuntimeException
    {
        this.start(c);
    }

    public void start(Cypher c)
    throws CypherException, RuntimeException
    {
        if (!this.isReady()) {
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" not in ready state. "+ this.stateHash());
        }

        if (this.isRunning()) {
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" while a matching Thread was still active. "+ this.stateHash());
        }

        if (c != null) {
            this.c = c;
        }

        if (this.c == null) {
            throw new CypherException("Missing Cypher context in "+ this.toString());
        }

        this.booting = true;
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run()
    throws RuntimeException
    {
        if (!this.booting && !this.isReady()) {
            RuntimeException e = new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" not in ready state. "+ this.stateHash());
            this.results.put(e);
            throw e;
        }

        if (!this.booting && this.isRunning()) {
            RuntimeException e = new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" while a matching Thread was still active. "+ this.stateHash());
            this.results.put(e);
            throw e;
        }

        this.booting = false;

        this.startTime = Instant.now();

        for (CypherQuery command : this.commands) {
            if (this.halt) {
                this.results.put("HALTED");
                ScriptOverseer.endIn(this.filename, CypherScriptFile.COMPLETED_LOCK_TIME);
                return;
            }

            try {
                this.results.put(Util.jsonArrayFirst(command.exec(this.c)));
                this.countCompleted++;
            } catch (CypherException e) {
                this.results.put(e.getCause().getMessage());
                this.results.put(e);
                return;
            }
        }

        this.completeTime = Instant.now();
        ScriptOverseer.endIn(this.filename, CypherScriptFile.COMPLETED_LOCK_TIME);
    }

    public String getFilename()
    {
        return this.filename;
    }

    @Override
    public boolean isReady()
    {
        return !this.halt && this.startTime == null && (this.thread == null || !this.thread.isAlive());
    }

    @Override
    public boolean isRunning()
    {
        return (null != this.startTime && this.thread.isAlive());
    }

    @Override
    public boolean isComplete()
    {
        return (null != this.startTime && null != this.completeTime);
    }

    public String stateHash() {
        StringBuilder b = new StringBuilder();
        b.append(this.isReady() ? '1' : '0');
        b.append(this.isRunning() ? '1' : '0');
        b.append(this.isComplete() ? '1' : '0');
        b.append(this.isFailed() ? '1' : '0');
        return b.toString();
    }

    public boolean isFailed()
    {
        return this.startTime != null && !this.isComplete() && (this.thread == null || !this.thread.isAlive());
    }

    @Override
    public int countActionsTotal()
    {
        return this.commands.size();
    }

    @Override
    public int countActionsComplete()
    {
        return this.countCompleted;
    }

    @Override
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

    @Override
    public void end()
    {
        this.startTime = null;
        this.completeTime = null;

        if (null != this.thread) {
            this.halt = true;
            return;
        }
    }

    @Override
    public String toString()
    {
        return "CypherScriptFile: " + this.filename;
    }

    @Override
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
            status.put("next", "Command available again "+ (CypherScriptFile.COMPLETED_LOCK_TIME/1000) +" seconds after it completed.");
        }

        if (this.isReady() || this.booting) {
            status.put("runTime", Util.durationDisplayFormat(Duration.ZERO));
            status.put("action", "starting");
        }

        if (this.isFailed()) {
            status.put("error", "An error occurred during execution. Check the results list for a detailed error message.");
            status.put("action", "failed");
            status.put("runTime", Util.durationDisplayFormat(ScriptOverseer.endIn(this.filename,CypherScriptFile.COMPLETED_LOCK_TIME)));
        }

        if (!status.has("action")) {
            status.put("action", "readying");
            status.put("next", "Will start once ready. If you're reading this, it's probably already running.");
        }

        return status;
    }
}
