<script setup>
import { RouterLink, RouterView } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
</script>


<template>
  <main>
    <RouterView />
    <ThemeToggle />
  </main>
</template>

<style>
/* Add these global styles to support dark mode */
:root {
  --text-color: #213547;
  --background-color: #ffffff;
}

.dark-mode {
  --text-color: #ffffffde;
  --background-color: #1e1e1e;
}

body {
  color: var(--text-color);
  background-color: var(--background-color);
  transition: color 0.3s, background-color 0.3s;
}
</style>