package bsuapi.coll;

import bsuapi.coll.ListCleanFunction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ListCleanFunctionTest
{
    private ArrayList<Object> list(Object ... a)
    {
        return new ArrayList<>(Arrays.asList(a));
    }

    // List listToClean - List invalid

    @Test
    public void cleanOfListAllEmpties()
    {
        List listToClean = this.list("aaa",null,"","bbb","ccc");
        List invalid = this.list("bbb");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOfList(listToClean, invalid, true);

        assertEquals(this.list("aaa","ccc"), result);
    }


    @Test
    public void cleanOfListAllEmptiesMulti()
    {
        List listToClean = this.list("aaa",null,"","bbb","ccc");
        List invalid = this.list("bbb","ccc");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOfList(listToClean, invalid, true);

        assertEquals(this.list("aaa"), result);
    }

    @Test
    public void cleanOfListAllEmptiesNumeric()
    {
        List listToClean = this.list(12,null,"",1.22,22,0);
        List invalid = this.list(12);

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOfList(listToClean, invalid, true);

        assertEquals(this.list(1.22,22,0), result);
    }

    @Test
    public void cleanOfListAllEmptiesResultNull()
    {
        List listToClean = this.list("aaa",null,"");
        List invalid = this.list("aaa");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOfList(listToClean, invalid, true);

        assertNull( result);
    }

    @Test
    public void cleanOfListAllEmptiesResultEmptyList()
    {
        List listToClean = this.list("aaa",null,"");
        List invalid = this.list("aaa");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOfList(listToClean, invalid, false);

        assertEquals(this.list(), result);
    }

    // List listToClean - String invalid

    @Test
    public void cleanOfAllEmpties()
    {
        List listToClean = this.list("aaa",null,"","bbb","ccc");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOf(listToClean, "bbb", true);

        assertEquals(this.list("aaa","ccc"), result);
    }

    @Test
    public void cleanOfAllEmptiesNumeric()
    {
        List listToClean = this.list(12,null,"",1.22,22,0);

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOf(listToClean, "", true);

        assertEquals(this.list(12,1.22,22,0), result);
    }

    @Test
    public void cleanOfAllEmptiesResultNull()
    {
        List listToClean = this.list("aaa",null,"");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOf(listToClean, "aaa", true);

        assertNull( result);
    }

    @Test
    public void cleanOfAllEmptiesResultEmptyList()
    {
        List listToClean = this.list("aaa",null,"");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.cleanOf(listToClean, "aaa", false);

        assertEquals(this.list(), result);
    }

    // List listToClean - no args

    @Test
    public void cleanAllEmpties()
    {
        List listToClean = this.list("aaa",null,"","bbb");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.clean(listToClean,  true);

        assertEquals(this.list("aaa","bbb"), result);
    }

    @Test
    public void cleanAllEmptiesNumeric()
    {
        List listToClean = this.list(12,null,"",1.22,22,0);

        ListCleanFunction c = new ListCleanFunction();
        List result = c.clean(listToClean,  true);

        assertEquals(this.list(12,1.22,22,0), result);
    }

    @Test
    public void cleanAllEmptiesResultNull()
    {
        List listToClean = this.list(null,"");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.clean(listToClean,  true);

        assertNull( result);
    }

    @Test
    public void cleanAllEmptiesResultEmptyList()
    {
        ArrayList listToClean = this.list(null,"");

        ListCleanFunction c = new ListCleanFunction();
        List result = c.clean(listToClean,  false);

        assertEquals(this.list(), result);
    }
}