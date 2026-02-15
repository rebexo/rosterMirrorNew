package org.roster.backend.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roster.backend.application.dto.ShiftDto;
import org.roster.backend.application.port.out.ShiftPort;
import org.roster.backend.domain.Shift;
import org.roster.backend.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test class for the {@link ShiftService} class.
 * This class uses Mockito for mocking dependencies and JUnit 5 for test organization and assertions.
 */
@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftPort shiftPort;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ShiftService shiftService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        // simuliere eingeloggten User für jeden Test
        currentUser = User.builder().id(1L).username("testuser").build();

        // SecurityContext Mocking Setup
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createShift_ShouldSaveAndReturnDto() {
        // Arrange
        ShiftDto inputDto = new ShiftDto();
        inputDto.setName("Frühschicht");
        inputDto.setStartTime(LocalTime.of(8, 0));
        inputDto.setEndTime(LocalTime.of(16, 0));

        // Mock: Wenn shiftPort.saveShift aufgerufen wird, gib eine Shift mit ID zurück
        when(shiftPort.saveShift(any(Shift.class))).thenAnswer(invocation -> {
            Shift s = invocation.getArgument(0);
            s.setId(UUID.randomUUID()); // simuliere DB-ID Generierung
            return s;
        });

        // Act
        ShiftDto result = shiftService.createShift(inputDto);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Frühschicht", result.getName());
        verify(shiftPort).saveShift(any(Shift.class)); // verifiziere Aufruf
    }

    @Test
    void deleteShift_ShouldThrowException_WhenUserIsNotOwner() {
        // Arrange
        UUID shiftId = UUID.randomUUID();
        User otherUser = User.builder().id(2L).username("other").build(); // anderer User

        Shift existingShift = new Shift();
        existingShift.setId(shiftId);
        existingShift.setPlanner(otherUser); // gehört NICHT currentUser

        when(shiftPort.findShiftById(shiftId)).thenReturn(Optional.of(existingShift));

        // Act & Assert
        assertThrows(SecurityException.class, () -> shiftService.deleteShift(shiftId));
        verify(shiftPort, never()).deleteShift(any()); // darf nicht gelöscht werden
    }

    @Test
    void deleteShift_ShouldDelete_WhenUserIsOwnerAndNotUsedInTemplate() {
        // Arrange
        UUID shiftId = UUID.randomUUID();
        Shift existingShift = new Shift();
        existingShift.setId(shiftId);
        existingShift.setPlanner(currentUser); // gehört currentUser

        when(shiftPort.findShiftById(shiftId)).thenReturn(Optional.of(existingShift));
        when(shiftPort.isShiftUsedInTemplate(shiftId)).thenReturn(false); // nicht in Verwendung

        // Act
        shiftService.deleteShift(shiftId);

        // Assert
        verify(shiftPort).deleteShift(existingShift);
    }
}