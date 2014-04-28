package lbaas.vxproxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.logging.Logger;

public class VxLogger extends Verticle {

    public void start() {

        final EventBus eb = vertx.eventBus();
        final Logger log = container.logger();

        eb.registerHandler("log.debug", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                log.debug(message.body());
            }
        });
        eb.registerHandler("log.trace", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                log.trace(message.body());
            }
        });
        eb.registerHandler("log.info", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                log.info(message.body());
            }
        });
        eb.registerHandler("log.warn", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                log.warn(message.body());
            }
        });
        eb.registerHandler("log.error", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                log.error(message.body());
            }
        });
        eb.registerHandler("log.fatal", new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> message) {
                log.fatal(message.body());
            }
        });

        log.info(String.format("Instance %s started", this.toString()));
    }
}
