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

public class CypherScriptFile implements GenerationStatus
{
    private static final int COMPLETED_LOCK_TIME = 60000;
    private Instant startTime;
    private int countCompleted;
    private String sourceFilename;
    private ArrayList<CypherQuery> commands;
    private JSONArray results;

    public static CypherScriptFile go(CypherScript scriptFile)
    throws Exception
    {
        return CypherScriptFile.go(scriptFile.filename());
    }

    public static CypherScriptFile go(String filename)
    throws Exception
    {
        GenerationStatus existing = GenerationOverseer.get(filename);
        if (existing instanceof CypherScriptFile) {
            return (CypherScriptFile) existing;
        }

        return new CypherScriptFile(filename);
    }

    public CypherScriptFile(String filename)
    throws Exception
    {
        this.countCompleted = 0;
        this.sourceFilename = filename;
        String sourceFileData = Util.readResourceFile(this.sourceFilename);

        this.commands = new ArrayList<>();
        this.results = new JSONArray();

        for (String cmd : sourceFileData.trim().split(";")) {
            cmd = cmd.trim();
            if (cmd.length() < 5) {continue;}
            this.commands.add(new CypherScriptFileCommand(cmd));
        }
    }

    public JSONObject exec(Cypher c)
    throws CypherException
    {
        this.startTime = Instant.now();
        GenerationOverseer.start(this.sourceFilename, this);

        for (CypherQuery command : this.commands) {
            this.results.put(Util.jsonArrayFirst(command.exec(c)));
            this.countCompleted++;
        }

        GenerationOverseer.endIn(this.sourceFilename, CypherScriptFile.COMPLETED_LOCK_TIME);

        return this.statusReport();
    }

    @Override
    public boolean isRunning() {
        return (null != this.startTime);
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
            status.put("runTime", this.runtime());
            if (this.countActionsComplete() < this.countActionsTotal()) {
                status.put("action", "running");
            } else {
                status.put("action", "completed");
                status.put("next", "Command available again "+ (CypherScriptFile.COMPLETED_LOCK_TIME/1000) +" seconds after it completed.");
            }
        } else {
            status.put("runTime", 0);
            status.put("action", "starting");
        }

        return status;
    }
}
