package lbaas.vxproxy;

import java.util.HashMap;
import java.util.Set;

import lbaas.vxproxy.util.LoggerOnEventBus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class RouteTable extends Verticle {

    private final HashMap<String, Set<String>> graphRoutes = new HashMap<>();
    private LoggerOnEventBus log;
    private RouteEventsManager routeEventsManager;

    private enum Action {
        ADD,
        DEL
    }

    public void start() {
        registerRouteEvents();
        EventBus eb = vertx.eventBus();
        startHttpServer(eb, container.config());
        routeEventsManager = new RouteEventsManager(eb);
        routeEventsManager.registerRouteAdd(graphRoutes);
        routeEventsManager.registerRouteDel(graphRoutes);
        this.log = new LoggerOnEventBus(eb);
        log.info(String.format("Instance RouteTable %s started", this.toString()));
    }

    private void registerRouteEvents() {

    }

    public long getVersion() {
        return routeEventsManager.getVersion();
    }

    private void startHttpServer(final EventBus eb, final JsonObject serverConf) throws RuntimeException {

        HttpServer server = vertx.createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();
        routeMatcher.post("/routez", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest req) {
                req.bodyHandler(new Handler<Buffer>() {
                    public void handle(Buffer body) {
                        try {
                            JsonObject json = new JsonObject(body.toString());
                            req.response().setStatusCode(200);
                            req.response().setStatusMessage("OK");

                            setRoute(json,eb);
                            if (json.containsField("debug") && (json.getBoolean("debug"))) {
                                req.response().end(getRoutes().encodePrettily());
                            } else {
                                req.response().end();
                            }

                        } catch (Exception e) {
                            log.error(String.format("JSON FAIL: %s\nBody: %s",
                                    e.getMessage(), body.toString()));
                            req.response().setStatusCode(400);
                            req.response().setStatusMessage("Bad Request");
                            req.response().end();
                        }
                    }
                });
            }
        });
        routeMatcher.get("/routez", new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                req.response().setStatusCode(200);
                req.response().setStatusMessage("OK");
                req.response().end(getRoutes().encodePrettily());
            }
        });
        routeMatcher.noMatch(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                req.response().setStatusCode(404);
                req.response().setStatusMessage("Not Found");
                req.response().end();
            }
        });
        Integer listenPort = 9090;
        if (serverConf!=null) {
            listenPort=serverConf.getInteger("port", 9090);
        }
        server.requestHandler(routeMatcher).listen(listenPort);
    }

    public boolean existVirtualHost(String vhost) {
        return graphRoutes.containsKey(vhost);
    }

    public void setRoute(JsonObject json, final EventBus eb) throws RuntimeException {

        Action action = Action.ADD; // Action default
        String vhost;
        String endpoint;
        Integer port;
        Long _version;
        if  (json.containsField("action")) {
            String actionJson = json.getString("action");
            if (actionJson.equalsIgnoreCase("add") || actionJson.equalsIgnoreCase("del")) {
                action = (json.getString("action").equalsIgnoreCase("add")) ? Action.ADD : Action.DEL;
            } else {
                throw new RuntimeException(String.format("Action %s not implemented.", actionJson));
            }
        }
        if (json.containsField("vhost")) {
            vhost = json.getString("vhost");
        } else {
            throw new RuntimeException("virtualhost undef");
        }
        if (json.containsField("host")) {
            endpoint = json.getString("host");
        } else {
            throw new RuntimeException("endpoint host undef");
        }
        if (json.containsField("port")) {
            port = json.getInteger("port");
        } else {
            throw new RuntimeException("endpoint port undef");
        }
        if (json.containsField("version")) {
            _version = json.getLong("version");
        } else {
            _version = 0L;
        }

        switch (action) {
            case ADD:
                eb.publish("route.add", String.format("%s:%s:%d:%d", vhost, endpoint, port, _version));
                break;
            case DEL:
                eb.publish("route.del", String.format("%s:%s:%d:%d", vhost, endpoint, port, _version));
                break;
        }
    }

    public JsonObject getRoutes() {
        JsonObject routes = new JsonObject();
        routes.putNumber("version", getVersion());
        JsonArray vhosts = new JsonArray();

        for (String vhost : graphRoutes.keySet()) {
            JsonObject vhostObj = new JsonObject();
            vhostObj.putString("name", vhost);
            JsonArray endpoints = new JsonArray();
            for (String value : graphRoutes.get(vhost)) {
                JsonObject endpointObj = new JsonObject();
                endpointObj.putString("host", value.split(":")[0]);
                endpointObj.putNumber("port", Integer.parseInt(value.split(":")[1]));
                endpoints.add(endpointObj);
            }
            vhostObj.putArray("endpoints", endpoints);
            vhosts.add(vhostObj);
        }
        routes.putArray("routes", vhosts);
        return routes;
    }
}
