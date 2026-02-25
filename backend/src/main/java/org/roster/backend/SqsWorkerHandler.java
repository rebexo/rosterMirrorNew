package org.roster.backend;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.roster.backend.application.port.in.iSolverService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

/**
 * Handles incoming AWS SQS events and processes messages using a defined workflow.
 *
 * This class serves as a Lambda function that listens to SQS messages. It initializes
 * a Spring Boot context, retrieves required beans, and processes each message in the
 * events received. The primary task is to handle schedule solving requests by parsing
 * the message, extracting necessary data, and invoking the appropriate service.
 *
 * Key Responsibilities:
 * - Reads configuration profiles from the environment to set up Spring Boot.
 * - Processes each SQS message by extracting the required JSON payload.
 * - Invokes the solver service for solving schedules based on the provided schema IDs.
 * - Handles errors gracefully, allowing AWS SQS to retry failed messages.
 *
 * Notes:
 * - This handler relies on Spring's ApplicationContext for dependency injection.
 * - Messages are expected in JSON format and should contain at least:
 *   - `schemaId`: A UUID representing the schema to solve.
 *   - `action`: The operation to perform (e.g., `SOLVE_SCHEDULE`).
 * - It is designed to handle potentially long-running processes (up to AWS Lambda's time limits).
 *
 * Error Handling:
 * - Any exceptions during message processing will lead to retries by AWS SQS.
 * - Proper logging is implemented for traceability and debugging.
 */
public class SqsWorkerHandler implements RequestHandler<SQSEvent, Void> {

    private static final ApplicationContext applicationContext;

    static {
        // Profil Umgebung lesen (template.yaml setzt dies auf "worker,dev")
        String profiles = System.getenv("SPRING_PROFILES_ACTIVE");
        if (profiles != null) {
            System.setProperty("spring.profiles.active", profiles);
        }

        // Spring Boot starten (Einmaliger Kaltstart für den Worker)
        applicationContext = SpringApplication.run(BackendApplication.class);
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        //hole SolverService und mapper aus laufendem context
        iSolverService solverService = applicationContext.getBean(iSolverService.class);
        ObjectMapper mapper = applicationContext.getBean(ObjectMapper.class);

        // gehe durch alle neuen Nachrichten und Queue
        for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
            try {
                context.getLogger().log("Lese SQS Auftrag: " + message.getBody());

                // JSON entpacken (muster: {"schemaId": "...", "action": "SOLVE_SCHEDULE"})
                JsonNode jsonNode = mapper.readTree(message.getBody());
                UUID schemaId = UUID.fromString(jsonNode.get("schemaId").asText());

                // Solver starten (wieder blockierend)
                solverService.solve(schemaId);

                context.getLogger().log("Schichtplan für Schema " + schemaId + " erfolgreich berechnet und gespeichert!");

            } catch (Exception e) {
                context.getLogger().log("Fehler im FaaS Worker: " + e.getMessage());
                // bei exception versucht AWS SQS es nochmal
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}