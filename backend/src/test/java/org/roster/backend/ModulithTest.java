package org.roster.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTest {

    // Ersetze 'BackendApplication.class' mit deiner Hauptklasse (die mit @SpringBootApplication)
    ApplicationModules modules = ApplicationModules.of(BackendApplication.class);

    @Test
    void verifyModularity() {
        // 1. Gibt die gefundene Modul-Struktur in der Konsole aus
        modules.forEach(System.out::println);

        // 2. Prüft die Regeln:
        // - Keine zyklischen Abhängigkeiten zwischen Modulen?
        // - Kein Zugriff auf interne (nicht-exportierte) Klassen anderer Module?
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        // 3. Generiert C4-Diagramme und UML als puml-Dateien
        // Zu finden später unter /target/spring-modulith-docs
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }
}
