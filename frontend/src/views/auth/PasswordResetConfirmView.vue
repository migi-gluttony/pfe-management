<template>
  <div class="auth-container">
    <div class="auth-left">
      <div class="auth-left-content">
        <div class="brand-logo">
        <router-link to="/" class="logo-link">

          <img src="@/assets/pfe_man_logo_whole_final.svg" alt="Logo" class="logo-img" />
                  </router-link>

        </div>
        <div class="auth-illustration">
          <img src="@/assets/illustrations/Reset password-bro.svg" alt="Reset Password Illustration" />
        </div>
      </div>
    </div>
    
    <div class="auth-right">
      <div class="auth-card">
        <div class="auth-header">
          <h2>Nouveau mot de passe</h2>
          <p>Créez un mot de passe fort et sécurisé</p>
        </div>
        
        <form @submit.prevent="handleResetConfirm" class="auth-form">
          <div class="form-group">
            <label for="newPassword" class="form-label">
              <i class="pi pi-lock"></i>
              Nouveau mot de passe
            </label>
            <Password 
              id="newPassword" 
              v-model="newPassword" 
              :class="{ 'p-invalid': validationErrors.newPassword }"
              class="form-input password-input" 
              placeholder="Créez un mot de passe fort"
              :feedback="true"
              aria-describedby="newPassword-error"
              toggleMask
              @input="checkPasswordStrength"
            />
            <small id="newPassword-error" class="p-error form-error">{{ validationErrors.newPassword }}</small>
            
            <div class="password-requirements">
              <div class="requirement" :class="{ met: requirements.length }">
                <i class="pi" :class="requirements.length ? 'pi-check-circle' : 'pi-circle'"></i>
                Au moins 8 caractères
              </div>
              <div class="requirement" :class="{ met: requirements.uppercase }">
                <i class="pi" :class="requirements.uppercase ? 'pi-check-circle' : 'pi-circle'"></i>
                Une lettre majuscule
              </div>
              <div class="requirement" :class="{ met: requirements.lowercase }">
                <i class="pi" :class="requirements.lowercase ? 'pi-check-circle' : 'pi-circle'"></i>
                Une lettre minuscule
              </div>
              <div class="requirement" :class="{ met: requirements.number }">
                <i class="pi" :class="requirements.number ? 'pi-check-circle' : 'pi-circle'"></i>
                Un chiffre
              </div>
              <div class="requirement" :class="{ met: requirements.special }">
                <i class="pi" :class="requirements.special ? 'pi-check-circle' : 'pi-circle'"></i>
                Un caractère spécial (!@#$...)
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label for="confirmPassword" class="form-label">
              <i class="pi pi-lock"></i>
              Confirmer le mot de passe
            </label>
            <Password 
              id="confirmPassword" 
              v-model="confirmPassword" 
              :class="{ 'p-invalid': validationErrors.confirmPassword }"
              class="form-input password-input" 
              placeholder="Confirmez votre mot de passe"
              aria-describedby="confirmPassword-error"
              :feedback="false"
              toggleMask
            />
            <small id="confirmPassword-error" class="p-error form-error">{{ validationErrors.confirmPassword }}</small>
          </div>
          
          <Button 
            type="submit" 
            :label="loading ? 'Réinitialisation...' : 'Réinitialiser le mot de passe'" 
            icon="pi pi-check" 
            class="submit-button" 
            :loading="loading"
            :disabled="loading || !allRequirementsMet"
          />
        </form>
        
        <div v-if="errorMessage" class="error-message">
          <i class="pi pi-exclamation-circle"></i>
          {{ errorMessage }}
        </div>
        
        <div v-if="successMessage" class="success-message">
          <i class="pi pi-check-circle"></i>
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

const requirements = ref({
  length: false,
  uppercase: false,
  lowercase: false,
  number: false,
  special: false
});

const allRequirementsMet = computed(() => {
  return Object.values(requirements.value).every(req => req === true);
});

onMounted(() => {
  if (!token.value) {
    errorMessage.value = 'Aucun jeton de réinitialisation n\'a été fourni. Veuillez recommencer le processus de réinitialisation.';
    setTimeout(() => {
      router.push('/reset-password');
    }, 3000);
  }
});

const checkPasswordStrength = () => {
  const password = newPassword.value;
  requirements.value = {
    length: password.length >= 8,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /[0-9]/.test(password),
    special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
  };
};

const validateForm = () => {
  let isValid = true;
  validationErrors.value = {
    newPassword: '',
    confirmPassword: ''
  };

  if (!newPassword.value) {
    validationErrors.value.newPassword = 'Le nouveau mot de passe est requis';
    isValid = false;
  } else if (!allRequirementsMet.value) {
    validationErrors.value.newPassword = 'Le mot de passe ne répond pas aux exigences de sécurité';
    isValid = false;
  }

  if (!confirmPassword.value) {
    validationErrors.value.confirmPassword = 'La confirmation du mot de passe est requise';
    isValid = false;
  } else if (newPassword.value !== confirmPassword.value) {
    validationErrors.value.confirmPassword = 'Les mots de passe ne correspondent pas';
    isValid = false;
  }

  return isValid;
};

const handleResetConfirm = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    await AuthService.confirmPasswordReset(token.value, newPassword.value);
    
    successMessage.value = 'Votre mot de passe a été réinitialisé avec succès.';
    
    toast.add({
      severity: 'success',
      summary: 'Mot de passe réinitialisé',
      detail: 'Votre mot de passe a été réinitialisé avec succès',
      life: 3000
    });
    
    setTimeout(() => {
      router.push('/login');
    }, 2000);
    
  } catch (error) {
    console.error('Password reset confirmation error:', error);
    
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
.auth-container {
  display: flex;
  min-height: 100vh;
  background-color: var(--background-color);
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.auth-left-content {
  text-align: center;
  color: white;
  z-index: 1;
  padding: 2rem;
}

.brand-logo {
  margin-bottom: 1.5rem;
}

.logo-img {
  height: 150px;
  filter: brightness(0) invert(1);
}







.auth-illustration img {
  max-width: 76%;
  width: 730px;
  filter: drop-shadow(0 10px 25px rgba(0,0,0,0.1));
}

.auth-illustration {
  margin-top: 2rem;
}



.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.auth-card {
  width: 100%;
  max-width: 450px;
  background: var(--surface-card);
  border-radius: 20px;
  padding: 3rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.auth-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.progress-step i {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  background-color: var(--surface-border);
  color: var(--text-color-secondary);
}

.progress-step span {
  font-size: 0.85rem;
  color: var(--text-color-secondary);
}

.progress-step.completed i {
  background-color: #4caf50;
  color: white;
}

.progress-step.active i {
  background-color: var(--primary-color);
  color: white;
}

.progress-step.active span {
  color: var(--primary-color);
  font-weight: 600;
}

.progress-line {
  flex: 1;
  height: 2px;
  background-color: var(--surface-border);
  margin: 0 1rem;
  max-width: 80px;
}

.auth-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.auth-header p {
  color: var(--text-color-secondary);
  font-size: 1rem;
}

.auth-form {
  width: 100%;
}

.form-group {
  margin-bottom: 1.75rem;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.form-label i {
  color: var(--primary-color);
  font-size: 1rem;
}

.form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border-radius: 10px;
  border: 2px solid var(--surface-border);
  background: var(--surface-ground);
  transition: all 0.2s ease;
}

.form-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(var(--primary-color-rgb), 0.1);
}

.form-error {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.85rem;
}

.password-requirements {
  margin-top: 1rem;
  padding: 1rem;
  background-color: var(--surface-ground);
  border-radius: 10px;
}

.requirement {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-color-secondary);
  transition: all 0.2s ease;
}

.requirement:last-child {
  margin-bottom: 0;
}

.requirement i {
  font-size: 1rem;
  color: var(--text-color-secondary);
}

.requirement.met {
  color: #4caf50;
}

.requirement.met i {
  color: #4caf50;
}

.submit-button {
  width: 100%;
  padding: 0.875rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(var(--primary-color-rgb), 0.3);
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--primary-color-rgb), 0.4);
}

.submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: var(--surface-400);
}

.error-message {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: rgba(244, 67, 54, 0.1);
  border-radius: 10px;
  color: #f44336;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.success-message {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: rgba(76, 175, 80, 0.1);
  border-radius: 10px;
  color: #4caf50;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

/* Password input styling */
:deep(.p-password) {
  width: 100%;
}

:deep(.p-password-input) {
  width: 100%;
  padding: 0.875rem 1rem;
  border-radius: 10px;
  border: 2px solid var(--surface-border);
  background: var(--surface-ground);
  transition: all 0.2s ease;
}

:deep(.p-password-input:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(var(--primary-color-rgb), 0.1);
}

:deep(.p-password .p-icon) {
  color: var(--text-color-secondary);
}

/* Responsive design */
@media (max-width: 992px) {
  .auth-left {
    display: none;
  }
  
  .auth-right {
    flex: 1;
  }
  
  .auth-card {
    max-width: 100%;
  }
}

@media (max-width: 576px) {
  .auth-card {
    padding: 2rem 1.5rem;
  }
  
  .auth-header h2 {
    font-size: 1.75rem;
  }
  
  .progress-indicator {
    transform: scale(0.9);
  }
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-card {
  animation: fadeIn 0.6s ease both;
}

.auth-left-content {
  animation: fadeIn 0.8s ease both;
  animation-delay: 0.2s;
}
</style>