package lbaas.vxproxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketVersion;
import org.vertx.java.core.json.JsonObject;

public class BackEndServer {

    private static final long MAX_DISABLE_LIMIT = 86400000L; // 1 day
    private boolean ok = true;
    private long lastCheck = 0L;
    private long disableLimit = 5000L; // microseconds
    private int multiplier = 10;
    private boolean autoEnable = false;
    private final HttpClient client;

    public BackEndServer(final HttpClient client, final JsonObject config) {
        this.client = client;
        this.ok = true;
        client.exceptionHandler(new Handler<Throwable>() {
                    public void handle(Throwable event) {
                ok = false;
            }
        });

//        // Configuration
//        client.setConnectTimeout(config.getInteger("clientConnectTimeout",client.getConnectTimeout()));
//        client.setKeepAlive(config.getBoolean("clientKeepAlive",client.isKeepAlive()));
//        client.setMaxPoolSize(config.getInteger("clientMaxPoolSize",client.getMaxPoolSize()));
//        Integer receiveBufferSize = config.getInteger("clientReceiveBufferSize",client.getReceiveBufferSize());
//        if (receiveBufferSize!=null && receiveBufferSize>=1) {
//            client.setReceiveBufferSize(receiveBufferSize);
//        }
//        client.setReuseAddress(config.getBoolean("clientReuseAddress",client.isReuseAddress()));
//        client.setSoLinger(config.getInteger("clientSoLinger",client.getSoLinger()));
//        client.setSSL(config.getBoolean("clientSSL",client.isSSL()));
//        client.setTCPKeepAlive(config.getBoolean("clientTCPKeepAlive",client.isTCPKeepAlive()));
//        client.setTCPNoDelay(config.getBoolean("clientTCPNoDelay",client.isTCPNoDelay()));
//        client.setTrustAll(config.getBoolean("clientTrustAll",client.isTrustAll()));
//        client.setTryUseCompression(config.getBoolean("clientTryUseCompression",client.getTryUseCompression()));
//        client.setUsePooledBuffers(config.getBoolean("clientUsePooledBuffers",client.isUsePooledBuffers()));
//        client.setVerifyHost(config.getBoolean("clientVerifyHost",client.isVerifyHost()));
    }

    public HttpClient getClient() {
        return client;
    }

    public boolean isOk() {
        if (autoEnable) {
            long now = System.currentTimeMillis();
            // check again after X seconds
            if (now > (lastCheck+disableLimit)) {
                ok = true;
                lastCheck = now;
                disableLimit *= multiplier;
                if (disableLimit > MAX_DISABLE_LIMIT) {
                    disableLimit = MAX_DISABLE_LIMIT;
                }            }
        }
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setAutoEnable(boolean autoEnable) {
        this.autoEnable = autoEnable;
    }

    public String toString() {
        return String.format("%s:%d", getHost(), getPort());
    }

    public HttpClient setTrustAll(boolean trustAll) {
        return client.setTrustAll(trustAll);
    }

    public boolean isTrustAll() {
        return client.isTrustAll();
    }

    public HttpClient setSSL(boolean ssl) {
        return client.setSSL(ssl);
    }

    public boolean isSSL() {
        return client.isSSL();
    }

    public HttpClient setKeyStorePath(String path) {
        return client.setKeyStorePath(path);
    }

    public String getKeyStorePath() {
        return client.getKeyStorePath();
    }

    public HttpClient setKeyStorePassword(String pwd) {
        return client.setKeyStorePassword(pwd);
    }

    public String getKeyStorePassword() {
        return client.getKeyStorePassword();
    }

    public HttpClient setTrustStorePath(String path) {
        return client.setTrustStorePath(path);
    }

    public String getTrustStorePath() {
        return client.getTrustStorePath();
    }

    public HttpClient setTrustStorePassword(String pwd) {
        return client.setTrustStorePassword(pwd);
    }

    public String getTrustStorePassword() {
        return client.getTrustStorePassword();
    }

    public HttpClient setTCPNoDelay(boolean tcpNoDelay) {
        return client.setTCPNoDelay(tcpNoDelay);
    }

    public HttpClient setTCPKeepAlive(boolean keepAlive) {
        return client.setTCPKeepAlive(keepAlive);
    }

    public HttpClient setSoLinger(int linger) {
        return client.setSoLinger(linger);
    }

    public HttpClient setUsePooledBuffers(boolean pooledBuffers) {
        return client.setUsePooledBuffers(pooledBuffers);
    }

    public boolean isTCPNoDelay() {
        return client.isTCPNoDelay();
    }

    public boolean isTCPKeepAlive() {
        return client.isTCPKeepAlive();
    }

    public int getSoLinger() {
        return client.getSoLinger();
    }

    public boolean isUsePooledBuffers() {
        return client.isUsePooledBuffers();
    }

    public HttpClient setSendBufferSize(int size) {
        return client.setSendBufferSize(size);
    }

    public HttpClient setReceiveBufferSize(int size) {
        return client.setReceiveBufferSize(size);
    }

    public HttpClient setReuseAddress(boolean reuse) {
        return client.setReuseAddress(reuse);
    }

    public HttpClient setTrafficClass(int trafficClass) {
        return client.setTrafficClass(trafficClass);
    }

    public int getSendBufferSize() {
        return client.getSendBufferSize();
    }

    public int getReceiveBufferSize() {
        return client.getReceiveBufferSize();
    }

    public boolean isReuseAddress() {
        return client.isReuseAddress();
    }

    public int getTrafficClass() {
        return client.getTrafficClass();
    }

    public HttpClient exceptionHandler(Handler<Throwable> handler) {
        return client.exceptionHandler(handler);
    }

    public HttpClient setMaxPoolSize(int maxConnections) {
        return client.setMaxPoolSize(maxConnections);
    }

    public int getMaxPoolSize() {
        return client.getMaxPoolSize();
    }

    public HttpClient setKeepAlive(boolean keepAlive) {
        return client.setKeepAlive(keepAlive);
    }

    public boolean isKeepAlive() {
        return client.isKeepAlive();
    }

    public HttpClient setPort(int port) {
        return client.setPort(port);
    }

    public int getPort() {
        return client.getPort();
    }

    public HttpClient setHost(String host) {
        return client.setHost(host);
    }

    public String getHost() {
        return client.getHost();
    }

    public HttpClient connectWebsocket(String uri, Handler<WebSocket> wsConnect) {
        return client.connectWebsocket(uri, wsConnect);
    }

    public HttpClient connectWebsocket(String uri, WebSocketVersion wsVersion,
            Handler<WebSocket> wsConnect) {
        return client.connectWebsocket(uri, wsVersion, wsConnect);
    }

    public HttpClient connectWebsocket(String uri, WebSocketVersion wsVersion,
            MultiMap headers, Handler<WebSocket> wsConnect) {
        return client.connectWebsocket(uri, wsVersion, headers, wsConnect);
    }

    public HttpClient getNow(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.getNow(uri, responseHandler);
    }

    public HttpClient getNow(String uri, MultiMap headers,
            Handler<HttpClientResponse> responseHandler) {
        return client.getNow(uri, headers, responseHandler);
    }

    public HttpClientRequest options(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.options(uri, responseHandler);
    }

    public HttpClientRequest get(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.get(uri, responseHandler);
    }

    public HttpClientRequest head(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.head(uri, responseHandler);
    }

    public HttpClientRequest post(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.post(uri, responseHandler);
    }

    public HttpClientRequest put(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.put(uri, responseHandler);
    }

    public HttpClientRequest delete(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.delete(uri, responseHandler);
    }

    public HttpClientRequest trace(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.trace(uri, responseHandler);
    }

    public HttpClientRequest connect(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.connect(uri, responseHandler);
    }

    public HttpClientRequest patch(String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.patch(uri, responseHandler);
    }

    public HttpClientRequest request(String method, String uri,
            Handler<HttpClientResponse> responseHandler) {
        return client.request(method, uri, responseHandler);
    }

    public void close() {
        close();
    }

    public HttpClient setVerifyHost(boolean verifyHost) {
        return client.setVerifyHost(verifyHost);
    }

    public boolean isVerifyHost() {
        return client.isVerifyHost();
    }

    public HttpClient setConnectTimeout(int timeout) {
        return client.setConnectTimeout(timeout);
    }

    public int getConnectTimeout() {
        return client.getConnectTimeout();
    }

    public HttpClient setTryUseCompression(boolean tryUseCompression) {
        return client.setTryUseCompression(tryUseCompression);
    }

    public boolean getTryUseCompression() {
        return client.getTryUseCompression();
    }

    public HttpClient setMaxWebSocketFrameSize(int maxSize) {
        return client.setMaxWebSocketFrameSize(maxSize);
    }

    public int getMaxWebSocketFrameSize() {
        return client.getMaxWebSocketFrameSize();
    }
}
