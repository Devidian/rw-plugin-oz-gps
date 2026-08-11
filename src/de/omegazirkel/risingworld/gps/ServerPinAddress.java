package de.omegazirkel.risingworld.gps;

import java.util.regex.Pattern;

/** Validation for the address format required by Player.connectToOtherServer. */
public final class ServerPinAddress {
    private static final Pattern HOST = Pattern.compile("(?i)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)*");
    private static final Pattern IPV4 = Pattern.compile("(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}");

    private ServerPinAddress() { }

    public static boolean isValid(String value) {
        if (value == null || value.length() > 255 || value.contains(" ")) return false;
        int separator = value.lastIndexOf(':');
        if (separator <= 0 || separator == value.length() - 1 || value.indexOf(':') != separator) return false;
        String host = value.substring(0, separator);
        String portText = value.substring(separator + 1);
        if (!HOST.matcher(host).matches() && !IPV4.matcher(host).matches()) return false;
        try {
            int port = Integer.parseInt(portText);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
