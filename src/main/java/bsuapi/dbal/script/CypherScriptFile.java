package bsuapi.dbal.script;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.GenerationStatus;
import bsuapi.resource.Util;
import bsuapi.service.GenerationOverseer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class CypherScriptFile implements Runnable, GenerationStatus
{
    private static final int COMPLETED_LOCK_TIME = 60000;
    private Instant startTime;
    private Instant completeTime;
    private int countCompleted;
    private String sourceFilename;
    private ArrayList<CypherQuery> commands;
    private JSONArray results;
    private Cypher c;
    private Thread thread;

    public static CypherScriptFile go(String filename)
    throws Exception
    {
        GenerationStatus existing = GenerationOverseer.get(filename);
        if (existing instanceof CypherScriptFile) {
            return (CypherScriptFile) existing;
        }

        return new CypherScriptFile(filename);
    }

    private CypherScriptFile(String filename)
    throws Exception
    {
        this.countCompleted = 0;
        this.sourceFilename = filename;
        String sourceFileData = Util.readResourceFile(this.sourceFilename);

        this.commands = new ArrayList<>();
        this.results = new JSONArray();

        for (String cmd : sourceFileData.trim().split(";")) {
            if (cmd.trim().length() < 5) {continue;}
            this.commands.add(new CypherScriptFileCommand(cmd));
        }

        GenerationOverseer.start(this.sourceFilename, this);
    }

    public void start(Cypher c)
    throws CypherException, RuntimeException
    {
        if (this.thread != null && this.thread.isAlive()) {
            throw new RuntimeException("Attempted to start a new Thread for "+ this.toString() +" while a matching Thread was still active.");
        }

        if (c != null) {
            this.c = c;
        }

        if (this.c == null) {
            throw new CypherException("Missing Cypher context in "+ this.toString());
        }

        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {
        this.startTime = Instant.now();

        for (CypherQuery command : this.commands) {
            try {
                this.results.put(Util.jsonArrayFirst(command.exec(this.c)));
                this.countCompleted++;
            } catch (CypherException e) {
                this.results.put(e);
                return;
            }
        }

        this.completeTime = Instant.now();
        GenerationOverseer.endIn(this.sourceFilename, CypherScriptFile.COMPLETED_LOCK_TIME);
    }

    public void exec(Cypher c)
    throws CypherException
    {
        this.start(c);
    }

    @Override
    public boolean isReady() {
        return (this.thread == null || !this.thread.isAlive());
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
            return null;
        }

        if (this.completeTime != null) {
            return Duration.between(this.startTime, this.completeTime);
        }

        return Duration.between(this.startTime, Instant.now());
    }

    @Override
    public String toString() {
        return "CypherScriptFile: " + this.sourceFilename;
    }

    @Override
    public JSONObject statusReport() {
        JSONObject status = new JSONObject();
        status.put("script", this.toString());
        status.put("countActionsTotal", this.countActionsTotal());
        status.put("countActionsComplete", this.countActionsComplete());
        status.put("results", this.results);

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
            status.put("runTime", Util.durationDisplayFormat(Duration.ofMillis(0)));
            status.put("action", "starting");
        }

        if (this.startTime != null && !this.isComplete() && (this.thread == null || !this.thread.isAlive())) {
            status.put("error", "An error occurred during execution. Check the results list for a detailed error message.");
            status.put("action", "failed");
            status.put("runTime", Util.durationDisplayFormat(GenerationOverseer.end(this.sourceFilename)));
        }

        return status;
    }
}
