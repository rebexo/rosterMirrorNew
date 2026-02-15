package org.roster.backend.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.roster.backend.application.dto.ShiftDto;
import org.roster.backend.application.port.in.iShiftService;
import org.roster.backend.application.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the ShiftController layer.
 * <p>
 * This class is responsible for testing the API endpoints of the {@link ShiftController}.
 * It uses MockMvc and simulates HTTP requests to validate the behavior and responses of
 * the controller layer. It also integrates mocked dependencies such as the {@link iShiftService}
 * to isolate the tests from the business logic.
 * </p>
 *
 * <b>Annotations:</b>
 * - {@code @WebMvcTest}: Loads only the web layer for testing.
 * - {@code @AutoConfigureMockMvc}: Automatically sets up MockMvc for testing.
 * - {@code @MockBean}: Replaces the real implementation of {@link iShiftService} with a mock for testing purposes.
 * - {@code @WithMockUser}: Simulates a logged-in user for security-related tests.
 * <b>Purpose of this Test Class:</b>
 * - Validate the success and failure scenarios for the ShiftController endpoints.
 * - Ensure proper integration between the controller and the service layer using mocks.
 * - Confirm that security constraints, if any, are correctly applied.
 * - Test controller behavior such as status codes, responses, and JSON structure.
 */
@WebMvcTest(ShiftController.class)
@AutoConfigureMockMvc
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private iShiftService shiftService; // Mock des Services

    @Autowired
    private ObjectMapper objectMapper;

    // --- Mocks für Security (JwtAuthFilter) ---
    // werden benötigt, damit der ApplicationContext startet
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "user") // simuliert eingeloggten User (wichtig für Security)
    void createShift_ShouldReturnCreatedShift() throws Exception {
        // Arrange
        ShiftDto inputDto = new ShiftDto();
        inputDto.setName("Spätschicht");
        inputDto.setStartTime(LocalTime.of(14, 0));
        inputDto.setEndTime(LocalTime.of(22, 0));

        ShiftDto outputDto = new ShiftDto();
        outputDto.setId(UUID.randomUUID());
        outputDto.setName("Spätschicht");
        outputDto.setStartTime(LocalTime.of(14, 0));
        outputDto.setEndTime(LocalTime.of(22, 0));

        when(shiftService.createShift(any(ShiftDto.class))).thenReturn(outputDto);

        // Act & Assert
        mockMvc.perform(post("/api/shifts")
                        .with(csrf()) // CSRF-Token mitschicken
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated()) // erwartet HTTP 201
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Spätschicht"));
    }
}