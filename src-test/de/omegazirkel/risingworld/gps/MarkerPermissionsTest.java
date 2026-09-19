package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MarkerPermissionsTest {
    @Test
    public void allowsGroupMarkerDeletionOnlyForCreatorOrAdministrator() {
        assertTrue(MarkerPermissions.canDeleteGroupMarker(false, 42, 42));
        assertTrue(MarkerPermissions.canDeleteGroupMarker(true, 7, 42));
        assertFalse(MarkerPermissions.canDeleteGroupMarker(false, 7, 42));
    }
}
