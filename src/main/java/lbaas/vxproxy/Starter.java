package lbaas.vxproxy;

import lbaas.vxproxy.util.LoggerOnEventBus;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Starter extends Verticle {

  public void start() {

      // Application config
      JsonObject appConfig = container.config();
      JsonObject proxyConf = appConfig.getObject("proxy");
      JsonObject loggerConf = appConfig.getObject("logger");
      JsonObject routeTableConf = appConfig.getObject("route_table");

      // Run verticles
      Integer maxVerticles = (int) Math.round(Runtime.getRuntime().availableProcessors()*1.5);
      Integer proxyInstances = proxyConf != null && proxyConf.containsField("instances") ? proxyConf.getInteger("instances") : maxVerticles;
      Integer loggerInstances = loggerConf != null && loggerConf.containsField("instances") ? loggerConf.getInteger("instances") : 1;
      Integer routeTableInstances = routeTableConf != null && routeTableConf.containsField("instances") ? routeTableConf.getInteger("instances") : maxVerticles;

      container.deployVerticle("lbaas.vxproxy.Proxy", proxyConf, proxyInstances);
      container.deployVerticle("lbaas.vxproxy.VxLogger", loggerConf, loggerInstances);
      container.deployVerticle("lbaas.vxproxy.RouteTable", routeTableConf, routeTableInstances);

      LoggerOnEventBus log = new LoggerOnEventBus(vertx.eventBus());
      log.info(String.format("Instance %s started", this.toString()));

  }
}
