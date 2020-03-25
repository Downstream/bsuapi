package bsuapi.service;

import java.time.Duration;
import java.util.HashMap;

public class ScriptOverseer
{
    private static ScriptOverseer instance;
    private HashMap<String, ScriptStatus> runningCommands;

    private ScriptOverseer() //singleton
    {
        this.runningCommands = new HashMap<>();
    }

    private static ScriptOverseer singleton()
    {
        if (null == ScriptOverseer.instance) {
            ScriptOverseer.instance = new ScriptOverseer();
        }

        return ScriptOverseer.instance;
    }

    private ScriptStatus getCommand(String key)
    {
        return this.runningCommands.get(key);
    }

    private Duration getCommandRuntime(String key)
    {
        ScriptStatus command = this.getCommand(key);
        if (null == command) {
            return null;
        }

        return command.runtime();
    }

    private void readyCommand(String key, ScriptStatus command)
    throws IllegalStateException
    {
        if (null != this.getCommand(key)) {
            throw new IllegalStateException("Generation attempted start on an already running command '"+ key +"'. Executors should first attempt to retrieve an existing instance via GenerationOverseer.get('"+key+"')");
        }

        this.runningCommands.put(key, command);
    }

    private Duration endCommand(String key)
    throws IllegalStateException
    {
        ScriptStatus command = this.getCommand(key);
        if (null == command) {
            throw new IllegalStateException("Generation attempted end on a command not currently running '"+ key +"'.");
        }

        this.runningCommands.remove(key);
        return command.runtime();
    }


    // PUBLIC ACCESSORS
    public static ScriptStatus get(String key)
    {
        return ScriptOverseer.singleton().getCommand(key);
    }

    public static void ready(String key, ScriptStatus command)
    throws IllegalStateException
    {
        ScriptOverseer.singleton().readyCommand(key, command);
    }

    public static void clear(String key)
    {
        // @WARN not thread safe
        ScriptOverseer.singleton().runningCommands.remove(key);
    }

    public static Duration end(String key)
    throws IllegalStateException
    {
        return ScriptOverseer.singleton().endCommand(key);
    }

    public static Duration endIn(String key, long wait)
    throws IllegalStateException
    {
        Duration runtime = ScriptOverseer.singleton().getCommandRuntime(key);

        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    ScriptOverseer.end(key);
                }
            },
            wait
        );

        return runtime;
    }
}
