package org.roster.backend.application.port.out;

import org.roster.backend.domain.AvailabilityEntry;
import org.roster.backend.domain.ScheduleProposal;
import java.util.List;

/**
 * Defines an output port for exporting schedule-related data to an Excel file.
 * Adapters implementing this interface provide the logic to generate an Excel workbook
 * containing the roster details based on the given schedule proposal and availability data.
 */
public interface ExcelExportPort {
    byte[] generateRosterFile(ScheduleProposal proposal, List<AvailabilityEntry> availabilities);
}