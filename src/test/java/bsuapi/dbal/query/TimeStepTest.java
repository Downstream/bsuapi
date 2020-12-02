package bsuapi.dbal.query;

import bsuapi.dbal.Node;
import bsuapi.dbal.VirtualNode;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TimeStepTest {

    @Test
    public void testBC() {
        assertEquals("0",TimeStep.YEAR50.getDateKey(this.createNode("0000-01-05")));
        assertEquals("-50",TimeStep.YEAR50.getDateKey(this.createNode("-0049-01-05")));
        assertEquals("-200",TimeStep.YEAR50.getDateKey(this.createNode("-0199-01-05")));
        assertEquals("-200",TimeStep.YEAR50.getDateKey(this.createNode("-0200-01-05")));
        assertEquals("-250",TimeStep.YEAR50.getDateKey(this.createNode("-0201-01-05")));
    }

    @Test
    public void testYear() {
        String key = TimeStep.YEAR.getDateKey(this.createNode("1201-01-05"));
        assertEquals("1201",key);
    }

    @Test
    public void testMonth() {
        String key = TimeStep.MONTH.getDateKey(this.createNode("1201-02-05"));
        assertEquals("Feb 1201",key);
    }

    @Test
    public void testDay() {
        String key = TimeStep.DAY.getDateKey(this.createNode("1201-01-05"));
        assertEquals("5 Jan 1201",key);
    }

    private Node createNode(String date)
    {
        HashMap<String, String> props = new HashMap<>();
        props.put("type","asset");
        props.put("guid","X");
        props.put("date",date);
        return new VirtualNode(props);
    }
}
