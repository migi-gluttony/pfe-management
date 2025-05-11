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
          <img src="@/assets/illustrations/Forgot password-rafiki.svg" alt="Password Reset Illustration" />
        </div>
      </div>
    </div>
    
    <div class="auth-right">
      <div class="auth-card">
        <div class="auth-header">
          <div class="back-button">
            <router-link to="/login" class="back-link">
              <i class="pi pi-arrow-left"></i>
              Retour
            </router-link>
          </div>
          <h2>Mot de passe oublié ?</h2>
          <p>Vérifiez votre identité pour réinitialiser votre mot de passe</p>
        </div>
        
        <form @submit.prevent="handleResetRequest" class="auth-form">
          <div class="form-group">
            <label for="email" class="form-label">
              <i class="pi pi-envelope"></i>
              Adresse e-mail (e-mail universitaire)
            </label>
            <InputText 
              id="email" 
              v-model="email" 
              type="email"
              class="form-input"
              :class="{ 'p-invalid': validationErrors.email }"
              placeholder="votrenom.efb@usms.ac.ma" 
              aria-describedby="email-error"
            />
            <small id="email-error" class="p-error form-error">{{ validationErrors.email }}</small>
          </div>
          
          <div class="form-group">
            <label for="dateNaissance" class="form-label">
              <i class="pi pi-calendar"></i>
              Date de naissance
            </label>
            <Calendar 
              id="dateNaissance" 
              v-model="dateNaissance" 
              :class="{ 'p-invalid': validationErrors.dateNaissance }"
              dateFormat="dd/mm/yy"
              class="form-input calendar-input" 
              placeholder="JJ/MM/AAAA"
              showIcon
              autocomplete="bday"
              aria-describedby="dateNaissance-error"
            />
            <small id="dateNaissance-error" class="p-error form-error">{{ validationErrors.dateNaissance }}</small>
          </div>
          
          <div class="form-group">
            <label class="form-label">Type d'identification</label>
            <div class="id-type-selector">
              <div 
                v-for="option in identificationOptions" 
                :key="option.value"
                class="id-option"
                :class="{ active: identificationType.value === option.value }"
                @click="identificationType = option"
              >
                <i :class="option.icon"></i>
                <span>{{ option.label }}</span>
              </div>
            </div>
          </div>
          
          <div v-if="identificationType.value === 'cni'" class="form-group">
            <label for="cni" class="form-label">
              <i class="pi pi-id-card"></i>
              Numéro CNI
            </label>
            <InputText 
              id="cni" 
              v-model="cni" 
              class="form-input"
              :class="{ 'p-invalid': validationErrors.cni }"
              placeholder="Entrez votre numéro CNI" 
              aria-describedby="cni-error"
            />
            <small id="cni-error" class="p-error form-error">{{ validationErrors.cni }}</small>
          </div>
          
          <div v-if="identificationType.value === 'cne'" class="form-group">
            <label for="cne" class="form-label">
              <i class="pi pi-id-card"></i>
              Numéro CNE
            </label>
            <InputText 
              id="cne" 
              v-model="cne" 
              class="form-input"
              :class="{ 'p-invalid': validationErrors.cne }"
              placeholder="Entrez votre numéro CNE" 
              aria-describedby="cne-error"
            />
            <small id="cne-error" class="p-error form-error">{{ validationErrors.cne }}</small>
          </div>
          
          <Button 
            type="submit" 
            :label="loading ? 'Vérification...' : 'Vérifier mon identité'" 
            icon="pi pi-check-circle" 
            class="submit-button" 
            :loading="loading"
            :disabled="loading"
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import AuthService from '@/services/AuthService';
import Calendar from 'primevue/calendar';

const router = useRouter();
const toast = useToast();
const email = ref('');
const dateNaissance = ref(null);
const cni = ref('');
const cne = ref('');
const identificationType = ref({ value: 'cni', label: 'CNI (Enseignants)', icon: 'pi pi-briefcase' });
const identificationOptions = ref([
  { value: 'cni', label: 'CNI (Enseignants)', icon: 'pi pi-briefcase' },
  { value: 'cne', label: 'CNE (Étudiants)', icon: 'pi pi-graduation-cap' }
]);

const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const validationErrors = ref({
  email: '',
  dateNaissance: '',
  cni: '',
  cne: ''
});
const resetToken = ref('');

onMounted(() => {
  if (AuthService.isAuthenticated()) {
    router.push('/dashboard');
  }
});

const validateForm = () => {
  let isValid = true;
  validationErrors.value = {
    email: '',
    dateNaissance: '',
    cni: '',
    cne: ''
  };

  if (!email.value) {
    validationErrors.value.email = 'L\'email est requis';
    isValid = false;
  } else if (!/^\S+@\S+\.\S+$/.test(email.value)) {
    validationErrors.value.email = 'Veuillez saisir une adresse email valide';
    isValid = false;
  }

  if (!dateNaissance.value) {
    validationErrors.value.dateNaissance = 'La date de naissance est requise';
    isValid = false;
  }

  if (identificationType.value.value === 'cni') {
    if (!cni.value) {
      validationErrors.value.cni = 'Le numéro CNI est requis';
      isValid = false;
    }
  } else {
    if (!cne.value) {
      validationErrors.value.cne = 'Le numéro CNE est requis';
      isValid = false;
    }
  }

  return isValid;
};

const handleResetRequest = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    const resetData = {
      email: email.value,
      dateNaissance: dateNaissance.value,
      ...(identificationType.value.value === 'cni' ? { cni: cni.value } : { cne: cne.value })
    };

    const response = await AuthService.requestPasswordReset(resetData);
    
    if (response.token) {
      resetToken.value = response.token;
      
      successMessage.value = 'Vérification réussie. Vous allez être redirigé vers la page de réinitialisation du mot de passe.';
      
      toast.add({
        severity: 'success',
        summary: 'Vérification réussie',
        detail: 'Votre identité a été vérifiée avec succès',
        life: 3000
      });
      
      setTimeout(() => {
        router.push({
          name: 'resetPasswordConfirm',
          query: { token: resetToken.value }
        });
      }, 2000);
    }
  } catch (error) {
    console.error('Password reset request error:', error);
    
    if (error.message) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = 'Une erreur s\'est produite lors de la vérification de votre identité. Veuillez réessayer.';
    }
    
    toast.add({
      severity: 'error',
      summary: 'Échec de la vérification',
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
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.brand-logo {
  margin-top: 2rem;
  margin-bottom: 1.5rem;
}

.logo-img {
  height: 150px;
  filter: brightness(0) invert(1);
}





.auth-illustration {
  margin-top: 2rem;
}

.auth-illustration img {
  max-width: 76%;
  width: 730px;
  filter: drop-shadow(0 10px 25px rgba(0,0,0,0.1));
}


.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-card {
  width: 100%;
  max-width: 650px;
  background: var(--surface-card);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.auth-header {
  text-align: center;
  margin-bottom: 2.5rem;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  top: -10px;
}

.back-link {
  color: var(--text-color-secondary);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.back-link:hover {
  color: var(--primary-color);
  transform: translateX(-5px);
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

.id-type-selector {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.id-option {
  flex: 1;
  padding: 1rem;
  border: 2px solid var(--surface-border);
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.id-option i {
  font-size: 1.5rem;
  color: var(--text-color-secondary);
}

.id-option span {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-color-secondary);
}

.id-option.active {
  border-color: var(--primary-color);
  background-color: rgba(var(--primary-color-rgb), 0.05);
}

.id-option.active i,
.id-option.active span {
  color: var(--primary-color);
}

.id-option:hover:not(.active) {
  border-color: var(--surface-border-hover);
  background-color: var(--surface-hover);
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

/* Calendar styling */
:deep(.p-calendar) {
  width: 100%;
}

:deep(.p-calendar .p-inputtext) {
  width: 100%;
  padding: 0.875rem 1rem;
  border-radius: 10px;
  border: 2px solid var(--surface-border);
  background: var(--surface-ground);
  transition: all 0.2s ease;
}

:deep(.p-calendar .p-inputtext:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(var(--primary-color-rgb), 0.1);
}

:deep(.p-calendar .p-icon) {
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
  
  .id-type-selector {
    flex-direction: column;
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