<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
</script>

<template>
  <div>
    <h1>Dashboard</h1>
    <h2>Willkommen {{ authStore.user?.username }}!</h2>
    <p>Wähle einen Punkt aus der Menüleiste oben, um zu beginnen.</p>
  </div>
</template>

<style scoped>
/* Du kannst hier spezifische Styles für das Dashboard hinzufügen */
</style>
