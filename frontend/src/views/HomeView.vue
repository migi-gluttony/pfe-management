<template>
    <div class="container">
      <div class="component-card">
        <h1>Welcome to PFE Management System</h1>
        <p>You are logged in successfully!</p>
        
        <div class="user-info mt-4" v-if="user">
          <h2>Your Account</h2>
          <div class="user-detail">
            <strong>Email:</strong> {{ user.email }}
          </div>
          <div class="user-detail">
            <strong>Role:</strong> {{ formatRole(user.role) }}
          </div>
        </div>
        
        <Button 
          @click="logout" 
          label="Logout" 
          icon="pi pi-sign-out" 
          class="p-button-danger mt-4"
        />
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useToast } from 'primevue/usetoast';
  import AuthService from '../services/AuthService';
  
  const router = useRouter();
  const toast = useToast();
  const user = ref(null);
  
  onMounted(() => {
    // Get the current user from AuthService
    user.value = AuthService.getCurrentUser();
  });
  
  const formatRole = (role) => {
    if (!role) return 'User';
    
    // Convert SNAKE_CASE to Title Case
    return role
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };
  
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
  .user-info {
    background-color: rgba(var(--primary-color-rgb), 0.1);
    padding: 1rem;
    border-radius: 0.5rem;
    margin-top: 2rem;
  }
  
  .user-detail {
    margin-bottom: 0.5rem;
  }
  </style>