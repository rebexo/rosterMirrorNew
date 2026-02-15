package org.roster.backend.application.port.in;

import org.roster.backend.application.dto.ShiftDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Input port interface defining the use cases for Shift management.
 * <p>
 * This interface represents the boundary between the primary adapter (e.g., REST Controller)
 * and the application core. It defines all operations related to the creation, modification,
 * and retrieval of shift definitions.
 * </p>
 * <b>Core Use Cases:</b>
 * <ul>
 * <li>Creating and maintaining base shift definitions.</li>
 * <li>Retrieving shifts for specific planners or contexts.</li>
 * </ul>
 */
public interface iShiftService {
    ShiftDto createShift(ShiftDto shiftDto);

    @Transactional(readOnly = true)
    List<ShiftDto> getShiftsForCurrentUser();

    ShiftDto updateShift(UUID shiftId, ShiftDto shiftDto);

    void deleteShift(UUID shiftId);
}
