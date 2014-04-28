package lbaas.vxproxy.util;

import org.vertx.java.core.eventbus.EventBus;

public class LoggerOnEventBus {

    private final EventBus eb;
    public LoggerOnEventBus(EventBus eb) {
            this.eb = eb;
    }

    public void info(Object message) {
        eb.publish("log.info", message.toString());
    }

    public void warn(Object message) {
        eb.publish("log.warn", message.toString());
    }

    public void error(Object message) {
        eb.publish("log.error", message.toString());
    }

    public void fatal(Object message) {
        eb.publish("log.fatal", message.toString());
    }

    public void debug(Object message) {
        eb.publish("log.debug", message.toString());
    }

    public void trace(Object message) {
        eb.publish("log.trace", message.toString());
    }
}
