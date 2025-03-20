<template>
    <Button 
      class="p-button-rounded p-button-text" 
      @click="toggleTheme" 
      :icon="currentTheme === 'dark' ? 'pi pi-sun' : 'pi pi-moon'"
      :aria-label="`Switch to ${currentTheme === 'dark' ? 'light' : 'dark'} mode`"
    />
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { ThemeService } from '../services/ThemeService';
  
  const currentTheme = ref('light');
  
  function toggleTheme() {
    currentTheme.value = ThemeService.toggleTheme();
  }
  
  onMounted(() => {
    // Initialize theme and set current value
    ThemeService.init();
    currentTheme.value = ThemeService.getTheme();
  });
  </script>