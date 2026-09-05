package de.omegazirkel.risingworld.gps.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

public class GpsGlobalMarkerRouteTest {
    @Test
    public void parsesAnOptionalNonNegativeCursor() {
        assertNull(GpsGlobalMarkerRoute.parseLastChange(Map.of()));
        assertEquals(Long.valueOf(42L), GpsGlobalMarkerRoute.parseLastChange(Map.of("lastChange", "42")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidCursor() {
        GpsGlobalMarkerRoute.parseLastChange(Map.of("lastChange", "-1"));
    }
}
