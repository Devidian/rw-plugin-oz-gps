package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServerPinAddressTest {
    @Test public void acceptsHostnameAndIpv4WithPort() {
        assertTrue(ServerPinAddress.isValid("play.example.org:4255"));
        assertTrue(ServerPinAddress.isValid("192.168.1.25:65535"));
    }

    @Test public void rejectsMalformedAddresses() {
        assertFalse(ServerPinAddress.isValid("play.example.org"));
        assertFalse(ServerPinAddress.isValid("play.example.org:0"));
        assertFalse(ServerPinAddress.isValid("play.example.org:65536"));
        assertFalse(ServerPinAddress.isValid("bad host:4255"));
    }
}
