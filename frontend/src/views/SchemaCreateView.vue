<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useSchemaStore, type Schema } from '@/stores/schema';
import { useTemplateStore } from '@/stores/template';
import { useRouter } from 'vue-router';

const schemaStore = useSchemaStore();
const templateStore = useTemplateStore();
const router = useRouter();

// Lokaler State für das Hauptformular
const newSchema = ref<Schema>({
  name: '',
  startDate: '',
  endDate: '',
  templateAssignments: []
});

// Lokaler State für das Unter-Formular (Zuweisung)
const currentAssignment = ref({
  templateId: '',
  validFrom: '',
  validTo: ''
});

onMounted(() => {
  templateStore.fetchTemplates();
});

// Zuweisungs-Datum automatisch vorausfüllen, wenn sich Plan-Datum ändert
watch(() => [newSchema.value.startDate, newSchema.value.endDate], ([newStart, newEnd]) => {
  if (newStart) currentAssignment.value.validFrom = newStart;
  if (newEnd) currentAssignment.value.validTo = newEnd;
});

function addAssignment() {
  if (!currentAssignment.value.templateId || !currentAssignment.value.validFrom || !currentAssignment.value.validTo) {
    alert('Bitte alle Felder für die Zuweisung ausfüllen.');
    return;
  }
  newSchema.value.templateAssignments.push({ ...currentAssignment.value });
  // Template zurücksetzen, Datum beibehalten (Komfort)
  currentAssignment.value.templateId = '';
}

function removeAssignment(index: number) {
  newSchema.value.templateAssignments.splice(index, 1);
}

async function handleSubmit() {
  if (newSchema.value.templateAssignments.length === 0) {
    alert('Bitte mindestens ein Template zuweisen.');
    return;
  }
  await schemaStore.createSchema(newSchema.value);
  router.push('/schemas');
}

function getTemplateName(templateId: string): string {
  const tpl = templateStore.templates.find(t => t.id === templateId);
  return tpl ? tpl.name : 'Unbekanntes Template';
}
</script>

<template>
  <div class="create-view">
    <div class="header-row">
      <button @click="router.push('/schemas')" class="btn-back">
        &larr; Zurück zur Übersicht
      </button>
      <h1>Neuen Dienstplan erstellen</h1>
    </div>

    <div class="form-card">
      <form @submit.prevent="handleSubmit">

        <section class="main-info">
          <div class="form-group full-width">
            <label>Name des Dienstplans</label>
            <input
              v-model="newSchema.name"
              placeholder="z.B. Dienstplan Mai"
              required
              class="main-input"
            />
          </div>

          <div class="date-row">
            <div class="form-group">
              <label>Startdatum</label>
              <input type="date" v-model="newSchema.startDate" required />
            </div>
            <div class="form-group">
              <label>Enddatum</label>
              <input type="date" v-model="newSchema.endDate" required />
            </div>
          </div>
        </section>

        <section class="assignment-section">
          <h3>Templates & Zeiträume</h3>
          <p class="hint-text">Weise Templates bestimmten Zeiträumen innerhalb des Dienstplans zu.</p>

          <div class="assignment-box">
            <div class="assignment-inputs">
              <div class="form-group template-field">
                <label>Template</label>
                <select v-model="currentAssignment.templateId">
                  <option disabled value="">-- Bitte wählen --</option>
                  <option v-for="template in templateStore.templates" :key="template.id" :value="template.id">
                    {{ template.name }}
                  </option>
                </select>
              </div>
              <div class="form-group date-field">
                <label>Von</label>
                <input type="date" v-model="currentAssignment.validFrom" />
              </div>
              <div class="form-group date-field">
                <label>Bis</label>
                <input type="date" v-model="currentAssignment.validTo" />
              </div>
              <div class="form-group button-field">
                <button type="button" @click="addAssignment" class="btn-add">Hinzufügen</button>
              </div>
            </div>
          </div>

          <div v-if="newSchema.templateAssignments.length > 0" class="assignments-list">
            <h4>Zugewiesene Zeiträume:</h4>
            <ul>
              <li v-for="(assignment, index) in newSchema.templateAssignments" :key="index" class="assignment-item">
                <div class="assignment-info">
                  <span class="tpl-name">{{ getTemplateName(assignment.templateId) }}</span>
                  <span class="tpl-dates">{{ assignment.validFrom }} bis {{ assignment.validTo }}</span>
                </div>
                <button type="button" @click="removeAssignment(index)" class="btn-remove" title="Entfernen">
                  🗑
                </button>
              </li>
            </ul>
          </div>
          <p v-else class="empty-list-msg">Noch keine Templates zugewiesen.</p>
        </section>

        <div class="form-footer">
          <button type="submit" class="submit-btn">Dienstplan erstellen</button>
        </div>

      </form>
    </div>
  </div>
</template>

<style scoped>
.create-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.header-row {
  margin-bottom: 2rem;
}

.btn-back {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 0;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}
.btn-back:hover { text-decoration: underline; color: hsla(160, 100%, 37%, 1); }

h1 { margin: 0; color: #2c3e50; }

.form-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid #e0e0e0;
  padding: 2rem;
}

/* Form Groups */
.form-group {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.4rem;
  color: #555;
}

input, select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  background-color: #fff;
  width: 100%; /* Wichtig für Flexbox */
  box-sizing: border-box;
}

input:focus, select:focus {
  outline: none;
  border-color: hsla(160, 100%, 37%, 1);
}

.main-input { font-size: 1.1rem; }

.date-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

/* Assignment Section */
.assignment-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.assignment-section h3 { margin-top: 0; margin-bottom: 0.25rem; }
.hint-text { color: #888; font-size: 0.9rem; margin-bottom: 1.5rem; }

.assignment-box {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

/* --- NEUES FLEXBOX LAYOUT --- */
.assignment-inputs {
  display: flex;
  flex-wrap: wrap; /* Erlaubt Umbruch wenn Platz fehlt */
  gap: 1rem;
  align-items: flex-end; /* Button unten bündig an Inputs ausrichten */
}

.template-field {
  flex: 4 1 250px; /* Nimmt den meisten Platz, mind. 250px */
  margin-bottom: 0; /* Margin wird durch gap ersetzt */
}

.date-field {
  flex: 2 1 140px; /* Mindestens 140px breit */
  margin-bottom: 0;
}

.button-field {
  flex: 0 0 auto; /* Nur so breit wie der Button braucht */
  margin-bottom: 0;
}

.btn-add {
  padding: 0.75rem 1.5rem;
  background-color: #2c3e50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
  height: 44px; /* Fixe Höhe, damit er exakt so hoch wie Inputs ist */
}
.btn-add:hover { background-color: #1a252f; }

/* List Styles */
.assignments-list h4 { margin-bottom: 0.5rem; color: #555; }

.assignments-list ul {
  list-style: none;
  padding: 0;
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.assignment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: white;
  border-bottom: 1px solid #eee;
}
.assignment-item:last-child { border-bottom: none; }

.assignment-info {
  display: flex;
  flex-direction: column;
}

.tpl-name { font-weight: bold; color: #2c3e50; }
.tpl-dates { font-size: 0.85rem; color: #666; }

.btn-remove {
  background: transparent;
  border: none;
  color: #e74c3c;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}
.btn-remove:hover { background-color: #fee; }

.empty-list-msg {
  text-align: center;
  color: #999;
  font-style: italic;
  margin-top: 1rem;
}

/* Footer */
.form-footer {
  margin-top: 2rem;
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  padding: 0.75rem 2rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s;
}
.submit-btn:hover { background-color: hsla(160, 100%, 30%, 1); }

/* Mobile Optimierung */
@media (max-width: 650px) {
  .date-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }

  /* Auf Mobile Button über volle Breite */
  .button-field {
    flex: 1 1 100%;
  }
  .btn-add {
    width: 100%;
    margin-top: 0.5rem;
  }
}
</style>
