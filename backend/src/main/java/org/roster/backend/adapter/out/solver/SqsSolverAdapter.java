package org.roster.backend.adapter.out.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roster.backend.application.port.out.SolverPort;
import org.roster.backend.domain.ScheduleProposal;
import org.roster.backend.domain.ScheduleSchema;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

/**
 * Cloud-Adapter, der anstelle des lokalen Timefold-Solvers genutzt wird.
 * Er sendet eine JSON-Nachricht an eine AWS SQS-Warteschlange.
 */
@Slf4j
@Component
@Profile("web")
@RequiredArgsConstructor
public class SqsSolverAdapter implements SolverPort {

    // Spring gibt uns automatisch den ObjectMapper für JSON-Umwandlungen
    private final ObjectMapper objectMapper;

    // Später würden wir das in die application.properties auslagern
    private final String queueUrl = "https://sqs.eu-central-1.amazonaws.com/123456789012/RosterSolverQueue";

    @Override
    public ScheduleProposal solve(ScheduleSchema schema) {
        log.info("[SQS-ADAPTER] Bereite Nachricht für AWS SQS vor...");

        try {
            // 1. Nachricht als JSON aufbauen (DTO)
            Map<String, String> messageBody = Map.of(
                    "schemaId", schema.getId().toString(),
                    "action", "SOLVE_SCHEDULE"
            );

            // in JSON-String umwandeln
            String jsonPayload = objectMapper.writeValueAsString(messageBody);
            log.info("JSON Payload generiert: {}", jsonPayload);

            /* * 2. AWS SQS Client aufrufen
             * (Auskommentiert, bis echte AWS Credentials auf dem System liegen!)
             */

            // try (SqsClient sqsClient = SqsClient.builder().region(Region.EU_CENTRAL_1).build()) {
            //     SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
            //             .queueUrl(queueUrl)
            //             .messageBody(jsonPayload)
            //             .build();
            //
            //     sqsClient.sendMessage(sendMsgRequest);
            //     log.info("Nachricht erfolgreich an SQS Queue gesendet!");
            // }

            log.info("[HINWEIS] Das tatsächliche Senden an AWS ist aktuell deaktiviert (Sicherheitsmodus).");

        } catch (Exception e) {
            log.error("Fehler beim Generieren der SQS-Nachricht", e);
        }

        // null zurückgeben, weil worker ergebnis in die db schreibt
        return null;
    }
}