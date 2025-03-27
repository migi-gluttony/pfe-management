<template>
  <div class="dashboard-container">
    <!-- Header with user info and logout
    <header class="dashboard-header">
      <div class="container">
        <div class="header-content">
          <div class="logo">
            <h1>PFE Management</h1>
          </div>
          <div class="user-menu">
            <span class="user-email">{{ user?.email }}</span>
            <Button @click="logout" class="p-button-text p-button-rounded" icon="pi pi-power-off" />
          </div>
        </div>
      </div>
    </header> -->

    <div class="dashboard-content">
      <div class="container">
        <div class="welcome-section">
          <h2>Bienvenue</h2>
          <div class="user-role-badge">
            {{ formatRole(user?.role) }}
          </div>
        </div>
        
        <!-- Role-specific dashboards - dynamically load based on role -->
        <component :is="currentDashboard" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import AuthService from '../services/AuthService';

// Import dashboard components
import StudentDashboard from '../components/dashboards/StudentDashboard.vue';
import SupervisorDashboard from '../components/dashboards/SupervisorDashboard.vue';
import HODDashboard from '../components/dashboards/HODDashboard.vue';
import JuryDashboard from '../components/dashboards/JuryDashboard.vue';

const router = useRouter();
const toast = useToast();
const user = ref(null);

// Determine which dashboard component to display based on user role
const currentDashboard = computed(() => {
  if (!user.value) return null;
  
  switch (user.value.role) {
    case 'ETUDIANT':
      return StudentDashboard;
    case 'ENCADRANT':
      return SupervisorDashboard;
    case 'CHEF_DE_DEPARTEMENT':
      return HODDashboard;
    case 'JURY':
      return JuryDashboard;
    default:
      // Return a default/fallback dashboard
      return null;
  }
});

onMounted(() => {
  // Get the current user from AuthService
  user.value = AuthService.getCurrentUser();
  
  // If no user is found, redirect to login
  if (!user.value) {
    router.push('/login');
  }
  
  console.log("Current user:", user.value);
  console.log("Selected dashboard component:", currentDashboard.value);
});

// Format role from SNAKE_CASE to Title Case
const formatRole = (role) => {
  if (!role) return 'User';
  
  return role
    .toLowerCase()
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

// Logout function
const logout = () => {
  AuthService.logout();
  
  toast.add({
    severity: 'info',
    summary: 'Logged Out',
    detail: 'You have been logged out successfully',
    life: 3000
  });
  
  router.push('/login');
};
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: var(--surface-ground);
}

.dashboard-header {
  background-color: var(--primary-color);
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  margin: 0;
  font-size: 1.5rem;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-email {
  font-size: 0.9rem;
}

.dashboard-content {
  padding: 2rem 0;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.welcome-section h2 {
  font-size: 2.5rem;
  margin: 0;
}

.user-role-badge {
  background-color: var(--primary-color);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  font-size: 0.9rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>