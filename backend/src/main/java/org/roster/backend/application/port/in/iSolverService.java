package org.roster.backend.application.port.in;

import org.roster.backend.domain.ScheduleProposal;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Input port interface defining the use cases for automated schedule generation.
 * <p>
 * This interface acts as the entry point for triggering the optimization engine.
 * It abstracts the complex solving process into a simple transactional operation
 * that the web layer can invoke.
 * </p>
 * <b>Core Use Case:</b>
 * <ul>
 * <li>Triggering the calculation of a schedule proposal for a given schema.</li>
 * </ul>
 */
public interface iSolverService {
    @Transactional
    ScheduleProposal solve(UUID schemaId);
}
