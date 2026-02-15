<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { formatDateDE } from '@/utils/formatters';

// Typdefinitionen
interface SchemaDto {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  expectedEntries: number | null;
  submittedEntriesCount: number;
}

interface AvailabilityEntryDetailDTO {
  id: string;
  staffName: string;
  targetShiftCount: number | null;
  unavailableDates: string[];
}

// Router & State
const route = useRoute();
const router = useRouter();
const schemaId = route.params.schemaId as string;

const schema = ref<SchemaDto | null>(null);
const availabilityEntries = ref<AvailabilityEntryDetailDTO[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// API Calls
const fetchSchemaDetails = async () => {
  try {
    const response = await axios.get<SchemaDto>(`${baseURL}/schemas/${schemaId}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    schema.value = response.data;
  } catch (err) {
    console.error("Fehler beim Laden der Schema-Details:", err);
    throw new Error("Konnte Schema-Details nicht laden.");
  }
};

const fetchAvailabilityEntries = async () => {
  try {
    const response = await axios.get<AvailabilityEntryDetailDTO[]>(`${baseURL}/schemas/${schemaId}/availability-entries`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    availabilityEntries.value = response.data;
  } catch (err) {
    console.error("Fehler beim Laden der Verfügbarkeiten:", err);
    throw new Error("Konnte Verfügbarkeitseinträge nicht laden.");
  }
};

const setTargetShiftCount = async (entryId: string, count: number | null) => {
  if (count === null || count < 0) {
    alert("Bitte geben Sie eine gültige positive Zahl ein.");
    return;
  }
  try {
    await axios.put(`${baseURL}/availability-entries/${entryId}/target-shift-count`, count, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json'
      }
    });
    // Kleines visuelles Feedback könnte hier eingebaut werden (z.B. Toast), für jetzt reicht Alert
    alert("Zielschichten gespeichert!");
  } catch (err) {
    console.error("Fehler beim Aktualisieren:", err);
    alert("Fehler beim Speichern.");
  }
};

const generateVorschlag = async () => {
  try {
    await axios.post(`${baseURL}/schemas/${schemaId}/generate_proposal`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    alert("Dienstplanvorschlag wurde erfolgreich generiert!");
    // Optional: Direkt zum Vorschlag navigieren, falls gewünscht
  } catch (err) {
    console.error("Fehler beim Generieren:", err);
    alert("Fehler beim Generieren des Vorschlags.");
  }
};

const formatUnavailableDates = (dates: string[]): string => {
  if (!dates || dates.length === 0) return '-';
  return dates.map(dateString => formatDateDE(dateString)).join(', ');
};

onMounted(async () => {
  isLoading.value = true;
  error.value = null;
  try {
    await Promise.all([fetchSchemaDetails(), fetchAvailabilityEntries()]);
  } catch (err) {
    error.value = "Daten konnten nicht vollständig geladen werden.";
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div class="availabilities-container">

    <div class="header-section">
      <button @click="router.push('/schemas')" class="btn-back">
        &larr; Zurück
      </button>

      <div v-if="schema" class="schema-info">
        <h1>{{ schema.name }}</h1>
        <div class="stats-badges">
          <div class="stat-badge">
            <span class="label">Eingereicht</span>
            <span class="value">{{ schema.submittedEntriesCount }}</span>
          </div>
          <div class="stat-badge secondary">
            <span class="label">Erwartet</span>
            <span class="value">{{ schema.expectedEntries ?? '-' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="state-message">Lade Daten...</div>
    <div v-else-if="error" class="state-message error">{{ error }}</div>

    <div v-else class="content-wrapper">

      <div class="table-card">
        <div v-if="availabilityEntries.length === 0" class="empty-state">
          Noch keine Verfügbarkeiten vorhanden.
        </div>

        <table v-else>
          <thead>
          <tr>
            <th style="width: 25%">Mitarbeiter</th>
            <th style="width: 40%">Nicht verfügbar am</th>
            <th style="width: 35%">Zielschichten (Soll)</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="entry in availabilityEntries" :key="entry.id">
            <td class="staff-name">{{ entry.staffName }}</td>
            <td class="dates-cell">{{ formatUnavailableDates(entry.unavailableDates) }}</td>
            <td>
              <div class="action-cell">
                <input
                  type="number"
                  min="0"
                  v-model.number="entry.targetShiftCount"
                  placeholder="0"
                />
                <button @click="setTargetShiftCount(entry.id, entry.targetShiftCount)" class="btn-save" title="Speichern">
                  Speichern
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="footer-actions">
        <button @click="generateVorschlag()" class="btn-generate">
          Dienstplan generieren
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
.availabilities-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

/* Header Styles */
.header-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.btn-back {
  align-self: flex-start;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 1rem;
  padding: 0;
  text-decoration: none;
  transition: color 0.2s;
}

.btn-back:hover {
  color: hsla(160, 100%, 37%, 1);
  text-decoration: underline;
}

.schema-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.schema-info h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.stats-badges {
  display: flex;
  gap: 1rem;
}

.stat-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: hsla(160, 100%, 37%, 0.1);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: 1px solid hsla(160, 100%, 37%, 0.2);
  min-width: 80px;
}

.stat-badge.secondary {
  background-color: #f8f9fa;
  border-color: #e9ecef;
}

.stat-badge .label {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: #666;
  margin-bottom: 0.2rem;
}

.stat-badge .value {
  font-weight: bold;
  font-size: 1.1rem;
  color: #333;
}

/* Table Styles */
.table-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
  border: 1px solid #e0e0e0;
  overflow: hidden; /* Runde Ecken für Child-Elemente */
  margin-bottom: 2rem;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background-color: #f9fafb;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 1px solid #e0e0e0;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #f1f1f1;
  vertical-align: middle;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background-color: #fafafa;
}

.staff-name {
  font-weight: 500;
  color: #2c3e50;
}

.dates-cell {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.4;
}

/* Action Cell (Input + Button) */
.action-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

input[type="number"] {
  width: 70px;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  text-align: center;
  font-size: 1rem;
}

input[type="number"]:focus {
  outline: none;
  border-color: hsla(160, 100%, 37%, 1);
}

.btn-save {
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-save:hover {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border-color: hsla(160, 100%, 37%, 1);
}

/* Footer Actions */
.footer-actions {
  display: flex;
  justify-content: flex-end;
}

.btn-generate {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  font-size: 1rem;
  font-weight: bold;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: background-color 0.2s, transform 0.1s;
}

.btn-generate:hover {
  background-color: hsla(160, 100%, 30%, 1);
  transform: translateY(-2px);
}

.btn-generate:active {
  transform: translateY(0);
}

/* States */
.state-message {
  text-align: center;
  padding: 3rem;
  color: #888;
  font-size: 1.1rem;
}

.state-message.error {
  color: #e74c3c;
}

.empty-state {
  padding: 3rem;
  text-align: center;
  color: #999;
  font-style: italic;
}
</style>

<!--<script setup lang="ts">-->
<!--import { ref, onMounted } from 'vue';-->
<!--import { useRoute, useRouter } from 'vue-router';-->
<!--import axios from 'axios';-->
<!--import { formatDateDE } from '@/utils/formatters'; // Annahme, dass du diesen Formatter hast-->

<!--// Typdefinitionen (könnten in einer separaten types.ts Datei liegen)-->
<!--interface SchemaDto {-->
<!--  id: string;-->
<!--  name: string;-->
<!--  startDate: string;-->
<!--  endDate: string;-->
<!--  expectedEntries: number | null;-->
<!--  submittedEntriesCount: number;-->
<!--}-->

<!--interface AvailabilityEntryDetailDTO {-->
<!--  id: string;-->
<!--  staffName: string;-->
<!--  targetShiftCount: number | null;-->
<!--  unavailableDates: string[]; // Datum als String im ISO-Format-->
<!--}-->

<!--// Router Instanzen-->
<!--const route = useRoute();-->
<!--const router = useRouter();-->

<!--// Props für die Komponente-->
<!--const schemaId = route.params.schemaId as string; // schemaId kommt über die Route-->

<!--// Reactive State-->
<!--const schema = ref<SchemaDto | null>(null);-->
<!--const availabilityEntries = ref<AvailabilityEntryDetailDTO[]>([]);-->
<!--const isLoading = ref(true);-->
<!--const error = ref<string | null>(null);-->

<!--// Basis-URL für deine API-->
<!--const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';-->

<!--// Funktionen ${schemaId}-->
<!--const fetchSchemaDetails = async () => {-->
<!--  try {-->
<!--    const response = await axios.get<SchemaDto>(`${baseURL}/schemas/${schemaId}`, {-->
<!--      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }-->
<!--    });-->
<!--    schema.value = response.data;-->
<!--  } catch (err) {-->
<!--    console.error("Fehler beim Laden der Schema-Details:", err);-->
<!--    error.value = "Konnte Schema-Details nicht laden.";-->
<!--  }-->
<!--};-->

<!--const fetchAvailabilityEntries = async () => {-->
<!--  try {-->
<!--    const response = await axios.get<AvailabilityEntryDetailDTO[]>(`${baseURL}/schemas/${schemaId}/availability-entries`, {-->
<!--      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }-->
<!--    });-->
<!--    // Füge eine lokale Eigenschaft für die Input-Bindung hinzu-->
<!--    availabilityEntries.value = response.data.map(entry => ({-->
<!--      ...entry,-->
<!--      targetShiftCountInput: entry.targetShiftCount // Temporäre Property für v-model-->
<!--    }));-->
<!--  } catch (err) {-->
<!--    console.error("Fehler beim Laden der Verfügbarkeiten:", err);-->
<!--    error.value = "Konnte Verfügbarkeitseinträge nicht laden.";-->
<!--  }-->
<!--};-->

<!--const setTargetShiftCount = async (entryId: string, count: number | null) => {-->
<!--  if (count === null || count < 0) {-->
<!--    alert("Bitte geben Sie eine gültige positive Zahl ein.");-->
<!--    return;-->
<!--  }-->
<!--  try {-->
<!--    await axios.put(`${baseURL}/availability-entries/${entryId}/target-shift-count`, count, {-->
<!--      headers: {-->
<!--        Authorization: `Bearer ${localStorage.getItem('authToken')}`,-->
<!--        'Content-Type': 'application/json' // Wichtig, da nur ein Integer gesendet wird-->
<!--      }-->
<!--    });-->
<!--    alert("Target Shift Count aktualisiert!");-->
<!--    // Optional: Verfügbarkeiten neu laden oder nur den Wert lokal aktualisieren-->
<!--    // await fetchAvailabilityEntries();-->
<!--  } catch (err) {-->
<!--    console.error("Fehler beim Aktualisieren des Target Shift Counts:", err);-->
<!--    error.value = "Fehler beim Aktualisieren des Target Shift Counts.";-->
<!--    alert("Fehler beim Aktualisieren: " + (err as any).response?.data || (err as any).message);-->
<!--  }-->
<!--};-->

<!--const generateVorschlag = async () => {-->
<!--  try {-->
<!--    await axios.post(`${baseURL}/schemas/${schemaId}/generate_proposal`, {}, {-->
<!--      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }-->
<!--    });-->
<!--    alert("Dienstplanvorschlag generiert!");-->
<!--    // Ggf. Weiterleitung zu einer Vorschlagsansicht-->
<!--  } catch (err) {-->
<!--    console.error("Fehler beim Generieren des Vorschlags -console:", err);-->
<!--    error.value = "Fehler beim Generieren des Vorschlags -error.";-->
<!--    alert("Fehler beim Generieren des Vorschlags -alert: " + (err as any).response?.data || (err as any).message);-->
<!--  }-->
<!--};-->

<!--const formatUnavailableDates = (dates: string[]): string => {-->
<!--  if (!dates || dates.length === 0) return '-';-->
<!--  return dates.map(dateString => formatDateDE(dateString)).join(', ');-->
<!--};-->

<!--// Lifecycle Hook-->
<!--// onMounted(async () => {-->
<!--//   isLoading.value = true;-->
<!--//   await Promise.all([-->
<!--//     fetchSchemaDetails(),-->
<!--//     fetchAvailabilityEntries()-->
<!--//   ]);-->
<!--//   isLoading.value = false;-->
<!--// });-->

<!--// Lifecycle Hook-->
<!--onMounted(async () => {-->
<!--  isLoading.value = true;-->
<!--  error.value = null; // Fehler zurücksetzen-->
<!--  try {-->
<!--    await Promise.all([-->
<!--      fetchSchemaDetails(),-->
<!--      fetchAvailabilityEntries()-->
<!--    ]);-->
<!--  } catch (err) {-->
<!--    // Fehler werden bereits in den einzelnen Fetch-Methoden gesetzt-->
<!--  } finally {-->
<!--    isLoading.value = false;-->
<!--  }-->
<!--});-->
<!--</script>-->

<!--<template>-->
<!--  <div class="schema-availabilities">-->
<!--    <button @click="router.push({ name: 'schema-list' })" class="btn-back">-->
<!--      &larr; zurück zur Dienstplanübersicht-->
<!--    </button>-->

<!--    <template v-if="isLoading">-->
<!--      <div class="loading-indicator">Lade Schema-Details...</div>-->
<!--    </template>-->
<!--    <template v-else-if="error">-->
<!--      <div class="error-message">Fehler beim Laden der Schema-Details.</div>-->
<!--    </template>-->
<!--    <template v-else-if="schema">-->
<!--      <h1>Dienstplan: {{ schema.name }}</h1>-->
<!--      <p>Erwartete Einträge: {{ schema.expectedEntries ?? '?' }}</p>-->
<!--      <p>Erfolgte Einträge: {{ schema.submittedEntriesCount }}</p>-->
<!--    </template>-->
<!--    <template v-else>-->
<!--      <h1>Dienstplan: Unbekannt</h1>-->
<!--      <p>Erwartete Einträge: ?</p>-->
<!--      <p>Erfolgte Einträge: ?</p>-->
<!--    </template>-->


<!--    <div v-if="isLoading && !error" class="loading-indicator">Lade Verfügbarkeiten...</div>-->
<!--    <div v-else-if="error" class="error-message">{{ error }}</div>-->

<!--    <div v-else class="availability-table-container">-->
<!--      <div v-if="availabilityEntries.length === 0" class="no-entries-message">-->
<!--        Für diesen Dienstplan wurden noch keine Verfügbarkeiten eingereicht.-->
<!--      </div>-->
<!--      <table v-else>-->
<!--        <thead>-->
<!--        <tr>-->
<!--          <th>Mitarbeitername</th>-->
<!--          <th>Nicht verfügbar</th>-->
<!--          <th>Zielschichten</th>-->
<!--          <th>Ändern</th>-->
<!--        </tr>-->
<!--        </thead>-->
<!--        <tbody>-->
<!--        <tr v-for="entry in availabilityEntries" :key="entry.id">-->
<!--          <td>{{ entry.staffName }}</td>-->
<!--          <td>{{ formatUnavailableDates(entry.unavailableDates) }}</td>-->
<!--          <td>-->
<!--            <input type="number" min="0" v-model.number="entry.targetShiftCount" />-->
<!--          </td>-->
<!--          <td>-->
<!--            <button @click="setTargetShiftCount(entry.id, entry.targetShiftCount)">Ziel setzen</button>-->
<!--          </td>-->
<!--        </tr>-->
<!--        </tbody>-->
<!--      </table>-->
<!--    </div>-->

<!--    <button @click="generateVorschlag()" class="btn-primary generate-button">Vorschlag generieren</button>-->
<!--  </div>-->
<!--</template>-->

<!--<style scoped>-->
<!--.schema-availabilities {-->
<!--  padding: 2rem;-->
<!--  max-width: 1000px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.btn-back {-->
<!--  background: #f0f0f0;-->
<!--  border: 1px solid #ccc;-->
<!--  padding: 0.5rem 1rem;-->
<!--  border-radius: 5px;-->
<!--  cursor: pointer;-->
<!--  margin-bottom: 1.5rem;-->
<!--}-->

<!--.availability-table-container {-->
<!--  margin-top: 1.5rem;-->
<!--  overflow-x: auto; /* Für responsive Tabellen */-->
<!--}-->

<!--table {-->
<!--  width: 100%;-->
<!--  border-collapse: collapse;-->
<!--  background-color: white;-->
<!--  box-shadow: 0 2px 8px rgba(0,0,0,0.1);-->
<!--  border-radius: 8px;-->
<!--  overflow: hidden; /* Für abgerundete Ecken der Tabelle */-->
<!--}-->

<!--th, td {-->
<!--  border: 1px solid #e0e0e0;-->
<!--  padding: 12px 15px;-->
<!--  text-align: left;-->
<!--}-->

<!--th {-->
<!--  background-color: #f8f8f8;-->
<!--  font-weight: bold;-->
<!--  color: #333;-->
<!--  text-transform: uppercase;-->
<!--  font-size: 0.9em;-->
<!--}-->

<!--tr:nth-child(even) {-->
<!--  background-color: #f9f9f9;-->
<!--}-->

<!--input[type="number"] {-->
<!--  width: 80px;-->
<!--  padding: 8px;-->
<!--  border: 1px solid #ccc;-->
<!--  border-radius: 4px;-->
<!--}-->

<!--td button {-->
<!--  background-color: hsla(160, 100%, 37%, 1);-->
<!--  color: white;-->
<!--  border: none;-->
<!--  padding: 8px 12px;-->
<!--  border-radius: 4px;-->
<!--  cursor: pointer;-->
<!--  transition: background-color 0.2s;-->
<!--}-->

<!--td button:hover {-->
<!--  background-color: hsla(160, 100%, 30%, 1);-->
<!--}-->

<!--.generate-button {-->
<!--  margin-top: 2rem;-->
<!--  background-color: #007bff; /* Beispiel: Blaue Farbe für den Button */-->
<!--}-->

<!--.loading-indicator, .error-message {-->
<!--  text-align: center;-->
<!--  margin-top: 2rem;-->
<!--  font-size: 1.1em;-->
<!--  color: #555;-->
<!--}-->
<!--.error-message {-->
<!--  color: #dc3545; /* Rot für Fehlermeldungen */-->
<!--}-->
<!--</style>-->
