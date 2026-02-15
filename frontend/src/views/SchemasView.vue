<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useSchemaStore } from '@/stores/schema';
import { RouterLink, useRouter } from 'vue-router';
import type { Ref } from 'vue';
import { formatDateDE } from '@/utils/formatters';
import axios from 'axios';

const schemaStore = useSchemaStore();
const router = useRouter();

// Lokaler State
const localExpectedEntries: Ref<Record<string, number | null>> = ref({});
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

onMounted(async () => {
  await schemaStore.fetchSchemas();
  schemaStore.schemas.forEach(schema => {
    if (schema.id) {
      localExpectedEntries.value[schema.id] = schema.expectedEntries ?? null;
    }
  });
});

function handleUpdate(schemaId: string) {
  const count = localExpectedEntries.value[schemaId];
  if (count !== null && count >= 0) {
    schemaStore.updateExpectedEntries(schemaId, count);
  }
}

function getFullLink(linkId: string): string {
  return `${window.location.origin}/submit/${linkId}`;
}

// Hilfsfunktion für das Markieren des Links (löst das TypeScript-Problem)
function selectText(event: Event) {
  const target = event.target as HTMLInputElement;
  target.select();
}

function goToAvailabilities(schemaId: string) {
  router.push({ name: 'schema-availabilities', params: { schemaId: schemaId } });
}

async function generateVorschlag(schemaId: string) {
  try {
    const response = await axios.post(`${baseURL}/schemas/${schemaId}/generate_proposal`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    const newProposalId = response.data.id;
    alert("Dienstplanvorschlag generiert!");
    router.push({ name: 'proposal-details', params: { proposalId: newProposalId } });
  } catch (err) {
    console.error("Fehler beim Generieren des Vorschlags:", err);
    alert("Fehler: " + ((err as any).response?.data || (err as any).message));
  }
}

async function viewLatestProposalForSchema(schemaId: string) {
  try {
    const response = await axios.get<string>(`${baseURL}/proposals/latest/bySchema/${schemaId}/id`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    const latestProposalId = response.data;
    if (latestProposalId) {
      router.push({ name: 'proposal-details', params: { proposalId: latestProposalId } });
    } else {
      alert("Für dieses Dienstplanschema wurde noch kein Vorschlag generiert.");
    }
  } catch (err) {
    console.error("Fehler beim Abrufen:", err);
    alert("Fehler: " + ((err as any).response?.data?.message || (err as any).message));
  }
}
</script>

<template>
  <div class="schemas-container">

    <div class="page-header">
      <h1>Deine Dienstpläne</h1>
      <RouterLink to="/schemas/new" class="btn-create">
        Neuen Dienstplan erstellen
      </RouterLink>
    </div>

    <div v-if="schemaStore.isLoading" class="loading-indicator">Lade Dienstpläne...</div>

    <div v-else-if="schemaStore.schemas.length > 0" class="schema-grid">
      <article v-for="schema in schemaStore.schemas" :key="schema.id" class="schema-card">

        <div class="card-header">
          <RouterLink :to="`/schemas/${schema.id}`" class="schema-title-link">
            <h2>{{ schema.name }}</h2>
          </RouterLink>
          <div class="date-badge">
            {{ formatDateDE(schema.startDate) }} bis {{ formatDateDE(schema.endDate) }}
          </div>
        </div>

        <div class="card-body">
          <div class="stats-row">
            <div class="stat-group">
              <span class="label">Antworten</span>
              <span class="value">{{ schema.submittedEntriesCount }}</span>
            </div>
            <div class="stat-divider">/</div>
            <div class="stat-group">
              <span class="label">Erwartet</span>
              <div class="input-wrapper">
                <input
                  type="number"
                  min="0"
                  v-model="localExpectedEntries[schema.id!]"
                  placeholder="-"
                  @blur="handleUpdate(schema.id!)"
                  @keyup.enter="handleUpdate(schema.id!)"
                />
                <button @click="handleUpdate(schema.id!)" class="btn-save-mini" title="Speichern">Speichern</button>
              </div>
            </div>
          </div>

          <div v-if="schema.availabilityLinkID" class="link-section">
            <label>Link für Mitarbeiter:</label>
            <input
              type="text"
              :value="getFullLink(schema.availabilityLinkID)"
              readonly
              @click="selectText"
            />
          </div>
        </div>

        <div class="card-footer">
          <button @click="goToAvailabilities(schema.id!)" class="btn-action secondary">Verfügbarkeiten</button>
          <div class="action-divider"></div>
          <div class="primary-actions">
            <button @click="generateVorschlag(schema.id!)" class="btn-action primary">Generieren</button>
            <button @click="viewLatestProposalForSchema(schema.id!)" class="btn-action outline" title="Letzten Plan anschauen">Plan ➔</button>
          </div>
        </div>

      </article>
    </div>

    <p v-else class="no-data-message">Du hast noch keine Dienstpläne erstellt.</p>
  </div>
</template>

<style scoped>
.schemas-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header Anpassungen */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem; /* Mehr Abstand nach unten zum Inhalt */
  padding-bottom: 1.5rem; /* Mehr "Luft" im Header selbst */
  border-bottom: 1px solid #eee;
  gap: 2rem; /* Sicherstellen, dass Button und Text nicht zusammenstoßen */
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
}

.btn-create {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  padding: 0.75rem 1.5rem; /* Etwas breiteres Padding für besseren Look ohne Icon */
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: background-color 0.2s;
  border: none;
  cursor: pointer;
  /* Flexbox für Icon entfernt, da Icon weg ist */
}

.btn-create:hover {
  background-color: hsla(160, 100%, 30%, 1);
}

/* Grid Layout */
.schema-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
}

/* Card Design */
.schema-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
  transition: transform 0.2s, box-shadow 0.2s;
}

.schema-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(0,0,0,0.08);
  border-color: #d0d0d0;
}

/* Card Header */
.card-header {
  padding: 1.5rem 1.5rem 1rem;
}

.schema-title-link {
  text-decoration: none;
  color: #2c3e50;
}

.schema-title-link h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.4rem;
  line-height: 1.3;
}

.schema-title-link:hover h2 {
  color: hsla(160, 100%, 37%, 1);
}

.date-badge {
  display: inline-block;
  background-color: #f8f9fa;
  color: #666;
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  border: 1px solid #eee;
}

/* Card Body */
.card-body {
  padding: 0 1.5rem 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Stats Row */
.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f9f9f9;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: 1px solid #eee;
}

.stat-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stat-group .label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #888;
  margin-bottom: 0.25rem;
}

.stat-group .value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
}

.stat-divider {
  font-size: 1.5rem;
  color: #ddd;
  font-weight: 300;
}

/* Input Wrapper & Save Button */
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.input-wrapper input {
  width: 60px;
  padding: 0.25rem;
  border: 1px solid #ccc;
  background: white;
  border-radius: 4px;
  text-align: center;
  font-weight: 600;
  font-size: 1rem;
  color: #333;
}

.input-wrapper input:focus {
  border-color: hsla(160, 100%, 37%, 1);
  outline: none;
}

.btn-save-mini {
  background: #e9ecef;
  border: 1px solid #ced4da;
  border-radius: 4px;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  font-size: 0.9rem;
}

.btn-save-mini:hover {
  background: #dbe0e5;
}

/* Link Section */
.link-section label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #555;
  margin-bottom: 0.4rem;
}

.link-section input {
  width: 100%;
  padding: 0.6rem;
  font-size: 0.9rem;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 6px;
  color: #666;
  cursor: text;
}

.link-section input:focus {
  border-color: #bbb;
  outline: none;
}

/* Card Footer */
.card-footer {
  padding: 1rem 1.5rem;
  background-color: #fafafa;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.action-divider {
  width: 1px;
  height: 24px;
  background-color: #e0e0e0;
  margin: 0 0.5rem;
}

.primary-actions {
  display: flex;
  gap: 0.5rem;
}

/* Actions Buttons */
.btn-action {
  cursor: pointer;
  border: none;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-action.secondary {
  background: transparent;
  color: #555;
  padding-left: 0;
  padding-right: 0;
}

.btn-action.secondary:hover {
  color: #000;
  text-decoration: underline;
}

.btn-action.primary {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.btn-action.primary:hover {
  background-color: hsla(160, 100%, 30%, 1);
  transform: translateY(-1px);
}

.btn-action.outline {
  background: white;
  border: 1px solid #ddd;
  color: #555;
}

.btn-action.outline:hover {
  border-color: #bbb;
  background-color: #f9f9f9;
}

.loading-indicator, .no-data-message {
  text-align: center;
  padding: 4rem;
  color: #777;
  font-size: 1.1rem;
}
</style>
