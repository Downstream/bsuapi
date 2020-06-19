package bsuapi.service;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptAbstract;

import java.util.concurrent.*;

/**
 * Partially replicated from neo4j-apoc-procedures-3.5 java/apoc/Pools.java
 */
public class ScriptExecutor
{
    public final static ExecutorService POOL = createDefaultPool();
    static final int MAX_THREADS = 4;

    static {
        Runtime.getRuntime().addShutdownHook(ScriptExecutor.shutdown());
    }

    private ScriptExecutor() {throw new UnsupportedOperationException();} // prevent instantiation

    private static Thread shutdown()
    {
        return new Thread(() -> {
            try {
                POOL.shutdown();
                POOL.awaitTermination(10, TimeUnit.SECONDS);
            } catch (Exception ignore) {
                //
            }
        });
    }

    public static ExecutorService createDefaultPool()
    {
        return new ThreadPoolExecutor(ScriptExecutor.MAX_THREADS / 2, ScriptExecutor.MAX_THREADS, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(ScriptExecutor.MAX_THREADS * 10));
    }

    public static CypherScriptAbstract exec(Cypher c, CypherScript script)
    {
        CypherScriptAbstract runner = script.getRunner(c);
        POOL.execute(runner);
        return runner;
    }
}
