package org.openidentityplatform.test.webserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.JBossLoggingAccessLogReceiver;
import io.undertow.util.HttpString;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

public class Main {

    final HttpClient client = HttpClient.newHttpClient();

    private void runServer() {
        final HttpHandler mainHandler = (exchange -> {
            Logger.getLogger(JBossLoggingAccessLogReceiver.DEFAULT_CATEGORY).info(exchange.getRequestHeaders());

            String accessToken = exchange.getRequestHeaders().getFirst("Authorization");
            if(accessToken == null) {
                String responseBody = htmlTemplate.formatted("No authorization header");
                exchange.setStatusCode(401);
                exchange.getResponseSender().send(responseBody);
            }
            final String message;
            Map<String, Object> data = getRemoteData(accessToken);
            if(data != null) {
                message = "email: %s, balance: %s".formatted(data.get("email"), data.get("balance"));
            } else {
                message = "no data, check access rights";
            }

            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "text/html");

            String responseBody = htmlTemplate.formatted(message);
            exchange.getResponseSender().send(responseBody);
        });

        final HttpHandler accessLogHandler = new AccessLogHandler(
                mainHandler,
                new JBossLoggingAccessLogReceiver(),
                "combined",
                JBossLoggingAccessLogReceiver.class.getClassLoader());

        Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(accessLogHandler)
                .build().start();
    }

    public Map<String, Object> getRemoteData(String accessToken) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://openig:8080/userinfo"))
                .header("Authorization", accessToken)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(responseBody, new TypeReference<>() {
                });
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Main().runServer();
    }


    final static String htmlTemplate = """
            <!DOCTYPE html>
            <html>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <link rel="icon" href="data:," />
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
                    integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
            <head>
            </head>
            <body>
                 <div class="container my-5">
                    <p>%s</p>
                 </div>
            </body>
            """;

}