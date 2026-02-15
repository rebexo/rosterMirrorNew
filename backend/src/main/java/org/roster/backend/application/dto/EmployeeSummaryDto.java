package org.roster.backend.application.dto;

import lombok.Data;
@Data
public class EmployeeSummaryDto {
    private String staffName;
    private int targetCount; // Anzahl Zielschichten
    private int actualCount; // Anzahl tatsächliche Schichten
}