package org.shadows.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
class HealthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        handleResponse(httpExchange);
    }


    private void handleResponse(HttpExchange httpExchange) throws IOException {
        String healthResponse = "{\"status\":\"Ok\"}";
        OutputStream outputStream = httpExchange.getResponseBody();
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, healthResponse.length());
        outputStream.write(healthResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
