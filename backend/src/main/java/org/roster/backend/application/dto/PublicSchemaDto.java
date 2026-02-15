package org.roster.backend.application.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PublicSchemaDto {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}