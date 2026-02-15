package org.roster.backend.application.port.in;

import org.roster.backend.application.dto.AvailabilityEntryDetailDto;
import org.roster.backend.application.dto.NewAvailabilityEntryDto;
import org.roster.backend.application.dto.PublicSchemaDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Input port interface defining the use cases for Availability management.
 * <p>
 * This interface handles the collection of staff constraints and preferences.
 * It supports operations for both the planner (viewing) and the employees (submitting).
 * </p>
 * <b>Core Use Cases:</b>
 * <ul>
 * <li>Submitting availability preferences (e.g., "I cannot work on Monday").</li>
 * <li>Retrieving aggregated availability data for a specific schema.</li>
 * </ul>
 */
public interface iAvailabilityService {
    @Transactional(readOnly = true)
    PublicSchemaDto getPublicSchema(String linkId);

    void submitAvailability(String linkId, NewAvailabilityEntryDto dto);

    // nur Lesezugriff
    @Transactional(readOnly = true)
    List<AvailabilityEntryDetailDto> getAvailabilityEntriesDetailsForSchema(UUID schemaId);

    // Schreibzugriff
    @Transactional
    void updateAvailabilityEntryTargetShiftCount(UUID entryId, Integer targetShiftCount);
}
