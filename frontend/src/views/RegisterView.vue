<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter, RouterLink } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const error = ref('');

async function handleRegister() {
  error.value = '';
  try {
    await authStore.register({
      username: username.value,
      password: password.value,
      confirmPassword: confirmPassword.value
    });
    // Bei Erfolg direkt zum Dashboard weiterleiten
    router.push({ name: 'dashboard' });
  } catch (err: any) {
    console.error("Fehler bei der Registrierung:", err);
    error.value = err.message || 'Registrierung fehlgeschlagen.';
  }
}
</script>

<template>
  <div class="form-container">
    <h2>Account erstellen</h2>
    <form @submit.prevent="handleRegister">
      <input v-model="username" type="text" placeholder="Benutzername" required />
      <input v-model="password" type="password" placeholder="Passwort" required />
      <input v-model="confirmPassword" type="password" placeholder="Passwort bestätigen" required />
      <button type="submit">Registrieren</button>
    </form>
    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="redirect-link">
      <span>Bereits einen Account?</span>
      <RouterLink to="/login">Jetzt anmelden</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.form-container {
  max-width: 400px;
  margin: 5rem auto;
  padding: 2rem;
  border: 1px solid #e0e0e0; /* Etwas weicherer Rahmen */
  border-radius: 12px;       /* Runde Ecken */
  background-color: #fff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); /* Leichter Schatten für Tiefe */
  text-align: center;
}

h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

/* Formular als Flex-Container (Spalte) */
form {
  display: flex;
  flex-direction: column;
  gap: 1rem; /* Abstand zwischen den Feldern */
}

input {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: hsla(160, 100%, 37%, 1);
}

button {
  margin-top: 0.5rem;
  padding: 0.75rem 2rem;
  border: none;
  border-radius: 6px;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  align-self: center; /* Zentriert den Button im Flex-Container */
  transition: background-color 0.2s;
}

button:hover {
  background-color: hsla(160, 100%, 27%, 1);
}

.error-message {
  color: #e74c3c;
  margin-top: 1rem;
}

.redirect-link {
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #666;
}

.redirect-link a {
  margin-left: 0.5rem;
  font-weight: bold;
  color: hsla(160, 100%, 37%, 1);
  text-decoration: none;
}
</style>
