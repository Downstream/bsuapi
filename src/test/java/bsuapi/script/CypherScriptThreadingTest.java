package bsuapi.script;

import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptFile;
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
    public void scriptReady()
    throws Exception
    {
        ScriptOverseer.clear(CypherScript.INFO.filename());
        CypherScriptFile script = CypherScriptFile.go(CypherScript.INFO);

        assertTrue(script.isReady());
        assertFalse(script.isRunning());
        assertFalse(script.isComplete());
        assertFalse(script.isFailed());
        JSONObject result = script.statusReport();
        assertEquals("starting", result.get("action"));
        assertEquals("0:00:00:000", result.get("runTime"));
    }

    @Test
    public void scriptRunning()
    throws Exception
    {
        ScriptOverseer.clear(CypherScript.INFO.filename());
        Waiter waiter = new Waiter();

        CypherScriptFile script = this.checkState(CypherScript.INFO.filename(), waiter, CypherScriptFile::isRunning,
            (script2) -> {
                waiter.assertFalse(script2.isReady());
                return true;
            }
        );

        JSONObject result = script.statusReport();
        assertEquals("running", result.get("action"));
        assertTrue(1 < script.runtime().toMillis());
    }

    @Test
    public void scriptComplete()
    throws Exception
    {
        ScriptOverseer.clear(CypherScript.INFO.filename());
        Waiter waiter = new Waiter();

        CypherScriptFile script = this.checkState(CypherScript.INFO.filename(), waiter, CypherScriptFile::isComplete,
            (script2) -> {
                waiter.assertFalse(script2.isReady());
                return true;
            }
        );

        JSONObject result = script.statusReport();
        assertEquals("completed", result.get("action"));
        assertTrue(1 < script.runtime().toMillis());
    }

    @Test
    public void scriptFailed()
    throws Exception
    {
        ScriptOverseer.clear("badScript.cypher");
        Waiter waiter = new Waiter();

        CypherScriptFile script = this.checkState("badScript.cypher", waiter, CypherScriptFile::isFailed,
            (script2) -> {
                waiter.assertFalse(script2.isComplete());
                return true;
            }
        );

        JSONObject result = script.statusReport();
        assertEquals("failed", result.get("action"));
        assertTrue(1 < script.runtime().toMillis());
    }

    private CypherScriptFile checkState(String filename, Waiter waiter, Function<CypherScriptFile, Boolean> stateTest, Function<CypherScriptFile,Boolean> threadTest)
    throws Exception
    {
        CypherScriptFile scriptRef1 = CypherScriptFile.go(filename);

        new Thread(() -> {
            int attempts = 0;
            try {
                CypherScriptFile scriptRef2 = CypherScriptFile.go(filename);
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
        }).start();

        scriptRef1.exec(db.createCypher());

        waiter.await(3000);

        return scriptRef1;
    }

    // @todo collisions
    // @todo concurrency exceptions
    // @todo script errors
}
