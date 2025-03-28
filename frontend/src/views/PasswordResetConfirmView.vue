<template>
    <div class="reset-confirm-container">
      <div class="reset-confirm-card">
        <div class="reset-confirm-header">
          <img src="@/assets/pfe_man_logo_whole_final.svg" alt="PFE Management Logo" class="logo" />
          <h1>Créer un nouveau mot de passe</h1>
          <p class="subheader">Veuillez choisir un nouveau mot de passe sécurisé pour votre compte</p>
        </div>
        
        <div class="form-container">
          <form @submit.prevent="handleResetConfirm">
            <!-- New password input -->
            <div class="form-field">
              <label for="newPassword">Nouveau mot de passe</label>
              <Password 
                id="newPassword" 
                v-model="newPassword" 
                :class="{ 'p-invalid': validationErrors.newPassword }"
                class="w-full" 
                placeholder="Entrez votre nouveau mot de passe"
                :feedback="true"
                aria-describedby="newPassword-error"
                toggleMask
              />
              <small id="newPassword-error" class="p-error">{{ validationErrors.newPassword }}</small>
              <small class="form-help-text">
                Votre mot de passe doit contenir au moins 8 caractères, incluant des chiffres et des caractères spéciaux.
              </small>
            </div>
            
            <!-- Confirm password input -->
            <div class="form-field">
              <label for="confirmPassword">Confirmer le mot de passe</label>
              <Password 
                id="confirmPassword" 
                v-model="confirmPassword" 
                :class="{ 'p-invalid': validationErrors.confirmPassword }"
                class="w-full" 
                placeholder="Confirmez votre nouveau mot de passe"
                aria-describedby="confirmPassword-error"
                :feedback="false"
                toggleMask
              />
              <small id="confirmPassword-error" class="p-error">{{ validationErrors.confirmPassword }}</small>
            </div>
            
            <!-- Submit button -->
            <Button 
              type="submit" 
              label="Réinitialiser le mot de passe" 
              icon="pi pi-check" 
              class="w-full" 
              :loading="loading"
            />
            
            <!-- Back to login link -->
            <div class="mt-3 text-center">
              <router-link to="/login" class="back-link">
                <i class="pi pi-arrow-left"></i> Retour à la page de connexion
              </router-link>
            </div>
          </form>
          
          <!-- Error message -->
          <div v-if="errorMessage" class="error-message mt-3">
            {{ errorMessage }}
          </div>
          
          <!-- Success message -->
          <div v-if="successMessage" class="success-message mt-3">
            {{ successMessage }}
          </div>
        </div>
      </div>
      
      <Toast position="top-center" />
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useToast } from 'primevue/usetoast';
  import AuthService from '@/services/AuthService';
  
  // Component state
  const router = useRouter();
  const route = useRoute();
  const toast = useToast();
  
  const token = computed(() => route.query.token);
  const newPassword = ref('');
  const confirmPassword = ref('');
  const loading = ref(false);
  const errorMessage = ref('');
  const successMessage = ref('');
  const validationErrors = ref({
    newPassword: '',
    confirmPassword: ''
  });
  
  // On component mount
  onMounted(() => {
    // If already logged in, redirect to dashboard
    if (AuthService.isAuthenticated()) {
      router.push('/dashboard');
    }
    
    // Check if token is present
    if (!token.value) {
      errorMessage.value = 'Aucun jeton de réinitialisation n\'a été fourni. Veuillez recommencer le processus de réinitialisation.';
      setTimeout(() => {
        router.push('/reset-password-request');
      }, 3000);
    }
  });
  
  // Form validation
  const validateForm = () => {
    let isValid = true;
    validationErrors.value = {
      newPassword: '',
      confirmPassword: ''
    };
  
    // Password validation
    if (!newPassword.value) {
      validationErrors.value.newPassword = 'Le nouveau mot de passe est requis';
      isValid = false;
    } else if (newPassword.value.length < 8) {
      validationErrors.value.newPassword = 'Le mot de passe doit contenir au moins 8 caractères';
      isValid = false;
    }
  
    // Password confirmation validation
    if (!confirmPassword.value) {
      validationErrors.value.confirmPassword = 'La confirmation du mot de passe est requise';
      isValid = false;
    } else if (newPassword.value !== confirmPassword.value) {
      validationErrors.value.confirmPassword = 'Les mots de passe ne correspondent pas';
      isValid = false;
    }
  
    return isValid;
  };
  
  // Handle reset confirmation
  const handleResetConfirm = async () => {
    if (!validateForm()) {
      return;
    }
  
    try {
      loading.value = true;
      errorMessage.value = '';
      successMessage.value = '';
  
      // Call API to confirm password reset
      await AuthService.confirmPasswordReset(token.value, newPassword.value);
      
      // Show success message
      successMessage.value = 'Votre mot de passe a été réinitialisé avec succès. Vous allez être redirigé vers la page de connexion.';
      
      toast.add({
        severity: 'success',
        summary: 'Mot de passe réinitialisé',
        detail: 'Votre mot de passe a été réinitialisé avec succès',
        life: 3000
      });
      
      // Redirect to login page
      setTimeout(() => {
        router.push('/login');
      }, 3000);
    } catch (error) {
      console.error('Password reset confirmation error:', error);
      
      // Handle error messages
      if (error.message) {
        errorMessage.value = error.message;
      } else {
        errorMessage.value = 'Une erreur s\'est produite lors de la réinitialisation de votre mot de passe. Veuillez réessayer.';
      }
      
      toast.add({
        severity: 'error',
        summary: 'Échec de la réinitialisation',
        detail: errorMessage.value,
        life: 5000
      });
    } finally {
      loading.value = false;
    }
  };
  </script>
  
  <style scoped>
  .reset-confirm-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 2rem;
    background-color: var(--surface-ground);
  }
  
  .reset-confirm-card {
    width: 100%;
    max-width: 480px;
    background-color: var(--surface-card);
    border-radius: 12px;
    box-shadow: var(--shadow-lg);
    overflow: hidden;
    transition: all var(--transition-speed);
  }
  
  .reset-confirm-header {
    padding: 2rem 2rem 1rem;
    text-align: center;
  }
  
  .reset-confirm-header .logo {
    height: 70px;
    margin-bottom: 1rem;
  }
  
  .reset-confirm-header h1 {
    font-size: 1.75rem;
    margin: 0;
    color: var(--text-color);
  }
  
  .subheader {
    color: var(--text-color-secondary);
    margin-top: 0.5rem;
    font-size: 0.95rem;
  }
  
  .form-container {
    padding: 0 2rem 2rem;
  }
  
  .back-link {
    font-size: 0.875rem;
    color: var(--primary-color);
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;
  }
  
  .back-link:hover {
    text-decoration: underline;
  }
  
  :deep(.p-password-input) {
    width: 100%;
  }
  
  @media (max-width: 576px) {
    .reset-confirm-container {
      padding: 1rem;
    }
    
    .reset-confirm-card {
      box-shadow: var(--shadow);
    }
    
    .reset-confirm-header {
      padding: 1.5rem 1.5rem 0.75rem;
    }
    
    .form-container {
      padding: 0 1.5rem 1.5rem;
    }
  }
  </style>