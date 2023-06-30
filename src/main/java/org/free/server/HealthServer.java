package org.free.server;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.free.AppProperties;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Health server
 *
 * @author bayura-ea
 */
@Slf4j
public class HealthServer implements Closeable {
    private final HttpServer server;

    public HealthServer(AppProperties properties) throws IOException {
        log.info("Starting http server...");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",
                        properties.serverPort()),
                0);
        server.createContext("/health", new HealthHandler());
        server.start();
        log.info("Starting http server...done");
    }

    @Override
    public void close() {
        server.stop(5);
    }
}
