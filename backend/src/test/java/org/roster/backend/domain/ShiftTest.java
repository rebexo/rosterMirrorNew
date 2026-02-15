package org.roster.backend.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for the {@link Shift} entity to verify the behavior of the
 * {@link Shift#isOvernightShift()} method.
 */
class ShiftTest {

    @Test
    void isOvernightShift_ShouldReturnTrue_WhenEndBeforeStart() {
        Shift shift = new Shift();
        shift.setStartTime(LocalTime.of(22, 0));
        shift.setEndTime(LocalTime.of(6, 0)); // Endet am nächsten Tag

        assertTrue(shift.isOvernightShift());
    }

    @Test
    void isOvernightShift_ShouldReturnFalse_WhenEndAfterStart() {
        Shift shift = new Shift();
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(16, 0));

        assertFalse(shift.isOvernightShift());
    }
}