<template>
  <header v-if="!isAuthenticated" class="app-header">
    <div class="logo-section">
      <!-- Logo section with background color that matches the sidebar width -->
      <div class="logo-container">
        <!-- Primary Logo -->
        <div class="logo-placeholder primary-logo">
          <img src="@/assets/LogoESTFBS.png" alt="EST FBS Logo" />
        </div>
        <!-- Secondary Logo -->
        <div class="logo-placeholder secondary-logo">
          <img src="@/assets/pfe_man_logo_whole_final.svg" alt="USMS Logo" />
        </div>
      </div>
    </div>
    
    <div class="content-section">
      <div class="app-title">
        <h1>Gestion des Projets de Fin d'Études</h1>
      </div>
      
      <div class="action-menu">
        <div class="">
          <Button 
            @click="navigateToLogin"
            class="p-button-text p-button-rounded" 
            icon="pi pi-sign-in" 
            label="Connexion"
          />
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
// In AppHeader.vue
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '../services/AuthService';
import Button from 'primevue/button';

const router = useRouter();

// Create a reactive reference to track authentication state
const authState = ref(AuthService.isAuthenticated());

// Function to update the auth state
const updateAuthState = () => {
  authState.value = AuthService.isAuthenticated();
};

// Auth state computed property that will be reactive
const isAuthenticated = computed(() => authState.value);

// Add an event listener for custom authentication events
onMounted(() => {
  window.addEventListener('auth-state-changed', updateAuthState);
});

// Navigation function
const navigateToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.app-header {
  display: flex;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 75px;
  z-index: 1000;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.logo-section {
  width: 250px; /* Match sidebar width */
  height: 100%;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--surface-border);
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1rem;
  width: 100%;
}

.logo-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  width: 50px;
}

.logo-placeholder img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  filter: brightness(0) invert(1); 
  /* Makes logos white */
}

.content-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  padding: 0 1.5rem;
  background-color: var(--surface-card);
  border-bottom: 1px solid var(--surface-border);
}

.app-title {
  display: flex;
  flex: 1;
  justify-content: center;
}

.app-title h1 {
  font-size: 1.2rem;
  margin: 0;
  color: var(--text-color);
  font-weight: 600;
  text-align: center;
}

.action-menu {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    height: auto;
  }
  
  .logo-section {
    width: 100%;
    padding: 0.5rem 0;
    order: 1;
  }
  
  .content-section {
    width: 100%;
    order: 2;
    padding: 0.75rem 1rem;
  }
  
  .app-title h1 {
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .logo-placeholder {
    height: 40px;
    width: 40px;
  }
  
  .app-title h1 {
    font-size: 0.85rem;
  }
  
  .logo-container {
    padding: 0 0.5rem;
  }
}
</style>