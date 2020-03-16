package bsuapi.service;

import bsuapi.dbal.query.GenerationStatus;

import java.time.Duration;
import java.util.HashMap;

public class GenerationOverseer
{
    private static GenerationOverseer instance;
    private HashMap<String, GenerationStatus> runningCommands;

    private GenerationOverseer() //singleton
    {
        this.runningCommands = new HashMap<>();
    }

    private static GenerationOverseer singleton()
    {
        if (null == GenerationOverseer.instance) {
            GenerationOverseer.instance = new GenerationOverseer();
        }

        return GenerationOverseer.instance;
    }

    private GenerationStatus getCommand(String key)
    {
        return this.runningCommands.get(key);
    }

    private Duration getCommandRuntime(String key)
    {
        GenerationStatus command = this.getCommand(key);
        if (null == command) {
            return null;
        }

        return command.runtime();
    }

    private void startCommand(String key, GenerationStatus command)
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
        GenerationStatus command = this.getCommand(key);
        if (null == command) {
            throw new IllegalStateException("Generation attempted end on a command not currently running '"+ key +"'.");
        }

        this.runningCommands.remove(key);
        return command.runtime();
    }


    // PUBLIC ACCESSORS
    public static GenerationStatus get(String key)
    {
        return GenerationOverseer.singleton().getCommand(key);
    }

    public static void start(String key, GenerationStatus command)
    throws IllegalStateException
    {
        GenerationOverseer.singleton().startCommand(key, command);
    }

    public static Duration end(String key)
    throws IllegalStateException
    {
        return GenerationOverseer.singleton().endCommand(key);
    }

    public static Duration endIn(String key, int wait)
    throws IllegalStateException
    {
        Duration runtime = GenerationOverseer.singleton().getCommandRuntime(key);

        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    GenerationOverseer.singleton().endCommand(key);
                }
            },
            wait
        );

        return runtime;
    }
}
