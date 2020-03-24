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

public class CypherScriptFile implements Runnable, ScriptStatus
{
    private static final int COMPLETED_LOCK_TIME = 60000;
    private Instant startTime;
    private Instant completeTime;
    private int countCompleted;
    private String sourceFilename;
    private CypherScript scriptFile;
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

        return new CypherScriptFile(scriptFile);
    }

    private CypherScriptFile(CypherScript scriptFile)
    throws Exception
    {
        this.countCompleted = 0;
        this.halt = false;
        this.booting = false;
        this.scriptFile = scriptFile;
        String sourceFileData = Util.readResourceFile(this.scriptFile.filename());

        this.commands = new ArrayList<>();
        this.results = new JSONArray();

        for (String cmd : sourceFileData.trim().split(";")) {
            if (cmd.trim().length() < 5) {continue;}
            this.commands.add(new CypherScriptFileCommand(cmd));
        }

        ScriptOverseer.ready(this.scriptFile.filename(), this);
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
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" not in ready state.");
        }

        if (this.isRunning()) {
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" while a matching Thread was still active.");
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
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" not in ready state.");
        }

        if (!this.booting && this.isRunning()) {
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" while a matching Thread was still active.");
        }

        this.booting = false;

        this.startTime = Instant.now();

        for (CypherQuery command : this.commands) {
            if (this.halt) {
                ScriptOverseer.endIn(this.scriptFile.filename(), CypherScriptFile.COMPLETED_LOCK_TIME);
                return;
            }

            try {
                this.results.put(Util.jsonArrayFirst(command.exec(this.c)));
                this.countCompleted++;
            } catch (CypherException e) {
                this.results.put(e);
                return;
            }
        }

        this.completeTime = Instant.now();
        ScriptOverseer.endIn(this.scriptFile.filename(), CypherScriptFile.COMPLETED_LOCK_TIME);
    }

    @Override
    public boolean isReady() {
        return !this.halt && this.startTime == null && (this.thread == null || !this.thread.isAlive());
    }

    @Override
    public boolean isRunning() {
        return (null != this.startTime && this.thread.isAlive());
    }

    @Override
    public boolean isComplete() {
        return (null != this.startTime && null != this.completeTime);
    }

    @Override
    public int countActionsTotal() {
        return this.commands.size();
    }

    @Override
    public int countActionsComplete() {
        return this.countCompleted;
    }

    @Override
    public Duration runtime() {
        if (this.startTime == null) {
            return Duration.ZERO;
        }

        if (this.completeTime != null) {
            return Duration.between(this.startTime, this.completeTime);
        }

        return Duration.between(this.startTime, Instant.now());
    }

    @Override
    public void end() {
        this.startTime = null;
        this.completeTime = null;

        if (null != this.thread) {
            this.halt = true;
            return;
        }
    }

    @Override
    public String toString() {
        return "CypherScriptFile: " + this.scriptFile.filename();
    }

    @Override
    public JSONObject statusReport() {
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

        if (this.isReady()) {
            status.put("runTime", Util.durationDisplayFormat(Duration.ZERO));
            status.put("action", "starting");
        }

        if (this.startTime != null && !this.isComplete() && (this.thread == null || !this.thread.isAlive())) {
            status.put("error", "An error occurred during execution. Check the results list for a detailed error message.");
            status.put("action", "failed");
            status.put("runTime", Util.durationDisplayFormat(ScriptOverseer.end(this.scriptFile.filename())));
        }

        return status;
    }
}
