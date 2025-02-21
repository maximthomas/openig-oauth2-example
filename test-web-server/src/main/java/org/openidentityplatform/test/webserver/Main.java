package org.openidentityplatform.test.webserver;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.JBossLoggingAccessLogReceiver;
import io.undertow.util.HttpString;
import org.jboss.logging.Logger;

import java.util.Base64;

public class Main {

    final static HttpHandler mainHandler = (exchange -> {
        Logger.getLogger(JBossLoggingAccessLogReceiver.DEFAULT_CATEGORY).info(exchange.getRequestHeaders());

        String email = "undefined";
        String authJwt = exchange.getRequestHeaders().getFirst("Authorization");
        
        if(authJwt != null) {
            String[] chunks = authJwt.split("\\.");
            if(chunks.length == 3) {
                String payload = new String(Base64.getDecoder().decode(chunks[1]));
                email = payload.replaceAll(".*email\\\":\"(.*?)\".*", "$1");
            }
        }

        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        String responseBody = "{\"email\": \"%s\"}\n".formatted(email);
        exchange.getResponseSender().send(responseBody);
    });

    final static HttpHandler accessLogHandler = new AccessLogHandler(
            mainHandler,
            new JBossLoggingAccessLogReceiver(),
            "combined",
            JBossLoggingAccessLogReceiver.class.getClassLoader());

    public static void main(String[] args) {
        Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(accessLogHandler)
                .build().start();
    }


}