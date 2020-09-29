package bsuapi.obj;

import bsuapi.dbal.query.SettingsEntry;
import bsuapi.dbal.query.SettingsList;
import org.junit.Test;

import static org.junit.Assert.*;

public class SettingGroupTest {

    @Test
    public void testColor() {
        SettingGroup group = SettingGroup.COLOR;

        assertEquals("OpenPipeSetting", group.label());
        assertEquals("color", group.key());
        assertTrue(group.query() instanceof SettingsEntry);
    }

    @Test
    public void testGlobe() {
        SettingGroup group = SettingGroup.GLOBE;

        assertEquals("OpenPipeSetting", group.label());
        assertEquals("globe", group.key());
        assertTrue(group.query() instanceof SettingsList);
    }

    @Test
    public void testTimeline() {
        SettingGroup group = SettingGroup.TIMELINE;

        assertEquals("OpenPipeSetting", group.label());
        assertEquals("timeline", group.key());
        assertTrue(group.query() instanceof SettingsList);
    }

    @Test
    public void testExplore() {
        SettingGroup group = SettingGroup.EXPLORE;

        assertEquals("OpenPipeSetting", group.label());
        assertEquals("explore", group.key());
        assertTrue(group.query() instanceof SettingsList);
    }
}
