package org.roster.backend.adapter.out.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roster.backend.application.port.out.SolverPort;
import org.roster.backend.domain.ScheduleProposal;
import org.roster.backend.domain.ScheduleSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

/**
 * The SqsSolverAdapter class is an implementation of the SolverPort interface used to integrate
 * AWS SQS (Simple Queue Service) for handling scheduling operations. This class prepares and
 * sends messages to an SQS queue to trigger schedule proposals, ensuring communication between
 * scheduling processes and external components via SQS.
 *
 * The adapter utilizes the following functionalities:
 * - JSON serialization using Jackson's ObjectMapper.
 * - Sends messages to an AWS SQS queue, configured by the queue URL.
 * - Logs key operations for monitoring and debugging.
 *
 * This class is intended to work under the "web" profile and leverages Spring's dependency injection
 * to manage required resources.
 *
 * An important note is that the return value is currently null, as the worker process writes results
 * directly to the database.
 */
@Slf4j
@Component
@Profile("web")
@RequiredArgsConstructor
public class SqsSolverAdapter implements SolverPort {

    // ObjectMapper für JSON-Umwandlungen von spring (takk!)
    private final ObjectMapper objectMapper;

    @Value("${SOLVER_QUEUE_URL}")
    private String queueUrl;

    @Override
    public ScheduleProposal solve(ScheduleSchema schema) {
        log.info("[SQS-ADAPTER] Bereite Nachricht für AWS SQS vor...");

        try {
            // JSON aufbauen (DTO)
            Map<String, String> messageBody = Map.of(
                    "schemaId", schema.getId().toString(),
                    "action", "SOLVE_SCHEDULE"
            );

            // in String umwandeln
            String jsonPayload = objectMapper.writeValueAsString(messageBody);
            log.info("JSON Payload generiert: {}", jsonPayload);

            // AWS SQS Client aufrufen
             try (SqsClient sqsClient = SqsClient.builder().region(Region.EU_CENTRAL_1).build()) {
                 SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                         .queueUrl(queueUrl)
                         .messageBody(jsonPayload)
                         .build();

                 sqsClient.sendMessage(sendMsgRequest);
                 log.info("Nachricht erfolgreich an SQS Queue gesendet!");
             }
        } catch (Exception e) {
            log.error("Fehler beim Generieren der SQS-Nachricht", e);
        }

        // null zurückgeben, weil worker ergebnis in die db schreibt
        return null;
    }
}