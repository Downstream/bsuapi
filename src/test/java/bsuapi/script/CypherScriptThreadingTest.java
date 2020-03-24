package bsuapi.script;

import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptFile;
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
    public void scriptReady()
    throws Exception
    {
        CypherScriptFile script = CypherScriptFile.go(CypherScript.INFO);

        assertTrue(script.isReady());
        assertFalse(script.isRunning());
        assertFalse(script.isComplete());
        JSONObject result = script.statusReport();
        assertEquals("starting", result.get("action"));
        assertEquals("0:00:00:000", result.get("runTime"));
    }

    @Test
    public void scriptRunning()
            throws Exception
    {
        CypherScriptFile script = CypherScriptFile.go(CypherScript.INFO);
        Waiter waiter = new Waiter();

        this.checkState(waiter, CypherScriptFile::isRunning,
            (script2) -> {
                waiter.assertFalse(script2.isReady());
                waiter.assertTrue(script2.isRunning());
                waiter.assertFalse(script2.isComplete());
                return true;
            }
        );

        script.exec(db.createCypher());

        waiter.await(2000);

        JSONObject result = script.statusReport();
        assertEquals("running", result.get("action"));
        assertTrue(1 < script.runtime().toMillis());
    }

    @Test
    public void scriptComplete()
            throws Exception
    {
        CypherScriptFile script = CypherScriptFile.go(CypherScript.INFO);
        Waiter waiter = new Waiter();

        this.checkState(waiter, CypherScriptFile::isComplete,
            (script2) -> {
                waiter.assertFalse(script2.isReady());
                waiter.assertTrue(script2.isComplete());
                return true;
            }
        );

        script.exec(db.createCypher());

        waiter.await(2000);

        JSONObject result = script.statusReport();
        assertEquals("completed", result.get("action"));
        assertTrue(1 < script.runtime().toMillis());
    }

    private void checkState(Waiter waiter, Function<CypherScriptFile, Boolean> stateTest, Function<CypherScriptFile,Boolean> threadTest)
    {
        new Thread(() -> {
            try {
                CypherScriptFile script = CypherScriptFile.go(CypherScript.INFO);
                while (!stateTest.apply(script)) {
                    sleep(10);
                }
                waiter.assertTrue(threadTest.apply(script));
            } catch (Exception e) {
                waiter.fail(e);
            }
            waiter.resume();
        }).start();
    }

    // @todo collisions
    // @todo concurrency exceptions
    // @todo script errors
}
