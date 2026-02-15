package org.roster.backend.application.port.in;

import org.roster.backend.application.dto.ProposalDetailDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Input port interface defining the use cases for Schedule Proposal management.
 * <p>
 * A "Proposal" is the output of the solver engine—a candidate for the final roster.
 * This interface allows the retrieval and evaluation of these generated plans.
 * </p>
 * <b>Core Use Cases:</b>
 * <ul>
 * <li>Fetching generated proposals for review.</li>
 * <li>(Future) Approving a proposal to become the final published plan.</li>
 * </ul>
 */
public interface iProposalService {
    /**
     * Retrieves the details of a specific schedule proposal based on its unique identifier.
     *
     * @param proposalId the unique identifier of the proposal to retrieve
     * @return a {@code ProposalDetailDto} containing detailed information about the proposal,
     *         including the schema name, employee summaries, and assigned shifts
     */
    ProposalDetailDto getProposalDetails(UUID proposalId);

    Optional<UUID> getLatestProposalIdBySchemaId(UUID schemaId);
}
