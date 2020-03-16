package bsuapi.coll;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ListSingleFunctionTest
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

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanList(listToClean, invalid);

        assertEquals("aaa", result);
    }


    @Test
    public void cleanOfListAllEmptiesMulti()
    {
        List listToClean = this.list("aaa",null,"","bbb","ccc");
        List invalid = this.list("bbb","ccc");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanList(listToClean, invalid);

        assertEquals("aaa", result);
    }

    @Test
    public void cleanOfListAllEmptiesNumeric()
    {
        List listToClean = this.list(12,null,"",1.22,22,0);
        List invalid = this.list(12);

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanList(listToClean, invalid);

        assertEquals(1.22, result);
    }

    @Test
    public void cleanOfListAllEmptiesResultNull()
    {
        List listToClean = this.list("aaa",null,"");
        List invalid = this.list("aaa");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanList(listToClean, invalid);

        assertNull( result);
    }

    @Test
    public void cleanOfListAllEmptiesResultEmptyList()
    {
        List listToClean = this.list("aaa",null,"");
        List invalid = this.list("aaa");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanList(listToClean, invalid);

        assertNull( result);
    }

    @Test
    public void cleanOfNullListNull()
    {
        List listToClean = null;
        List invalid = this.list("aaa");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanList(listToClean, invalid);

        assertNull( result);
    }

    // List listToClean - String invalid

    @Test
    public void cleanOfAllEmpties()
    {
        List listToClean = this.list("aaa",null,"","bbb","ccc");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanOf(listToClean, "bbb");

        assertEquals("aaa", result);
    }

    @Test
    public void cleanOfAllEmptiesNumeric()
    {
        List listToClean = this.list(12,null,"",1.22,22,0);

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanOf(listToClean, "");

        assertEquals(12, result);
    }

    @Test
    public void cleanOfAllEmptiesResultNull()
    {
        List listToClean = this.list("aaa",null,"");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanOf(listToClean, "aaa");

        assertNull( result);
    }

    @Test
    public void cleanOfAllEmptiesResultEmptyList()
    {
        List listToClean = this.list("aaa",null,"");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanOf(listToClean, "aaa");

        assertNull( result);
    }

    @Test
    public void cleanOfListNull()
    {
        List listToClean = null;

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleCleanOf(listToClean, "aaa");

        assertNull( result);
    }

    // List listToClean - no args

    @Test
    public void cleanAllEmpties()
    {
        List listToClean = this.list("aaa",null,"","bbb");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleClean(listToClean);

        assertEquals("aaa", result);
    }

    @Test
    public void cleanAllEmptiesNumeric()
    {
        List listToClean = this.list(12,null,"",1.22,22,0);

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleClean(listToClean);

        assertEquals(12, result);
    }

    @Test
    public void cleanAllEmptiesResultNull()
    {
        List listToClean = this.list(null,"");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleClean(listToClean);

        assertNull( result);
    }

    @Test
    public void cleanAllEmptiesResultEmptyList()
    {
        ArrayList listToClean = this.list(null,"");

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleClean(listToClean);

        assertNull( result);
    }

    @Test
    public void cleanListNull()
    {
        ArrayList listToClean = null;

        ListSingleFunction c = new ListSingleFunction();
        Object result = c.singleClean(listToClean);

        assertNull( result);
    }
}