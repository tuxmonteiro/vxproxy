package lbaas.vxproxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

public class RouteEventsManager {

    private final EventBus eb;
    private Long version = 0L;
    private boolean versionRecycled = false;
    private Character separator = ':';

    private void nextVersion() {
        if (version < Long.MAX_VALUE-1) {
            version++;
            versionRecycled = versionRecycled && version < Long.MAX_VALUE / 2;
        } else {
            version = 1L;
            versionRecycled = true;
        }
    }

    public Character getSeparator() {
        return separator;
    }

    public void setSeparator(Character separator) {
        this.separator = separator;
    }

    public RouteEventsManager(final EventBus eb) {
        this.eb = eb;
    }

    public Long getVersion() {
        return version;
    }

    public boolean getVersionRecycled() {
        return versionRecycled;
    }

    public void registerRouteAdd(final HashMap<String, Set<String>> graphRoutes) {
        eb.registerHandler("route.add", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                String[] route = message.body().split(separator.toString());
                if (route.length > 2) {
                    if (graphRoutes.containsKey(route[0])) {
                        Set<String> backEndServers = graphRoutes.get(route[0]);
                        backEndServers.add(String.format("%s:%s", route[1], route[2]));
                    } else {
                        Set<String> backEndServers = new HashSet<String>();
                        graphRoutes.put(route[0], backEndServers);
                        backEndServers.add(String.format("%s:%s", route[1], route[2]));
                    }
                    try {
                        if (route.length > 3) {
                            version = Long.parseLong(route[3]);
                        } else {
                            nextVersion();
                        }
                    } catch (NumberFormatException e) {
                        nextVersion();
                    }
                }
            }
        });
    }

    public void registerRouteDel(final HashMap<String, Set<String>> graphRoutes) {
        eb.registerHandler("route.del", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                String[] route = message.body().split(separator.toString());
                if (route.length > 2) {
                    if (graphRoutes.containsKey(route[0])) {
                        Set<String> backEndServers = graphRoutes.get(route[0]);
                        backEndServers.remove(String.format("%s:%s", route[1], route[2]));
                    }
                    try {
                        if (route.length > 3) {
                            version = Long.parseLong(route[3]);
                        } else {
                            nextVersion();
                        }
                    } catch (NumberFormatException e) {
                        nextVersion();
                    }
                }
            }
        });
    }
}
