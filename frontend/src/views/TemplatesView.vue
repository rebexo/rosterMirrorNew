<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useTemplateStore, type Template } from '@/stores/template';
import { useShiftStore } from '@/stores/shift';

const templateStore = useTemplateStore();
const shiftStore = useShiftStore();

const isModalOpen = ref(false);
const editingTemplate = ref<Partial<Template>>({});
const isNew = ref(false);

const newShiftAssignment = ref({ weekday: 0, positionName: '', shiftId: '' });

onMounted(() => {
  templateStore.fetchTemplates();
  shiftStore.fetchShifts(); // Lade Schichten für das Dropdown im Modal
});

function openCreateModal() {
  isNew.value = true;
  editingTemplate.value = { name: '', description: '', shifts: [] };
  isModalOpen.value = true;
}

function openEditModal(template: Template) {
  isNew.value = false;
  // Tiefe Kopie, um reaktive Probleme mit verschachtelten Objekten zu vermeiden
  editingTemplate.value = JSON.parse(JSON.stringify(template));
  isModalOpen.value = true;
}

function closeModal() { isModalOpen.value = false; }

async function saveTemplate() {
  if (isNew.value) {
    await templateStore.createTemplate(editingTemplate.value as Template);
  } else {
    await templateStore.updateTemplate(editingTemplate.value as Template);
  }
  closeModal();
}

async function handleDelete(templateId?: string) {
  if (!templateId) return;
  if (confirm('Soll dieses Template wirklich gelöscht werden?')) {
    await templateStore.deleteTemplate(templateId);
  }
}

function addShiftToTemplate() {
  if (!newShiftAssignment.value.shiftId) return;
  editingTemplate.value.shifts?.push({ ...newShiftAssignment.value });
}

function removeShiftFromTemplate(index: number) {
  editingTemplate.value.shifts?.splice(index, 1);
}
</script>

<template>
  <div>
    <h1>Deine Templates</h1>
    <button @click="openCreateModal">Neues Template erstellen</button>

    <div v-if="templateStore.isLoading">Lade...</div>
    <ul v-else="template-list">
      <li v-for="template in templateStore.templates" :key="template.id" @click="openEditModal(template)" class="template-card">
        <span><strong>{{ template.name }}</strong><p>{{ template.description }}</p></span>
        <button @click.stop="handleDelete(template.id)" class="delete-btn">Löschen</button>
      </li>
    </ul>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isNew ? 'Neues Template' : 'Template bearbeiten' }}</h2>
        <form @submit.prevent="saveTemplate">
          <input v-model="editingTemplate.name" placeholder="Template-Name" required />
          <textarea v-model="editingTemplate.description" placeholder="Beschreibung"></textarea>

          <div class="shift-adder">
            <h4>Schichten hinzufügen</h4>
            <select v-model="newShiftAssignment.weekday">
              <option :value="0">Mo</option><option :value="1">Di</option><option :value="2">Mi</option>
              <option :value="3">Do</option><option :value="4">Fr</option><option :value="5">Sa</option><option :value="6">So</option>
            </select>
            <input v-model="newShiftAssignment.positionName" placeholder="Position" />
            <select v-model="newShiftAssignment.shiftId">
              <option disabled value="">Schicht auswählen</option>
              <option v-for="shift in shiftStore.shifts" :key="shift.id" :value="shift.id">{{ shift.name }}</option>
            </select>
            <button type="button" @click="addShiftToTemplate">+</button>
          </div>

          <ul class="assigned-shifts">
            <li v-for="(shift, index) in editingTemplate.shifts" :key="index">
              <span>Tag: {{ shift.weekday }}, Pos: {{ shift.positionName }}</span>
              <button type="button" @click="removeShiftFromTemplate(index)">X</button>
            </li>
          </ul>

          <div class="modal-actions">
            <button type="button" @click="closeModal">Abbrechen</button>
            <button type="submit">Speichern</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Allgemeine Container Styles */
h1 {
  margin-bottom: 1.5rem;
}

button {
  cursor: pointer;
  border: none;
  border-radius: 4px;
  padding: 0.5rem 1rem;
  font-weight: 500;
  transition: opacity 0.2s;
}

/* Haupt-Button zum Erstellen */
button:not(.delete-btn, .modal-actions button, .shift-adder button) {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  font-size: 1rem;
  margin-bottom: 2rem;
}

/* Listen-Layout (Cards) */
.template-list {
  list-style: none;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); /* Responsive Grid */
  gap: 1.5rem;
}

.template-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.template-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.template-card strong {
  font-size: 1.2rem;
  display: block;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.template-card p {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.delete-btn {
  align-self: flex-end;
  background-color: transparent;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  font-size: 0.8rem;
}

.delete-btn:hover {
  background-color: #e74c3c;
  color: white;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 600px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.modal-content h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 1rem;
}

.modal-content form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.modal-content input,
.modal-content textarea,
.modal-content select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-family: inherit;
  width: 100%;
  box-sizing: border-box; /* Wichtig damit Padding nicht die Breite sprengt */
}

.modal-content textarea {
  min-height: 80px;
  resize: vertical;
}

/* Shift Adder Section im Modal */
.shift-adder {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  border: 1px dashed #ced4da;
}

.shift-adder h4 {
  margin-top: 0;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
  color: #555;
}

.shift-adder select,
.shift-adder input {
  margin-bottom: 0.5rem;
}

.assigned-shifts {
  list-style: none;
  padding: 0;
  margin-top: 1rem;
  border-top: 1px solid #eee;
}

.assigned-shifts li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f1f1f1;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
  border-top: 1px solid #eee;
  padding-top: 1rem;
}

.modal-actions button[type="submit"] {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
}

.modal-actions button[type="button"] {
  background-color: #f1f1f1;
  color: #333;
}
</style>
