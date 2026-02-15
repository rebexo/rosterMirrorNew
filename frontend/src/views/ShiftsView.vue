<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useShiftStore, type Shift } from '@/stores/shift';

const shiftStore = useShiftStore();

const isModalOpen = ref(false);
const editingShift = ref<Partial<Shift>>({});
const isNew = ref(false);

onMounted(() => {
  shiftStore.fetchShifts();
});

function openCreateModal() {
  isNew.value = true;
  editingShift.value = {
    name: '',
    startTime: '00:00',
    endTime: '00:00'
  };
  isModalOpen.value = true;
}

function openEditModal(shift: Shift) {
  isNew.value = false;
  editingShift.value = { ...shift };
  isModalOpen.value = true;
}

function closeModal() {
  isModalOpen.value = false;
}

async function saveShift() {
  if (isNew.value) {
    await shiftStore.createShift(editingShift.value as Omit<Shift, 'id'>);
  } else {
    await shiftStore.updateShift(editingShift.value as Shift);
  }
  closeModal();
}

async function handleDelete(shiftId: string) {
  if (confirm('Bist du sicher, dass du diese Schicht löschen möchtest?')) {
    await shiftStore.deleteShift(shiftId);
  }
}
</script>

<template>
  <div class="shifts-container">

    <div class="page-header">
      <h1>Deine Schichten</h1>
      <button @click="openCreateModal" class="btn-create">
        Neue Schicht erstellen
      </button>
    </div>

    <div v-if="shiftStore.isLoading" class="loading-state">Lade Schichten...</div>

    <div v-else-if="shiftStore.shifts.length > 0" class="shift-grid">
      <article
        v-for="shift in shiftStore.shifts"
        :key="shift.id"
        class="shift-card"
        @click="openEditModal(shift)"
      >
        <div class="card-content">
          <h3>{{ shift.name }}</h3>
          <div class="time-badge">
            {{ shift.startTime }} – {{ shift.endTime }}
          </div>
        </div>

        <div class="card-actions">
          <button class="btn-delete" @click.stop="handleDelete(shift.id)" title="Löschen">
            🗑
          </button>
        </div>
      </article>
    </div>

    <p v-else class="empty-state">Du hast noch keine Schichten angelegt.</p>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isNew ? 'Neue Schicht erstellen' : 'Schicht bearbeiten' }}</h2>
          <button @click="closeModal" class="btn-close">×</button>
        </div>

        <form @submit.prevent="saveShift">
          <div class="form-group">
            <label>Name der Schicht</label>
            <input v-model="editingShift.name" placeholder="z.B. Frühschicht" required />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Startzeit</label>
              <input type="time" v-model="editingShift.startTime" step="60" required />
            </div>
            <div class="form-group">
              <label>Endzeit</label>
              <input type="time" v-model="editingShift.endTime" step="60" required />
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" @click="closeModal" class="btn-cancel">Abbrechen</button>
            <button type="submit" class="btn-submit">Speichern</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<style scoped>
.shifts-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Header - angepasst mit mehr Abstand */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem; /* Mehr Abstand nach unten */
  padding-bottom: 1.5rem; /* Mehr "Luft" im Header */
  border-bottom: 1px solid #eee;
  gap: 2rem; /* Sicherer Abstand zwischen Titel und Button */
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
}

.btn-create {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap; /* Verhindert Umbruch im Button */
}

.btn-create:hover {
  background-color: hsla(160, 100%, 30%, 1);
}

/* Grid */
.shift-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

/* Card */
.shift-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  cursor: pointer;
}

.shift-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(0,0,0,0.08);
  border-color: hsla(160, 100%, 37%, 0.5);
}

.card-content h3 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.time-badge {
  display: inline-flex;
  align-items: center;
  background-color: #f8f9fa;
  color: #555;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  border: 1px solid #eee;
}

.time-badge .icon {
  margin-right: 0.5rem;
  font-size: 1rem;
}

.btn-delete {
  background: transparent;
  border: 1px solid #fcede8;
  color: #e74c3c;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1.1rem;
}

.btn-delete:hover {
  background-color: #fee;
  border-color: #e74c3c;
}

/* Modal Styling */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
  animation: slideIn 0.2s ease-out;
}

@keyframes slideIn {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.modal-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: #aaa;
  cursor: pointer;
  line-height: 1;
}
.btn-close:hover { color: #555; }

/* Form Styles */
.form-group {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
}

.form-row {
  display: flex;
  gap: 1.5rem;
}
.form-row .form-group { flex: 1; }

label {
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #555;
  font-size: 0.9rem;
}

input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

input:focus {
  outline: none;
  border-color: hsla(160, 100%, 37%, 1);
}

.modal-actions {
  margin-top: 2rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.btn-cancel {
  background: #f1f3f5;
  color: #495057;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.btn-cancel:hover { background: #e9ecef; }

.btn-submit {
  background: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.btn-submit:hover { background: hsla(160, 100%, 30%, 1); }

.loading-state, .empty-state {
  text-align: center;
  padding: 4rem;
  color: #777;
  font-size: 1.1rem;
}
</style>
