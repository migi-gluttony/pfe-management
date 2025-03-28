<template>
  <div class="login-container">
    <div class="component-card login-form">
      <h1 class="text-center">Créer un nouveau mot de passe</h1>
      <div class="form-wrapper">
        <form @submit.prevent="handleResetConfirm">
          <!-- New password input -->
          <div class="form-group mb-3">
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
          <div class="form-group mb-3">
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
            class="w-full p-button-text p-button-rounded" 
            :loading="loading"
          />
          
          
        </form>
        
        <!-- Error message -->
        <div v-if="errorMessage" class="error-message mt-3 p-error">
          {{ errorMessage }}
        </div>
        
        <!-- Success message -->
        <div v-if="successMessage" class="success-message mt-3 p-success">
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
  // Check if token is present
  if (!token.value) {
    errorMessage.value = 'Aucun jeton de réinitialisation n\'a été fourni. Veuillez recommencer le processus de réinitialisation.';
    setTimeout(() => {
      router.push('/reset-password');
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
    successMessage.value = 'Votre mot de passe a été réinitialisé avec succès.';
    
    toast.add({
      severity: 'success',
      summary: 'Mot de passe réinitialisé',
      detail: 'Votre mot de passe a été réinitialisé avec succès',
      life: 3000
    });
    
    // Redirect to login page immediately
    router.push('/login');
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
  padding: 1rem;
}

.login-form {
  max-width: 33%;
  width: 100%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.form-wrapper {
  padding: 1.5rem 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-help-text {
  display: block;
  font-size: 0.8rem;
  color: var(--text-color-secondary);
  margin-top: 0.25rem;
}

.forgot-password {
  font-size: 0.875rem;
  color: var(--primary-color);
  text-decoration: none;
}

.forgot-password:hover {
  text-decoration: underline;
}

.error-message {
  text-align: center;
  padding: 0.75rem;
  border-radius: 0.25rem;
  background-color: rgba(244, 67, 54, 0.1);
}

.success-message {
  text-align: center;
  padding: 0.75rem;
  border-radius: 0.25rem;
  background-color: rgba(76, 175, 80, 0.1);
}

:deep(.p-password-input) {
  width: 100%;
}
</style>