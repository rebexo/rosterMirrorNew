package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iSolverService;
import org.roster.backend.application.port.out.*;
import org.roster.backend.domain.*; // normale Entities
//import org.roster.backend.solver.domain.*; // Solver-Entities
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*; // Für Set
import java.util.UUID;

/**
 * Application service responsible for orchestrating the automated schedule generation process.
 * <p>
 * This service implements the use case defined in {@link iSolverService}, strictly adhering to
 * the Onion Architecture principles. It does not contain any solving logic itself but rather
 * delegates the heavy lifting to the infrastructure layer via the {@link SolverPort}.
 * </p>
 * <b>Workflow:</b>
 * <ol>
 * <li>Retrieves the necessary schema data via {@link SchemaPort}.</li>
 * <li>Delegates the optimization task to the {@link SolverPort} implementation.</li>
 * <li>Persists the generated result via {@link ProposalPort}.</li>
 * </ol>
 * <p>
 * This design ensures that the business logic remains decoupled from the specific choice of
 * the solver engine (e.g., Timefold, OR-Tools, custom algorithms).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SolverService implements iSolverService {

    private final SolverPort solverPort;      // nutzt Adapter
    private final SchemaPort schemaPort;
    private final ProposalPort proposalPort;  // speichert Ergebnis

    /**
     * Solves a schedule for a given schema and returns the generated schedule proposal.
     * <p>
     * This method orchestrates the solving process, which involves fetching the schema data,
     * delegating the solving operation to a solver adapter, and persisting the result.
     * </p>
     *
     * @param schemaId the unique identifier of the schedule schema that needs to be solved
     * @return the generated {@link ScheduleProposal} containing the resulting schedule
     * @throws IllegalArgumentException if no schema is found for the given schemaId
     */
    @Transactional
    @Override
    public ScheduleProposal solve(UUID schemaId) {
        // Schema laden
        ScheduleSchema schema = schemaPort.findSchemaById(schemaId)
                .orElseThrow(() -> new IllegalArgumentException("Schema not found"));

        // Adapter aufrufen -> Adapter liefert fertig gebautes, aber noch nicht gespeichertes Proposal
        ScheduleProposal proposal = solverPort.solve(schema);

        // Ergebnis speichern
        return proposalPort.save(proposal);
    }
}