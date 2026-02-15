package org.roster.backend.application.port.in;

import org.roster.backend.application.dto.TemplateDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Input port interface defining the use cases for Weekly Template management.
 * <p>
 * Templates allow planners to define reusable patterns of shifts (e.g., "Standard Week", "Holiday Week")
 * that can be applied to multiple schedule schemas. This interface handles the CRUD operations
 * for these templates and their associated shift positions.
 * </p>
 */
public interface iTemplateService {
    TemplateDto createTemplate(TemplateDto templateDto);

    @Transactional(readOnly = true)
    List<TemplateDto> getTemplatesForCurrentUser();

    // UPDATE
    TemplateDto updateTemplate(UUID templateId, TemplateDto templateDto);

    // DELETE
    void deleteTemplate(UUID templateId);
}
