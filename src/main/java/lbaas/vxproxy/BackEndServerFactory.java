package lbaas.vxproxy;

import java.util.HashMap;
import java.util.Set;

import lbaas.vxproxy.util.LoggerOnEventBus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class BackEndServerFactory {

    private final HashMap<String, BackEndServer> mapBackEndServerToBackEndServer = new HashMap<>();
    private final HashMap<String, VirtualHost> mapVHostToVhost = new HashMap<>();
    private final HashMap<String, Set<String>> graphRoutes = new HashMap<>();
    private final EventBus eb;
    private final Vertx vertx;
    private final Verticle verticle;
    private final JsonObject conf;
    private final RouteEventsManager routeEventsManager;
    private final LoggerOnEventBus log;
    private long delay = 1000L;

    public BackEndServerFactory(final Verticle verticle) {
        this.verticle = verticle;
        this.vertx = verticle.getVertx();
        this.eb = vertx.eventBus();
        this.conf = verticle.getContainer().config();
        this.routeEventsManager = new RouteEventsManager(eb);
        this.log = new LoggerOnEventBus(eb);

        routeEventsManager.registerRouteAdd(graphRoutes);
        routeEventsManager.registerRouteDel(graphRoutes);
        log.info(String.format("%s -> %s loaded", verticle.toString(), this.toString()));
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean existBackEndServer(String host, Integer port) {
        return existBackEndServer(String.format("%s:%d", host, port));
    }

    public boolean existBackEndServer(String BackEndServerWithPort) {
        return mapBackEndServerToBackEndServer.containsKey(BackEndServerWithPort);
    }

    private BackEndServer addBackEndServer(String host, Integer port) {
        return addBackEndServer(String.format("%s:%d", host, port));    }

    private BackEndServer addBackEndServer(String BackEndServerWithPort) {
        String[] BackEndServerWithPortArray = BackEndServerWithPort.split(":");
        if (BackEndServerWithPortArray.length < 2) {
            return null;
        }
        String host = BackEndServerWithPortArray[0];
        Integer port = Integer.parseInt(BackEndServerWithPortArray[1]);
        BackEndServer BackEndServer = new BackEndServer(vertx.createHttpClient(), conf);
        BackEndServer.setHost(host).setPort(port);

        if (BackEndServer.isOk()) {
            mapBackEndServerToBackEndServer.put(BackEndServer.toString(), BackEndServer);
            return BackEndServer;
        }
        return null;
    }

    private void removeBackEndServer(String host, Integer port) {
        removeBackEndServer(String.format("%s:%d", host, port));
    }

    private void removeBackEndServer(String BackEndServerWithPort) {
        BackEndServer BackEndServer = getBackEndServer(BackEndServerWithPort);
        if (BackEndServer != null) {
            BackEndServer.close();
        }
        mapBackEndServerToBackEndServer.put(BackEndServerWithPort, null);
        mapBackEndServerToBackEndServer.remove(BackEndServerWithPort);
    }
    public BackEndServer getBackEndServer(String host, Integer port) {
        if (existBackEndServer(host, port)) {
            return mapBackEndServerToBackEndServer.get(String.format("%s:%d", host, port));
        }

        return addBackEndServer(host, port);
    }

    public BackEndServer getBackEndServer(String BackEndServerWithPort) {
        String[] BackEndServerWithPortArray = BackEndServerWithPort.split(":");
        if (BackEndServerWithPortArray.length < 2) {
            return null;
        }
        return getBackEndServer(BackEndServerWithPortArray[0], Integer.parseInt(BackEndServerWithPortArray[1]));
    }

    public boolean existVirtualHost(String vhost) {
        return graphRoutes.containsKey(vhost);
    }

    private VirtualHost addVirtualHost(final String vhost) {
        VirtualHost virtualhost = new VirtualHost(vhost, eb);
        mapVHostToVhost.put(vhost, virtualhost);
        eb.registerHandler(String.format("%s:searchNewBackEndServer", vhost), new Handler<Message<Integer>>() {
            @Override
            public void handle(Message<Integer> message) {
                if (message.body()==getVirtualHost(vhost).getPreferredBackEndServer()) {
                    getNewChoice(vhost);
                }
            }
        });
        return virtualhost;
    }

    public VirtualHost getVirtualHost(String vhost) {
        if (existVirtualHost(vhost)) {
            if (mapVHostToVhost.containsKey(vhost)) {
                return mapVHostToVhost.get(vhost);
            } else {
                return addVirtualHost(vhost);
            }
        }
        return null;
    }

    public void removeVirtualHost(String vhost) {
        mapVHostToVhost.put(vhost, null);
        mapVHostToVhost.remove(vhost);
    }

    private BackEndServer getNewChoice(String vhost) {
        if (existVirtualHost(vhost)) {

            Set<String> backEndServersWithPort = graphRoutes.get(vhost);
            VirtualHost virtualhost = getVirtualHost(vhost);

            if (!backEndServersWithPort.isEmpty()) {

                BackEndServer backEndServerObj = null;
                int mapSize = backEndServersWithPort.size();
                int discoveryFactor = 10; // higher number = higher probability to finding at least one
                for (int count=0; count<mapSize*discoveryFactor; count++) {
                    Integer selected = virtualhost.getLoadBalanceAlgorithm().getSelected(mapSize);
                    String backEndServerWithPort = backEndServersWithPort.toArray()[selected].toString();
                    backEndServerObj = getBackEndServer(backEndServerWithPort);
                    if (backEndServerObj!=null) {
                        if (backEndServerObj.isOk()) {
                            eb.publish(String.format("%s:setPreferredBackEndServer", vhost), selected);
                            return backEndServerObj;
                        }
                    }
                }
            }
        }
        return null;
    }

    public HttpClient getChoice(final String vhost) {
        VirtualHost virtualhost = getVirtualHost(vhost);
        Integer preferredBackEndServer = virtualhost.getPreferredBackEndServer();
        BackEndServer backEndServer = getBackEndServer(graphRoutes.get(vhost).toArray()[preferredBackEndServer].toString());
        if (backEndServer!=null) {
            if (backEndServer.isOk()) {
                long now = System.currentTimeMillis();
                if (virtualhost.getLastChange()<(now+delay)) {
                    eb.send(String.format("%s:searchNewBackEndServer", vhost), preferredBackEndServer);
                    virtualhost.setLastChange(now);
                }
                return backEndServer.getClient();
            }
        }
        return getNewChoice(vhost).getClient();
    }
}
