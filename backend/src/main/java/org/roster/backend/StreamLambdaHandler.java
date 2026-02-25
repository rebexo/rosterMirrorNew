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
 * Handles requests for AWS Lambda functions by leveraging the Spring Boot framework.
 *
 * This class serves as a RequestStreamHandler implementation designed to translate
 * AWS Lambda stream input into HTTP requests, which are then routed to appropriate
 * Spring Boot controllers. It uses SpringBootLambdaContainerHandler to initialize and
 * manage the Spring Boot application context specifically for AWS Proxy requests.
 *
 * Key Responsibilities:
 * - Initializes the Spring Boot application context during the static block execution.
 * - Reads and applies active Spring profiles from the environment.
 * - Provides a generic handleRequest implementation to process Lambda stream requests
 *   and forward them as HTTP requests to the Spring Boot application.
 *
 * Initialization Details:
 * - The active Spring profiles are determined by the "SPRING_PROFILES_ACTIVE" environment
 *   variable, defaulting to "web,dev" for local testing if unset.
 * - Uses the BackendApplication class to bootstrap the Spring Boot application.
 * - Handles exceptions during context initialization by logging and throwing a RuntimeException
 *   to terminate the Lambda function gracefully.
 *
 * Dependencies and Usage:
 * - Relies on the aws-serverless-java-container library for handling AWS Proxy requests.
 * - The SpringBootLambdaContainerHandler adapts the Spring web infrastructure for the serverless environment.
 *
 * Error Handling:
 * - If the Spring Boot application context fails to initialize, the Lambda function will
 *   terminate with an appropriate runtime exception.
 */
public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            // lese profil aus template.yaml
            String profiles = System.getenv("SPRING_PROFILES_ACTIVE");
            if (profiles != null) {
                System.setProperty("spring.profiles.active", profiles);
            } else {
                System.setProperty("spring.profiles.active", "web,dev"); // Fallback für lokales Testen
            }

            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(BackendApplication.class);

        } catch (ContainerInitializationException e) {
            // wenn Spring Boot nicht hochfährt, stürzt Lambda kontrolliert ab
            e.printStackTrace();
            throw new RuntimeException("Konnte das Spring Boot Application Context nicht initialisieren", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        // übersetzung AWS-Stream in HTTP-Request für Controller
        handler.proxyStream(inputStream, outputStream, context);
    }
}