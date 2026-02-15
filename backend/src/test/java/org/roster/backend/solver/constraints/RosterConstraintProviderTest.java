package org.roster.backend.solver.constraints;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import org.junit.jupiter.api.Test;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.roster.backend.solver.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

class RosterConstraintProviderTest {

    // Initialisiert den Verifier für deinen Provider.
    ConstraintVerifier<RosterConstraintProvider, RosterSolution> constraintVerifier =
            ConstraintVerifier.build(new RosterConstraintProvider(), RosterSolution.class, ShiftAssignment.class);

    @Test
    void employeeUnavailable_ShouldPenalize() {
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 2, 1);

        // 1. Mitarbeiter (Target 1, damit Min/Max Regeln nicht feuern)
        Employee employee = new Employee(employeeId, "Hans", 1);

        // 2. Verfügbarkeit auf UNAVAILABLE
        Availability availability = new Availability(employeeId, date, AvailabilityStatus.UNAVAILABLE);

        // 3. Schicht und Zuweisung
        Shift shift = new Shift(UUID.randomUUID(), "Früh", date, LocalTime.of(8, 0), LocalTime.of(16, 0), UUID.randomUUID());
        ShiftAssignment assignment = new ShiftAssignment(UUID.randomUUID(), shift, employee);

        // 4. Prüfung
        // WICHTIG: Da wir den Gesamt-Score prüfen, müssen wir die Strafe als MINUS-Wert angeben.
        // Eine "Hard Penalty" von 10 bedeutet einen Score von "-10 Hard".
        constraintVerifier.verifyThat()
                .given(employee, availability, shift, assignment)
                .scores(HardSoftScore.ofHard(-10));
    }

    @Test
    void employeeAvailable_ShouldNotPenalize() {
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 2, 1);

        Employee employee = new Employee(employeeId, "Hans", 1);

        // Kein Availability-Objekt = Mitarbeiter ist verfügbar (Standardannahme)

        Shift shift = new Shift(UUID.randomUUID(), "Früh", date, LocalTime.of(8, 0), LocalTime.of(16, 0), UUID.randomUUID());
        ShiftAssignment assignment = new ShiftAssignment(UUID.randomUUID(), shift, employee);

        // Prüfung: Score sollte 0 sein (keine Strafen).
        constraintVerifier.verifyThat()
                .given(employee, shift, assignment)
                .scores(HardSoftScore.ZERO);
    }

    @Test
    void penalizeUnassignedShifts_ShouldPenalize_WhenEmployeeIsNull() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        Shift shift = new Shift(UUID.randomUUID(), "Früh", date, LocalTime.of(8, 0), LocalTime.of(16, 0), UUID.randomUUID());

        // Zuweisung OHNE Mitarbeiter (null)
        ShiftAssignment assignment = new ShiftAssignment(UUID.randomUUID(), shift, null);

        // Die Regel 'penalizeUnassignedShifts' gibt 100 Soft-Punkte Strafe.
        // Das entspricht einem Score von "0 Hard, -100 Soft".
        constraintVerifier.verifyThat()
                .given(shift, assignment)
                .scores(HardSoftScore.ofSoft(-100));
    }
}