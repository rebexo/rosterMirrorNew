package org.roster.backend.config;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.out.persistence.*;
import org.roster.backend.domain.Shift;
import org.roster.backend.domain.TemplateShift;
import org.roster.backend.domain.User;
import org.roster.backend.domain.WeeklyTemplate;
import org.roster.backend.domain.enums.Role;
import org.roster.backend.adapter.out.persistence.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.roster.backend.domain.*;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth; // für nächsten Monat
import java.util.UUID;

@Component
@Profile("dev") // nur im dev-Profile werden DAten angelegt
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final WeeklyTemplateRepository weeklyTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final ScheduleSchemaRepository scheduleSchemaRepository;
    private final AvailabilityEntryRepository availabilityEntryRepository;
    // ggf fürs Löschen:
    private final SchemaTemplateAssignmentRepository schemaTemplateAssignmentRepository;
    private final AvailabilityDetailRepository availabilityDetailRepository;
    private final TemplateShiftRepository templateShiftRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- Starting Data Seeder ---");

        if (userRepository.count() > 0) {
            System.out.println("--- Data Seeding Skipped (Database already contains data) ---");
            return;
        }

        // 1. Bestehende Daten löschen, um einen sauberen Start zu garantieren
//        availabilityDetailRepository.deleteAll();
//        availabilityEntryRepository.deleteAll();
//        schemaTemplateAssignmentRepository.deleteAll();
//        scheduleSchemaRepository.deleteAll();
//        templateShiftRepository.deleteAll(); // Shifts innerhalb von Templates löschen
//        weeklyTemplateRepository.deleteAll();
//        shiftRepository.deleteAll();
//        userRepository.deleteAll();
//        weeklyTemplateRepository.deleteAll();
//        shiftRepository.deleteAll();
//        userRepository.deleteAll();


        // 1. User
        User devUser = createUser("Rebecca", Role.PLANNER);
        User devUser2 = createUser("Louise", Role.PLANNER);

        System.out.println("Created User: " + devUser.getUsername());
        System.out.println("Created User: " + devUser2.getUsername());

        // 2. Shifts
        Shift kaffeeFrueh = createShift("Kaffee Früh", "08:30", "16:00", devUser);
        Shift kuecheFrueh = createShift("Küche Früh", "08:15", "15:00", devUser);
        Shift grau = createShift("Grau", "10:00", "17:00", devUser);
        Shift kaffeeSpaet = createShift("Kaffee Spät", "16:00", "22:00", devUser);
        Shift kuecheSpaet = createShift("Küche Spät", "15:00", "21:00", devUser);
        Shift service = createShift("Service", "09:00", "17:00", devUser);
        Shift kueche2 = createShift("KücheZwei", "10:00", "18:00", devUser);

        Shift fruehCafe = createShift("Cafe Fete Frühschicht", "06:00", "14:00", devUser);
        Shift grauCafe = createShift("Cafe Fete Knecht", "10:30", "17:00", devUser);
        Shift spaetCafe = createShift("Cafe Fete Spät", "14:00", "22:00", devUser);
        Shift spaetBar = createShift("Cafe Fete Spätschicht", "20:00", "23:59", devUser);

        Shift fruehGinst = createShift("Frühschicht Musenhain", "16:30", "01:00", devUser);
        Shift spaetGinst = createShift("Spätschicht Musenhain", "19:30", "03:00", devUser);
        Shift grauGinst = createShift("Hilfsschicht Musenhain", "20:00", "23:30", devUser);

        shiftRepository.saveAll(List.of(kaffeeFrueh, kuecheFrueh, grau, kaffeeSpaet, kuecheSpaet, service, kueche2,
                fruehCafe, grauCafe, spaetCafe, spaetBar, fruehGinst, spaetGinst, grauGinst));


        System.out.println("Created " + shiftRepository.count() + " Shifts.");

        // 3. Standard-Templates basteln

        // -- Kraweel --
        WeeklyTemplate kraweel = createWeeklyTemplate("Kraweel-Woche", devUser, "Taubtrüber Ginst am Musenhain");
        List<Shift> kraweelDayShifts = List.of(kaffeeFrueh, kuecheFrueh, grau, kaffeeSpaet, kuecheSpaet);
        List<String> kraweelNames = List.of("Kaffee Früh", "Küche Früh", "Grau", "Kaffee Spät", "Küche Spät");

        for (int day = 0; day < 7; day++) {
            for (int s = 0; s < kraweelDayShifts.size(); s++) {
                kraweel
                        .getShifts()
                        .add(createTemplateShift(day, kraweelNames.get(s), kraweelDayShifts.get(s), kraweel));
            }
        }
        weeklyTemplateRepository.save(kraweel);
        System.out.println("Created Template: " + kraweel.getName());


        // --- Default Bar ---
        WeeklyTemplate standardWocheBar = createWeeklyTemplate("Defaultbelegung Bar", devUser, "Normale Wochenbelegung Bar");
        for (int i = 0; i < 7; i++) {
            standardWocheBar
                    .getShifts()
                    .add(createTemplateShift(i, "Barkeeper", spaetBar, standardWocheBar));
        }
        weeklyTemplateRepository.save(standardWocheBar);
        System.out.println("Created Template: " + standardWocheBar.getName());


        // --- Musenhain ---
        WeeklyTemplate standardWocheGinst = createWeeklyTemplate("Standardwoche Musenhain", devUser, "...");
        // Mo-Mi & So: nur Barkeep
        for (int day : List.of(0, 1, 2, 6)) {
            standardWocheGinst.getShifts().add(
                    createTemplateShift(day, "Barkeeper allein", fruehGinst, standardWocheGinst)
            );
        }
        // Do: Früh & Spät
        standardWocheGinst.getShifts().add(createTemplateShift(3, "Barkeeper früh", fruehGinst, standardWocheGinst));
        standardWocheGinst.getShifts().add(createTemplateShift(3, "Barkeeper spät", spaetGinst, standardWocheGinst));
        // Fr & Sa: Früh, Spät & Grau
        for (int day : List.of(4, 5)) {
            standardWocheGinst.getShifts().add(createTemplateShift(day, "Barkeeper früh", fruehGinst, standardWocheGinst));
            standardWocheGinst.getShifts().add(createTemplateShift(day, "Barkeeper spät", spaetGinst, standardWocheGinst));
            standardWocheGinst.getShifts().add(createTemplateShift(day, "Springer", grauGinst, standardWocheGinst));
        }
        weeklyTemplateRepository.save(standardWocheGinst);

        // 4. ScheduleSchemata

        YearMonth nextMonth = YearMonth.now().plusMonths(1);

        // --- some Bar ---
        ScheduleSchema nextMonthSchema = createSchema(
                "Dienstplan " + nextMonth,
                nextMonth,
                devUser,
                5,
                standardWocheBar);
        scheduleSchemaRepository.save(nextMonthSchema);
        System.out.println("Created ScheduleSchema: " + nextMonthSchema.getName() + " (ID: " + nextMonthSchema.getId() + ")");


        // --- Musenhain (Ginst) ---
        System.out.println("Creating ScheduleSchema Ginst...");

        ScheduleSchema nextMonthSchemaGinst = createSchema(
                "Dienstplan Ginst " + nextMonth.getMonth().name() + " " + nextMonth.getYear(),
                nextMonth,
                devUser,
                8, // expectedEntries
                standardWocheGinst
        );
        scheduleSchemaRepository.save(nextMonthSchemaGinst);
        System.out.println("Created ScheduleSchema: " + nextMonthSchemaGinst.getName());

        // --- kraweel ---
        System.out.println("Creating ScheduleSchema Kraweel");

        ScheduleSchema kraweelSchema = createSchema(
                "Dienstplan Kraweel " + nextMonth.getMonth().name(),
                nextMonth,
                devUser,
                14,
                kraweel
        );
        scheduleSchemaRepository.save(kraweelSchema);
        System.out.println("Created ScheduleSchema Kraweel " +  kraweelSchema.getName());


        // 6. AvailabilityEntries

        System.out.println("Creating AvailabilityEntries...");

        // Verfügbarkeiten für den Standard-Dienstplan (nextMonthSchema)
        createAvailability(nextMonthSchema, "Ford Prefect", "Kann am 10. erst ab 15 Uhr.", 8, List.of(3, 4));
        createAvailability(nextMonthSchema, "Frida Kahlo", "Bevorzuge Wochenenden", 6, List.of(9));

        // Verfügbarkeiten für Musenhain (nextMonthSchemaGinst)
        createAvailability(nextMonthSchemaGinst, "Korbi", "Kann am 10.11. erst ab 15 Uhr.", 8, List.of(3, 4, 5));
        createAvailability(nextMonthSchemaGinst, "Flora", "Sorry, viel weg diesen Monat", 5,
                List.of(0, 1, 2, 7, 8, 14, 15, 16, 9, 21, 22, 23, 28, 29, 30));
        createAvailability(nextMonthSchemaGinst, "Nymia", "Christmas is coming", 12, List.of(5, 9, 17, 24));
        createAvailability(nextMonthSchemaGinst, "Loy", "Loyle loyle loyle", 8,
                List.of(1, 2, 3, 4, 5, 10, 13, 14, 15, 16, 24, 25, 26));
        createAvailability(nextMonthSchemaGinst, "Fritzie", "Fritziele Fritziele Fritziele", 8,
                List.of(0, 1, 2, 3, 4, 5, 9, 13, 14, 15, 16, 24, 25, 23, 6));
        createAvailability(nextMonthSchemaGinst, "Jamie", "Jamiele Jamiele Jamiele", 10,
                List.of(0, 2, 3, 4, 9, 10, 11, 13, 18));
        createAvailability(nextMonthSchemaGinst, "Daniel", "Danielle Danielle Danielle", 5,
                List.of(8, 21, 22, 23, 24, 10, 28));


        // --- 15 ppl fürs Kraweel ---

        // viel arbeiten
        createAvailability(kraweelSchema, "Arthur Dent", "Bin flexibel.", 12, List.of(0, 14));
        createAvailability(kraweelSchema, "Zaphod Beeblebrox", "Präsidenten haben nie Zeit, außer Dienstags.", 5, List.of(0, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13));
        createAvailability(kraweelSchema, "Trillian Astra", "Lerne gerade für den Pilotenschein.", 10, List.of(5, 6, 12, 13, 19, 20, 26, 27));

        // am we lieber nicht
        createAvailability(kraweelSchema, "Marvin", "Das Leben... hasse es. Wochenenden sind besonders schlimm.", 8, List.of(5, 6, 12, 13, 19, 20, 26, 27));
        createAvailability(kraweelSchema, "Slartibartfast", "Muss Fjorde designen am Wochenende.", 6, List.of(5, 6, 12, 13, 19, 20, 26, 27));

        // große Blöcke
        createAvailability(kraweelSchema, "Tricia McMillan", "Urlaub in der ersten Monatshälfte.", 8, List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        createAvailability(kraweelSchema, "Deep Thought", "Denke 7,5 Millionen Jahre nach. Diesen Monat komplett weg.", 0, List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30));

        createAvailability(kraweelSchema, "Fenny", "Uni-Vorträge Mo-Mi.", 10, List.of(0, 1, 2, 7, 8, 9, 14, 15, 16, 21, 22, 23, 28, 29));
        createAvailability(kraweelSchema, "Lunkwill", "Nur Do-So verfügbar.", 12, List.of(0, 1, 2, 7, 8, 9, 14, 15, 16, 21, 22, 23, 28, 29));
        createAvailability(kraweelSchema, "Fook", "Habe Mittwochs immer Bandprobe.", 8, List.of(2, 9, 16, 23));
        createAvailability(kraweelSchema, "Prosser", "Muss Umgehungsstraßen planen.", 7, List.of(1, 3, 5, 10, 15, 20, 25));
        createAvailability(kraweelSchema, "Benjy Mouse", "Experimente am Montag.", 9, List.of(0, 7, 14, 21, 28));
        createAvailability(kraweelSchema, "Frankie Mouse", "Käseverkostung am Freitag.", 9, List.of(4, 11, 18, 25));
        createAvailability(kraweelSchema, "Vogon Jeltz", "Gedichtlesung (unerträglich).", 4, List.of(10, 11, 12, 13, 14, 20, 21, 22, 23, 24));
        createAvailability(kraweelSchema, "Random Dent", "Weiß noch nicht genau.", 10, List.of(15, 16, 17));

        System.out.println("Created " + availabilityEntryRepository.count() + " AvailabilityEntries total.");
        System.out.println("--- Data Seeding Complete ---");

    }

    private User createUser(String name, Role role) {
        User user = new User();
        user.setUsername(name);
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Shift createShift(String name, String start, String end, User planner) {
        Shift s = new Shift();
        s.setName(name);
        s.setStartTime(LocalTime.parse(start));
        s.setEndTime(LocalTime.parse(end));
        s.setPlanner(planner);
        return s;
    }

    private WeeklyTemplate createWeeklyTemplate(String name, User planner, String description) {
        WeeklyTemplate wt = new WeeklyTemplate();
        wt.setName(name);
        wt.setPlanner(planner);
        wt.setDescription(description);
        return wt;
    }

    private TemplateShift createTemplateShift(int day, String pos, Shift shift, WeeklyTemplate wt) {
        TemplateShift ts = new TemplateShift();
        ts.setWeekday(day);
        ts.setPositionName(pos);
        ts.setShift(shift);
        ts.setTemplate(wt);
        return ts;
    }

    private ScheduleSchema createSchema(String name, YearMonth ym, User planner, int expected, WeeklyTemplate template) {
        ScheduleSchema schema = new ScheduleSchema();
        schema.setName(name);
        schema.setStartDate(ym.atDay(1));
        schema.setEndDate(ym.atEndOfMonth());
        schema.setPlanner(planner);
        schema.setExpectedEntries(expected);
        schema.setAvailabilityLinkID(UUID.randomUUID().toString().substring(0, 12));

        SchemaTemplateAssignment assignment = new SchemaTemplateAssignment();
        assignment.setTemplate(template);
        assignment.setSchema(schema);
        assignment.setValidFrom(schema.getStartDate());
        assignment.setValidTo(schema.getEndDate());
        schema.getTemplateAssignments().add(assignment);

        return schema;
    }

    private void createAvailability(ScheduleSchema schema, String name, String comment, int target, List<Integer> unavailableDays) {
        AvailabilityEntry entry = new AvailabilityEntry();
        entry.setStaffName(name);
        entry.setSchema(schema);
        entry.setComment(comment);
        entry.setTargetShiftCount(target);

        for (Integer dayOffset : unavailableDays) {
            AvailabilityDetail detail = new AvailabilityDetail();
            detail.setDate(schema.getStartDate().plusDays(dayOffset));
            detail.setStatus(AvailabilityStatus.UNAVAILABLE);
            detail.setEntry(entry);
            entry.getDetails().add(detail);
        }
        availabilityEntryRepository.save(entry);
    }

}