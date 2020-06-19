package bsuapi.script;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptAbstract;
import bsuapi.service.ScriptExecutor;
import bsuapi.service.ScriptOverseer;
import bsuapi.test.TestCypherResource;
import net.jodah.concurrentunit.Waiter;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Function;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class CypherScriptThreadingTest
{
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
    }

    @AfterClass
    public static void tearDown() {
        db.close();
    }

    @Test
    public void scriptRunning()
    throws Exception
    {
        CypherScript script = CypherScript.INFO;
        ScriptOverseer.clear(script.name());
        CypherScriptAbstract runner = script.getRunner(db.createCypher());
        Waiter waiter = new Waiter();

        Thread t = this.checkState(runner, waiter, CypherScriptAbstract::isRunning,
            (runnerRef) -> {
                waiter.assertFalse(runnerRef.isReady());
                waiter.assertTrue(runnerRef.isRunning());
                waiter.assertFalse(runnerRef.isComplete());
                assertTrue(1 < runnerRef.runtime().toMillis());
                return true;
            }
        );

        t.start();
        ScriptExecutor.POOL.execute(runner);
        waiter.await(2000);

        t.join(3000);
        JSONObject result = runner.statusReport();
        assertEquals("running", result.get("action"));
        assertTrue(1 < runner.runtime().toMillis());
    }

    @Test
    public void scriptComplete()
    throws Exception
    {
        CypherScript script = CypherScript.INFO;
        ScriptOverseer.clear(script.name());
        CypherScriptAbstract runner = script.getRunner(db.createCypher());
        Waiter waiter = new Waiter();

        Thread t = this.checkState(runner, waiter, CypherScriptAbstract::isComplete,
                (runnerRef) -> {
                    waiter.assertFalse(runnerRef.isReady());
                    waiter.assertFalse(runnerRef.isRunning());
                    waiter.assertTrue(runnerRef.isComplete());
                    assertTrue(1 < runnerRef.runtime().toMillis());
                    return true;
                }
        );

        t.start();
        ScriptExecutor.POOL.execute(runner);
        waiter.await(2000);

        t.join(5000);
        JSONObject result = runner.statusReport();
        assertEquals("completed", result.get("action"));
        assertTrue(1 < runner.runtime().toMillis());
    }

    private Thread checkState(CypherScriptAbstract runner, Waiter waiter, Function<CypherScriptAbstract, Boolean> stateTest, Function<CypherScriptAbstract,Boolean> threadTest)
    throws Exception
    {
        return new Thread(() -> {
            int attempts = 0;
            try {
                CypherScriptAbstract scriptRef2 = runner.getScript().getRunner(db.createCypher());

                boolean inExpectedState = stateTest.apply(scriptRef2);
                while (!inExpectedState && attempts++ < 40) {
                    sleep(50); // 2000 ms total
                    inExpectedState = stateTest.apply(scriptRef2);
                }
                waiter.assertTrue(inExpectedState);
            } catch (Exception e) {
                waiter.fail(e);
            } finally {
                waiter.resume();
            }
        });
    }

    // @todo collisions
    // @todo concurrency exceptions
    // @todo script errors
}
