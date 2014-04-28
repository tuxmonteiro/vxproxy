package lbaas.vxproxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

public class VirtualHost {

    private final String name;
    private Integer preferredBackEndServer;
    private ILoadBalanceAlgorithm loadBalanceAlgorithm;
    private Long lastChange;

    public VirtualHost(final String name, final EventBus eb) {
        this.name = name;
        this.preferredBackEndServer = 0;
        this.loadBalanceAlgorithm = new RandomLBAlgorithm();
        this.lastChange = 0L;
        eb.registerHandler(String.format("%s:setPreferredClient", name), new Handler<Message<Integer>>() {
            @Override
            public void handle(Message<Integer> event) {
                if (event.body() >= 0) {
                    preferredBackEndServer = event.body();
                }
            }
        });
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Integer getPreferredBackEndServer() {
        return preferredBackEndServer;
    }

    public void setPreferredBackEndServer(Integer preferredClient) {
        this.preferredBackEndServer = preferredClient;
    }

    public ILoadBalanceAlgorithm getLoadBalanceAlgorithm() {
        return loadBalanceAlgorithm;
    }

    public void setLoadBalanceAlgorithm(ILoadBalanceAlgorithm loadBalanceAlgorithm) {
        this.loadBalanceAlgorithm = loadBalanceAlgorithm;
    }

    public Long getLastChange() {
        return lastChange;
    }

    public void setLastChange(Long lastChange) {
        this.lastChange = lastChange;
    }

}
