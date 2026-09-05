package de.omegazirkel.risingworld.gps.web;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.google.gson.Gson;

import de.omegazirkel.risingworld.OZToolsNativeWebAccess;

import net.risingworld.api.callbacks.WebserverHandler;
import net.risingworld.api.events.general.HttpRequestEvent;
import net.risingworld.api.events.general.HttpRequestEvent.HttpMethod;

/** Read-only native route for public GPS markers only. */
public final class GpsGlobalMarkerRoute implements WebserverHandler {
    private static final Gson GSON = new Gson();
    private final BooleanSupplier enabled;
    private final Function<Long, Object> exporter;

    public GpsGlobalMarkerRoute(BooleanSupplier enabled, Function<Long, Object> exporter) {
        this.enabled = enabled;
        this.exporter = exporter;
    }

    @Override
    public void onRequest(HttpRequestEvent event) {
        event.setResponseHeader("Cache-Control", "no-store");
        event.setContentType("application/json; charset=utf-8");
        if (!enabled.getAsBoolean()) {
            event.setResponseCode(404);
            event.setResponseBody("{\"error\":\"not_found\"}");
            return;
        }
        if (!OZToolsNativeWebAccess.authorize(event)) return;
        if (event.getMethod() != HttpMethod.GET) {
            event.setResponseCode(405);
            event.setResponseHeader("Allow", "GET");
            event.setResponseBody("{\"error\":\"method_not_allowed\"}");
            return;
        }
        String type = event.getQueryParameters().get("type");
        if (!"global".equals(type)) {
            event.setResponseCode(400);
            event.setResponseBody("{\"error\":\"invalid_marker_type\"}");
            return;
        }
        try {
            event.setResponseCode(200);
            event.setResponseBody(GSON.toJson(exporter.apply(parseLastChange(event.getQueryParameters()))));
        } catch (IllegalArgumentException ex) {
            event.setResponseCode(400);
            event.setResponseBody("{\"error\":\"invalid_last_change\"}");
        } catch (RuntimeException ex) {
            event.setResponseCode(503);
            event.setResponseBody("{\"error\":\"markers_unavailable\"}");
        }
    }

    public static Long parseLastChange(Map<String, String> query) {
        String raw = query.get("lastChange");
        if (raw == null) return null;
        if (!raw.matches("\\d+")) throw new IllegalArgumentException("Invalid lastChange");
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid lastChange", ex);
        }
    }
}
