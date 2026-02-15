package org.roster.backend.application.port.in;

import java.io.IOException;
import java.util.UUID;

/**
 * Interface defining the contract for exporting roster-related data to Excel files.
 *
 * This service facilitates the generation of Excel documents that encapsulate
 * important details tied to a specific roster proposal. Implementations of this
 * interface are responsible for accessing the necessary data and generating
 * the resultant Excel files.
 */
public interface iExportService {
    /**
     * Generates an Excel file for the given proposal ID.
     *
     * @param proposalId the unique identifier of the proposal for which the Excel file is generated
     * @return a byte array representing the generated Excel file
     * @throws IOException if an error occurs during the file generation process
     */
    byte[] generateProposalExcel(UUID proposalId) throws IOException;
}