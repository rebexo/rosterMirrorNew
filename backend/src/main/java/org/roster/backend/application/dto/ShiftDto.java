package org.roster.backend.application.dto;

import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ShiftDto {
    private UUID id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}