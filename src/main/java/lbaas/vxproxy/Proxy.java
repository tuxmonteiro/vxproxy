package lbaas.vxproxy;

import lbaas.vxproxy.util.LoggerOnEventBus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

public class Proxy extends Verticle {

    public void start() {

        final HttpServer server = vertx.createHttpServer();
        final EventBus eb = vertx.eventBus();
        final BackEndServerFactory backEndServerFactory = new BackEndServerFactory(this);
        final LoggerOnEventBus log = new LoggerOnEventBus(eb);

        // Configurations
        final JsonObject config = container.config();
        server.setUsePooledBuffers(config.getBoolean("usePooledBuffer", server.isUsePooledBuffers()));
        server.setTCPKeepAlive(config.getBoolean("tcpKeepAlive", server.isTCPKeepAlive()));
        server.setTCPNoDelay(config.getBoolean("tcpNoDelay", server.isTCPNoDelay()));
        server.setCompressionSupported(config.getBoolean("compressionSupported", server.isCompressionSupported()));
        server.setReuseAddress(config.getBoolean("reuseAddress", server.isReuseAddress()));
        server.setAcceptBacklog(config.getInteger("acceptBacklog", server.getAcceptBacklog()));

        Integer receiveBufferSize = config.getInteger("receiveBufferSize",server.getReceiveBufferSize());
        if (receiveBufferSize!=null && receiveBufferSize>=1) {
            server.setReceiveBufferSize(receiveBufferSize);
        }
        Integer sendBufferSize = config.getInteger("sendBufferSize", server.getSendBufferSize());
        if (sendBufferSize!=null && sendBufferSize>=1) {
            server.setSendBufferSize(sendBufferSize);
        }
        server.setSoLinger(config.getInteger("soLinger", server.getSoLinger()));
        server.setSSL(config.getBoolean("ssl", server.isSSL()));

        final long postEndConnectionTimeout = config.getLong("postEndConnectionTimeout", 2000L);

        server.requestHandler(new Handler<HttpServerRequest>() {

            @Override
            public void handle(final HttpServerRequest req) {
                // Obtem o header "Host", considerando apenas o hostname, não a porta, caso definida.
                final String vhost = req.headers().get("Host").split(":")[0];
                if (!backEndServerFactory.existVirtualHost(vhost)) {
                    log.error(String.format("Host: %s UNDEF", vhost));
                    req.response().setStatusCode(400);
                    req.response().setStatusMessage("Bad Request");
                    req.response().end();
                    return;
                }
                final HttpClient backEndServer = backEndServerFactory.getChoice(vhost);
                if (backEndServer == null) {
                    log.error("Endpoints unknown or without eligible\n");
                    req.response().setStatusCode(502);
                    req.response().setStatusMessage("Bad Gateway");
                    req.response().end();
                    return;
                }

                final HttpClientRequest cReq = backEndServer.request(req.method(), req.uri(),
                                new Handler<HttpClientResponse>() {
                                    public void handle(HttpClientResponse cRes) {
                                        req.response().setStatusCode(
                                                cRes.statusCode());
                                        req.response().headers()
                                                .set(cRes.headers());
                                        req.response().setChunked(true);

                                        Pump.createPump(cRes, req.response()).start();

                                        cRes.endHandler(new VoidHandler() {
                                            public void handle() {
                                                try {
                                                    req.response().end();
                                                    vertx.setTimer(postEndConnectionTimeout, new Handler<Long>() {
                                                        public void handle(Long event) {
                                                            req.response().close();
                                                        }
                                                    });
                                                } catch (RuntimeException e1) {
                                                    log.error(e1.getMessage());
                                                }
                                            }
                                        });
                                    }
                                });

                cReq.exceptionHandler(new Handler<Throwable>() {
                    public void handle(Throwable event) {
                        log.error(event.getMessage());
                        if (event.getMessage().contains("Connection refused")) {
                            ((BackEndServer)backEndServer).setOk(false);
                        }
                        req.response().setStatusCode(502);
                        req.response().setStatusMessage("Bad Gateway");
                        req.response().end();
                    }
                });

                String xff;
                String remote = req.remoteAddress().getAddress().getHostAddress();
                req.headers().set("X-Real-IP", remote);

                if (req.headers().contains("X-Forwarded-For")) {
                    xff = req.headers().get("X-Forwarded-For")+", "+remote;
                    req.headers().remove("X-Forwarded-For");
                } else {
                    xff = remote;
                }
                req.headers().set("X-Forwarded-For", xff);

                if (req.headers().contains("Forwarded-For")) {
                    xff = req.headers().get("Forwarded-For")+", "+remote;
                    req.headers().remove("Forwarded-For");
                } else {
                    xff = remote;
                }
                req.headers().set("Forwarded-For", xff);

                if (!req.headers().contains("X-Forwarded-Host")) {
                    req.headers().set("X-Forwarded-Host", vhost);
                }

                if (!req.headers().contains("X-Forwarded-Proto")) {
                    req.headers().set("X-Forwarded-Proto", "http");
                }

                cReq.headers().set(req.headers());
                cReq.setChunked(true);

                Pump.createPump(req, cReq).start();

                req.endHandler(new VoidHandler() {
                    public void handle() {
                        cReq.end();
                    }
                });
                req.exceptionHandler(new Handler<Throwable>() {
                    public void handle(Throwable event) {
                        log.error(event.getMessage());
                        cReq.end();
                        if (!(event instanceof java.lang.IllegalStateException)) {
                            req.response().setStatusCode(502);
                            req.response().setStatusMessage("Bad Gateway");
                            req.response().end();
                        }
                    }
                });
            }

        }).listen(8080);
        log.info(String.format("Instance %s started", this.toString()));
    }
}
