<template>
  <div class="container">
    <div class="component-card reset-password-form">
      <h1 class="text-center">Reset Password</h1>
      
      <!-- Request Password Reset Form -->
      <div v-if="!token" class="form-wrapper">
        <p class="instruction-text">
          Enter your email address below to receive a password reset link.
        </p>
        
        <form @submit.prevent="handleRequestReset">
          <div class="form-group mb-3">
            <label for="email">Email</label>
            <InputText 
              id="email" 
              v-model="email" 
              type="email" 
              class="w-full"
              :class="{ 'p-invalid': validationErrors.email }" 
              placeholder="Enter your email"
              required
            />
            <small class="p-error">{{ validationErrors.email }}</small>
          </div>

          <div class="form-actions">
            <Button 
              type="submit" 
              label="Request Reset Link" 
              icon="pi pi-envelope" 
              class="w-full" 
              :loading="loading"
            />
            
            <Button 
              type="button" 
              label="Back to Login" 
              icon="pi pi-arrow-left" 
              class="w-full mt-2 p-button-outlined" 
              @click="$router.push('/login')"
            />
          </div>
        </form>
        
        <div v-if="requestSuccess" class="success-message mt-3">
          <i class="pi pi-check-circle" style="font-size: 1.5rem"></i>
          <p>A password reset link has been sent to your email if it exists in our system.</p>
        </div>
      </div>
      
      <!-- Reset Password Form (when token is present) -->
      <div v-else class="form-wrapper">
        <p class="instruction-text">
          Please enter your new password.
        </p>
        
        <form @submit.prevent="handleResetPassword">
          <div class="form-group mb-3">
            <label for="password">New Password</label>
            <Password 
              id="password" 
              v-model="password" 
              toggleMask 
              class="w-full"
              :class="{ 'p-invalid': validationErrors.password }" 
              placeholder="Enter new password"
              required
            />
            <small class="p-error">{{ validationErrors.password }}</small>
            <small class="form-help-text">
              Password must be at least 8 characters and include letters, numbers, and special characters.
            </small>
          </div>

          <div class="form-group mb-3">
            <label for="confirmPassword">Confirm New Password</label>
            <Password 
              id="confirmPassword" 
              v-model="confirmPassword" 
              toggleMask 
              :feedback="false"
              class="w-full"
              :class="{ 'p-invalid': validationErrors.confirmPassword }" 
              placeholder="Confirm new password"
              required
            />
            <small class="p-error">{{ validationErrors.confirmPassword }}</small>
          </div>

          <div class="form-actions">
            <Button 
              type="submit" 
              label="Reset Password" 
              icon="pi pi-check" 
              class="w-full" 
              :loading="loading"
            />
            
            <Button 
              type="button" 
              label="Back to Login" 
              icon="pi pi-arrow-left" 
              class="w-full mt-2 p-button-outlined" 
              @click="$router.push('/login')"
            />
          </div>
        </form>
        
        <div v-if="resetSuccess" class="success-message mt-3">
          <i class="pi pi-check-circle" style="font-size: 1.5rem"></i>
          <p>Your password has been successfully reset. You can now login with your new password.</p>
          <Button 
            type="button" 
            label="Go to Login" 
            icon="pi pi-sign-in" 
            class="mt-3" 
            @click="$router.push('/login')"
          />
        </div>
      </div>
      
      <div v-if="errorMessage" class="error-message mt-3">
        <i class="pi pi-times-circle" style="font-size: 1.5rem"></i>
        <p>{{ errorMessage }}</p>
      </div>
    </div>
    <Toast />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import AuthService from '../services/AuthService';

// Router and route
const route = useRoute();
const router = useRouter();
const toast = useToast();

// Form data
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const token = ref('');

// UI state
const loading = ref(false);
const errorMessage = ref('');
const requestSuccess = ref(false);
const resetSuccess = ref(false);
const validationErrors = reactive({
  email: '',
  password: '',
  confirmPassword: ''
});

// Check for token in URL on component mount
onMounted(() => {
  // Get token from URL parameter
  token.value = route.query.token || '';
});

// Function to handle requesting password reset
const handleRequestReset = async () => {
  // Reset states
  errorMessage.value = '';
  requestSuccess.value = false;
  
  // Validate email
  if (!validateEmail()) {
    return;
  }
  
  try {
    loading.value = true;
    
    // Call API to request password reset
    await AuthService.requestPasswordReset(email.value);
    
    // Show success message
    requestSuccess.value = true;
    toast.add({
      severity: 'success',
      summary: 'Request Sent',
      detail: 'Password reset instructions have been sent to your email',
      life: 5000
    });
    
  } catch (error) {
    console.error('Request password reset error:', error);
    
    // Show generic error message for security
    errorMessage.value = 'We encountered an issue with your request. Please try again later.';
    
    toast.add({
      severity: 'error',
      summary: 'Request Failed',
      detail: errorMessage.value,
      life: 5000
    });
  } finally {
    loading.value = false;
  }
};

// Function to handle resetting password
const handleResetPassword = async () => {
  // Reset states
  errorMessage.value = '';
  resetSuccess.value = false;
  
  // Validate passwords
  if (!validatePasswords()) {
    return;
  }
  
  try {
    loading.value = true;
    
    // Call API to reset password
    await AuthService.resetPassword(token.value, password.value);
    
    // Show success message
    resetSuccess.value = true;
    toast.add({
      severity: 'success',
      summary: 'Password Reset',
      detail: 'Your password has been successfully reset',
      life: 5000
    });
    
  } catch (error) {
    console.error('Reset password error:', error);
    
    // Show error message
    errorMessage.value = error.message || 'Password reset failed. The link may be expired or invalid.';
    
    toast.add({
      severity: 'error',
      summary: 'Reset Failed',
      detail: errorMessage.value,
      life: 5000
    });
  } finally {
    loading.value = false;
  }
};

// Validation functions
const validateEmail = () => {
  validationErrors.email = '';
  
  if (!email.value) {
    validationErrors.email = 'Email is required';
    return false;
  }
  
  if (!/^\S+@\S+\.\S+$/.test(email.value)) {
    validationErrors.email = 'Please enter a valid email address';
    return false;
  }
  
  return true;
};

const validatePasswords = () => {
  validationErrors.password = '';
  validationErrors.confirmPassword = '';
  let isValid = true;
  
  // Password validation
  if (!password.value) {
    validationErrors.password = 'Password is required';
    isValid = false;
  } else if (password.value.length < 8) {
    validationErrors.password = 'Password must be at least 8 characters';
    isValid = false;
  } else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(password.value)) {
    validationErrors.password = 'Password must include letters, numbers, and special characters';
    isValid = false;
  }
  
  // Confirm password validation
  if (!confirmPassword.value) {
    validationErrors.confirmPassword = 'Please confirm your password';
    isValid = false;
  } else if (password.value !== confirmPassword.value) {
    validationErrors.confirmPassword = 'Passwords do not match';
    isValid = false;
  }
  
  return isValid;
};
</script>

<style scoped>
.reset-password-form {
  max-width: 500px;
  margin: 2rem auto;
}

.form-wrapper {
  padding: 1rem 0;
}

.instruction-text {
  margin-bottom: 1.5rem;
  text-align: center;
  color: var(--text-color);
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
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-color);
  opacity: 0.8;
}

.success-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1rem;
  background-color: rgba(76, 175, 80, 0.1);
  border-radius: 0.5rem;
  margin-top: 1.5rem;
}

.success-message i {
  color: #4CAF50;
  margin-bottom: 0.5rem;
}

.error-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1rem;
  background-color: rgba(244, 67, 54, 0.1);
  border-radius: 0.5rem;
  margin-top: 1.5rem;
}

.error-message i {
  color: #F44336;
  margin-bottom: 0.5rem;
}

:deep(.p-password-input) {
  width: 100%;
}
</style>