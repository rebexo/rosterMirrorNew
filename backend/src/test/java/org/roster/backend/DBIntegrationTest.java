package org.roster.backend;

import org.junit.jupiter.api.Test;
import org.roster.backend.application.port.out.SchemaPort;
import org.roster.backend.application.port.out.UserPort;
import org.roster.backend.domain.ScheduleSchema;
import org.roster.backend.domain.User;
import org.roster.backend.domain.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DBIntegrationTest {

    @Autowired
    private SchemaPort schemaPort;

    @Autowired
    private UserPort userPort; // für Anlage Planner

    @Test
    void shouldSaveAndLoadSchemaInDatabase() {
        // 1. ARRANGE:
        // User/Planner für Schema anlegen
        User planner = User.builder()
                .username("testPlanner")
                .password("password")
                .role(Role.PLANNER)
                .build();

        planner = userPort.saveUser(planner);

        // Schema erstellen, Pflichtfelder füllen
        ScheduleSchema schema = new ScheduleSchema();
        schema.setName("Integrationstest");
        schema.setStartDate(LocalDate.now());
        schema.setEndDate(LocalDate.now().plusDays(7));

        // AvailabilityLinkID setzen
        schema.setAvailabilityLinkID(UUID.randomUUID().toString());

        // Planner setzen
        schema.setPlanner(planner);

        // 2. ACT: Schema speichern
        schemaPort.saveSchema(schema);

        // 3. ASSERT:
        List<ScheduleSchema> allSchemas = schemaPort.findAllSchemas();

        assertThat(allSchemas).isNotEmpty();
        assertThat(allSchemas)
                .anyMatch(s -> s.getName().equals("Integrationstest"));
    }
}
