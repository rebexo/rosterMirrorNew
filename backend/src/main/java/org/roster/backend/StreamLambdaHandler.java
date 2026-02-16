package org.roster.backend;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Der Haupteingang für AWS Lambda.
 * Nimmt den AWS-Event-Stream entgegen und leitet ihn in die Spring Boot Anwendung um.
 */
public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            // Hier sagen wir dem AWS-Mantel: "Starte unsere normale Spring Boot App!"
            // WICHTIG: Setze hier das Profil auf "web", damit die Dummy-DB und der SQS-Adapter laden
            System.setProperty("spring.profiles.active", "web,dev");

            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(BackendApplication.class);

        } catch (ContainerInitializationException e) {
            // Wenn Spring Boot nicht hochfährt, stürzt Lambda kontrolliert ab
            e.printStackTrace();
            throw new RuntimeException("Konnte das Spring Boot Application Context nicht initialisieren", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        // Hier passiert die Magie: Der AWS-Stream wird in einen HTTP-Request für deine Controller übersetzt
        handler.proxyStream(inputStream, outputStream, context);
    }
}