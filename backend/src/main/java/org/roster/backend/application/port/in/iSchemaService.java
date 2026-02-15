package org.roster.backend.application.port.in;

import org.roster.backend.application.dto.CalculatedShiftDto;
import org.roster.backend.application.dto.SchemaDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Input port interface defining the use cases for Schedule Schema management.
 * <p>
 * A "Schema" represents the structural definition of a specific planning period (e.g., "November 2024"),
 * including its valid date range and the templates assigned to it.
 * </p>
 * <b>Core Use Cases:</b>
 * <ul>
 * <li>Defining new planning periods.</li>
 * <li>Assigning weekly templates to specific date ranges within a schema.</li>
 * <li>Managing the lifecycle of a plan (e.g., locking data collection).</li>
 * </ul>
 */
public interface iSchemaService {
    SchemaDto createSchema(SchemaDto schemaDto);

    @Transactional(readOnly = true)
    List<SchemaDto> getSchemasForCurrentUser();

    @Transactional(readOnly = true)
    List<CalculatedShiftDto> getSchemaDetails(UUID schemaId);

    SchemaDto updateExpectedEntries(UUID schemaId, Integer count);

    @Transactional(readOnly = true)
    SchemaDto getSchemaById(UUID schemaId);
}
