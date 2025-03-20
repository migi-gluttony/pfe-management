<template>
    <div class="container">
      <div class="component-card reset-password-form">
        <h1 class="text-center">Reset Password</h1>
        
        <!-- Step 1: Request reset link -->
        <div v-if="currentStep === 'request'" class="form-wrapper">
          <p class="instruction-text">
            Enter your email address and we'll send you a link to reset your password.
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
                aria-describedby="email-error"
                placeholder="Enter your email"
                required
              />
              <small id="email-error" class="p-error">{{ validationErrors.email }}</small>
            </div>
  
            <div class="form-actions">
              <Button 
                type="submit" 
                label="Send Reset Link" 
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
        </div>
        
        <!-- Step 2: Success message after sending reset link -->
        <div v-else-if="currentStep === 'sent'" class="form-wrapper">
          <div class="success-message">
            <i class="pi pi-check-circle" style="font-size: 3rem"></i>
            <h2>Check Your Email</h2>
            <p>
              We've sent a password reset link to <strong>{{ email }}</strong>.
              Please check your inbox and click on the link to reset your password.
            </p>
            <p class="mt-3">
              Didn't receive the email? Check your spam folder or 
              <a href="#" @click.prevent="currentStep = 'request'">try again</a>.
            </p>
            
            <Button 
              type="button" 
              label="Back to Login" 
              icon="pi pi-arrow-left" 
              class="mt-4 p-button-outlined" 
              @click="$router.push('/login')"
            />
          </div>
        </div>
        
        <!-- Step 3: Reset password form (when user clicks link from email) -->
        <div v-else-if="currentStep === 'reset'" class="form-wrapper">
          <p class="instruction-text">
            Create a new password for your account.
          </p>
          
          <form @submit.prevent="handlePasswordReset">
            <div class="form-group mb-3">
              <label for="new-password">New Password</label>
              <Password 
                id="new-password" 
                v-model="newPassword" 
                toggleMask 
                class="w-full"
                :class="{ 'p-invalid': validationErrors.newPassword }" 
                aria-describedby="new-password-error"
                placeholder="Enter new password"
                required
              />
              <small id="new-password-error" class="p-error">{{ validationErrors.newPassword }}</small>
              <small class="form-help-text">
                Password must be at least 8 characters and include letters, numbers, and special characters.
              </small>
            </div>
  
            <div class="form-group mb-3">
              <label for="confirm-password">Confirm New Password</label>
              <Password 
                id="confirm-password" 
                v-model="confirmPassword" 
                toggleMask 
                :feedback="false"
                class="w-full"
                :class="{ 'p-invalid': validationErrors.confirmPassword }" 
                aria-describedby="confirm-password-error"
                placeholder="Confirm new password"
                required
              />
              <small id="confirm-password-error" class="p-error">{{ validationErrors.confirmPassword }}</small>
            </div>
  
            <Button 
              type="submit" 
              label="Reset Password" 
              icon="pi pi-check" 
              class="w-full" 
              :loading="loading"
            />
          </form>
        </div>
        
        <!-- Step 4: Success message after password reset -->
        <div v-else-if="currentStep === 'complete'" class="form-wrapper">
          <div class="success-message">
            <i class="pi pi-check-circle" style="font-size: 3rem"></i>
            <h2>Password Reset Successful</h2>
            <p>
              Your password has been successfully reset. You can now log in with your new password.
            </p>
            
            <Button 
              type="button" 
              label="Go to Login" 
              icon="pi pi-sign-in" 
              class="mt-4" 
              @click="$router.push('/login')"
            />
          </div>
        </div>
        
        <div v-if="errorMessage" class="error-message mt-3 p-error">
          {{ errorMessage }}
        </div>
      </div>
      <Toast />
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useToast } from 'primevue/usetoast';
  import AuthService from '../services/AuthService';
  
  const route = useRoute();
  const router = useRouter();
  const toast = useToast();
  const email = ref('');
  const newPassword = ref('');
  const confirmPassword = ref('');
  const token = ref('');
  const loading = ref(false);
  const errorMessage = ref('');
  const currentStep = ref('request'); // 'request', 'sent', 'reset', 'complete'
  const validationErrors = ref({
    email: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  onMounted(() => {
    // Check if we have token in URL parameters (from email link)
    token.value = route.query.token || '';
    
    if (token.value) {
      currentStep.value = 'reset';
      // You might want to validate the token here before showing the reset form
    }
  });
  
  const validateEmail = () => {
    validationErrors.value.email = '';
    
    if (!email.value) {
      validationErrors.value.email = 'Email is required';
      return false;
    } else if (!/^\S+@\S+\.\S+$/.test(email.value)) {
      validationErrors.value.email = 'Please enter a valid email address';
      return false;
    }
    
    return true;
  };
  
  const validatePasswords = () => {
    validationErrors.value.newPassword = '';
    validationErrors.value.confirmPassword = '';
    let isValid = true;
    
    // Validate new password
    if (!newPassword.value) {
      validationErrors.value.newPassword = 'New password is required';
      isValid = false;
    } else if (newPassword.value.length < 8) {
      validationErrors.value.newPassword = 'Password must be at least 8 characters';
      isValid = false;
    } else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(newPassword.value)) {
      validationErrors.value.newPassword = 'Password must include letters, numbers, and special characters';
      isValid = false;
    }
    
    // Validate password confirmation
    if (!confirmPassword.value) {
      validationErrors.value.confirmPassword = 'Please confirm your password';
      isValid = false;
    } else if (newPassword.value !== confirmPassword.value) {
      validationErrors.value.confirmPassword = 'Passwords do not match';
      isValid = false;
    }
    
    return isValid;
  };
  
  const handleRequestReset = async () => {
    if (!validateEmail()) {
      return;
    }
    
    try {
      loading.value = true;
      errorMessage.value = '';
      
      // Call the AuthService to request password reset
      await AuthService.requestPasswordReset(email.value);
      
      // Show success toast
      toast.add({
        severity: 'success',
        summary: 'Link Sent',
        detail: 'Password reset link has been sent to your email',
        life: 3000
      });
      
      // Move to the success screen
      currentStep.value = 'sent';
      
    } catch (error) {
      console.error('Request reset error:', error);
      
      // Handle specific error messages from the backend
      if (error.message) {
        errorMessage.value = error.message;
      } else {
        errorMessage.value = 'An unexpected error occurred. Please try again.';
      }
      
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
  
  const handlePasswordReset = async () => {
    if (!validatePasswords()) {
      return;
    }
    
    try {
      loading.value = true;
      errorMessage.value = '';
      
      // Call the AuthService to reset the password
      await AuthService.resetPassword(token.value, newPassword.value);
      
      // Show success toast
      toast.add({
        severity: 'success',
        summary: 'Password Reset',
        detail: 'Your password has been successfully reset',
        life: 3000
      });
      
      // Move to completion screen
      currentStep.value = 'complete';
      
    } catch (error) {
      console.error('Password reset error:', error);
      
      // Handle specific error messages from the backend
      if (error.message) {
        errorMessage.value = error.message;
      } else {
        errorMessage.value = 'An unexpected error occurred. Please try again.';
      }
      
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
  </script>
  
  <style scoped>
  .reset-password-form {
    max-width: 480px;
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
    text-align: center;
    padding: 1.5rem 0;
  }
  
  .success-message i {
    color: var(--primary-color);
    margin-bottom: 1rem;
  }
  
  .success-message h2 {
    margin-bottom: 1rem;
  }
  
  .error-message {
    text-align: center;
    padding: 0.75rem;
    border-radius: 0.25rem;
    background-color: rgba(244, 67, 54, 0.1);
  }
  
  :deep(.p-password-input) {
    width: 100%;
  }
  </style>