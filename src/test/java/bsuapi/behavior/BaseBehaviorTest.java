package bsuapi.behavior;

import bsuapi.dbal.Node;
import bsuapi.dbal.Topic;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BaseBehaviorTest
{
    @Test
    public void testTopicFoundMessage() {
        Topic t = this.mockTopic();
        Behavior b = new Assets(t);

        String result = b.buildMessage();
        assertEquals("Found :LABEL {FIELD:\"KEY\"}", result);
    }

    @Test
    public void testTopicEmptyMessage() {
        Topic t = this.mockTopic();
        doReturn(false).when(t).hasMatch();
        Behavior b = new Assets(t);

        String result = b.buildMessage();
        assertEquals("No Match Found For :LABEL", result);
    }

    @Test
    public void testAppendBehavior() {
        Topic t = this.mockTopic();
        Behavior b = new Assets(t);

        Behavior child = mock(Behavior.class);
        doReturn("CHILDNAME").when(child).getBehaviorKey();
        doReturn("CHILDDATA").when(child).getBehaviorData();
        doCallRealMethod().when(child).putBehaviorData(any());
        b.appendBehavior(child);

        JSONObject data = new JSONObject();
        b.putAppendedBehaviorData(data);

        assertEquals("CHILDDATA", data.query("/CHILDNAME"));
    }

    @Test
    public void testConfigEmpty() {
        Map<String, String> config = new HashMap<>();
        Behavior b = this.mockBehaviorConfig(config);

        assertNotNull(b.getConfigParam("limit"));
    }

    @Test
    public void testConfigNull() {
        Topic t = this.mockTopic();
        Behavior b = new Assets(t);
        b.setConfig(null);

        assertNotNull(b.getConfigParam("limit"));
    }

    @Test
    public void testConfigLimit() {
        Map<String, String> config = new HashMap<>();
        config.put("limit", "10");
        Behavior b = this.mockBehaviorConfig(config);

        assertEquals("10", b.getConfigParam("limit"));
    }

    @Test
    public void testConfigPage() {
        Map<String, String> config = new HashMap<>();
        config.put("page", "3");
        Behavior b = this.mockBehaviorConfig(config);

        assertEquals("20", b.getConfigParam("limit"));
        assertEquals("3", b.getConfigParam("page"));
    }

    @Test
    public void testConfigAll() {
        Map<String, String> config = new HashMap<>();
        config.put("limit", "100");
        config.put("page", "2");
        Behavior b = this.mockBehaviorConfig(config);

        assertEquals("100", b.getConfigParam("limit"));
        assertEquals("2", b.getConfigParam("page"));
    }

    private Topic mockTopic()
    {
        Node n = mock(Node.class);
        Topic t = mock(Topic.class);
        doReturn(true).when(t).hasMatch();
        doReturn("LABEL").when(t).name();
        doReturn("FIELD").when(t).getNodeKeyField();
        doReturn("KEY").when(t).getNodeKey();
        doReturn(n).when(t).getNode();

        return t;
    }

    private Behavior mockBehaviorConfig(Map<String, String> config)
    {
        Topic t = this.mockTopic();
        Behavior b = new Assets(t);
        b.setConfig(config);
        return b;
    }
}
