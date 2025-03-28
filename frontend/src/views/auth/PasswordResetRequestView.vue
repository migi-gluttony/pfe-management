<template>
  <div class="login-container">
    <div class="component-card login-form">
      <h1 class="text-center">Réinitialisation de mot de passe</h1>
      <div class="form-wrapper">
        <form @submit.prevent="handleResetRequest">
          <!-- Email input -->
          <div class="form-group mb-3">
            <label for="email">Adresse e-mail</label>
            <div class="p-input-icon-left w-full">
              <InputText 
                id="email" 
                v-model="email" 
                :class="{ 'p-invalid': validationErrors.email }"
                class="w-full" 
                placeholder="votrenom.efb@usms.ac.ma" 
                aria-describedby="email-error"
              />
            </div>
            <small id="email-error" class="p-error">{{ validationErrors.email }}</small>
          </div>
          
          <!-- Date de naissance -->
          <div class="form-group mb-3">
            <label for="dateNaissance">Date de naissance</label>
            <Calendar 
              id="dateNaissance" 
              v-model="dateNaissance" 
              :class="{ 'p-invalid': validationErrors.dateNaissance }"
              dateFormat="dd/mm/yy"
              class="w-full" 
              placeholder="JJ/MM/AAAA"
              showIcon
              autocomplete="bday"

              aria-describedby="dateNaissance-error"
            />
            <small id="dateNaissance-error" class="p-error">{{ validationErrors.dateNaissance }}</small>
          </div>
          
          <!-- Identification type selection (CNE or CNI) -->
          <div class="form-group mb-3">
            <label>Type d'identification</label>
            <div class="p-selectbutton-sm w-full">
              <SelectButton v-model="identificationType" :options="identificationOptions" optionLabel="label" class="w-full" />
            </div>
          </div>
          
          <!-- CNI input (for ENCADRANT, JURY, CHEF_DE_DEPARTEMENT) -->
          <div v-if="identificationType.value === 'cni'" class="form-group mb-3">
            <label for="cni">Numéro CNI</label>
            <div class="p-input-icon-left w-full">
              <InputText 
                id="cni" 
                v-model="cni" 
                :class="{ 'p-invalid': validationErrors.cni }"
                class="w-full" 
                placeholder="Entrez votre numéro CNI" 
                aria-describedby="cni-error"
              />
            </div>
            <small id="cni-error" class="p-error">{{ validationErrors.cni }}</small>
          </div>
          
          <!-- CNE input (for ETUDIANT) -->
          <div v-if="identificationType.value === 'cne'" class="form-group mb-3">
            <label for="cne">Numéro CNE</label>
            <div class="p-input-icon-left w-full">
              <InputText 
                id="cne" 
                v-model="cne" 
                :class="{ 'p-invalid': validationErrors.cne }"
                class="w-full" 
                placeholder="Entrez votre numéro CNE" 
                aria-describedby="cne-error"
              />
            </div>
            <small id="cne-error" class="p-error">{{ validationErrors.cne }}</small>
          </div>
          
          <!-- Submit button -->
          <Button 
            type="submit" 
            label="Vérifier l'identité" 
            icon="pi pi-check-circle" 
            class="w-full p-button-text p-button-rounded" 
            :loading="loading"
          />
          
          <!-- Back to login link -->
          <div class="mt-3 text-center">
            <router-link to="/login" class="forgot-password">
              <i class="pi pi-arrow-left"></i> Retour à la page de connexion
            </router-link>
          </div>
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import AuthService from '@/services/AuthService';
import Calendar from 'primevue/calendar';
import SelectButton from 'primevue/selectbutton';

import DatePicker from 'primevue/datepicker';

// Component state
const router = useRouter();
const toast = useToast();
const email = ref('');
const dateNaissance = ref(null);
const cni = ref('');
const cne = ref('');
const identificationType = ref({ value: 'cni', label: 'CNI (Enseignants)' });
const identificationOptions = ref([
  { value: 'cni', label: 'CNI (Enseignants)' },
  { value: 'cne', label: 'CNE (Étudiants)' }
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

// On component mount
onMounted(() => {
  // If already logged in, redirect to dashboard
  if (AuthService.isAuthenticated()) {
    router.push('/dashboard');
  }
});

// Form validation
const validateForm = () => {
  let isValid = true;
  validationErrors.value = {
    email: '',
    dateNaissance: '',
    cni: '',
    cne: ''
  };

  // Email validation
  if (!email.value) {
    validationErrors.value.email = 'L\'email est requis';
    isValid = false;
  } else if (!/^\S+@\S+\.\S+$/.test(email.value)) {
    validationErrors.value.email = 'Veuillez saisir une adresse email valide';
    isValid = false;
  }

  // Date de naissance validation
  if (!dateNaissance.value) {
    validationErrors.value.dateNaissance = 'La date de naissance est requise';
    isValid = false;
  }

  // ID validation based on type
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

// Handle reset request
const handleResetRequest = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    // Prepare reset request data
    const resetData = {
      email: email.value,
      dateNaissance: dateNaissance.value,
      // Include only the appropriate ID type
      ...(identificationType.value.value === 'cni' ? { cni: cni.value } : { cne: cne.value })
    };

    // Call API to request password reset
    const response = await AuthService.requestPasswordReset(resetData);
    
    // Store the token for the next step
    if (response.token) {
      resetToken.value = response.token;
      
      // Show success message
      successMessage.value = 'Vérification réussie. Vous allez être redirigé vers la page de réinitialisation du mot de passe.';
      
      toast.add({
        severity: 'success',
        summary: 'Vérification réussie',
        detail: 'Votre identité a été vérifiée avec succès',
        life: 3000
      });
      
      // Redirect to reset confirmation page with token
      setTimeout(() => {
        router.push({
          name: 'resetPasswordConfirm',
          query: { token: resetToken.value }
        });
      }, 2000);
    }
  } catch (error) {
    console.error('Password reset request error:', error);
    
    // Handle error messages
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
  padding: 1rem;
}

.login-form {
  max-width: 35%;
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

:deep(.p-calendar), :deep(.p-calendar-w-btn) {
  width: 100%;
}

:deep(.p-password-input) {
  width: 100%;
}
</style>