<template>
  <div class="container">
    <div class="component-card login-form">
      <h1 class="text-center">Login</h1>
      <div class="form-wrapper">
        <form @submit.prevent="handleLogin">
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

          <div class="form-group mb-3">
            <div class="flex justify-content-between">
              <label for="password">Password</label>
            </div>
            <Password 
              id="password" 
              v-model="password" 
              :feedback="false"
              toggleMask
              class="w-full" 
              :class="{ 'p-invalid': validationErrors.password }"
              aria-describedby="password-error"
              placeholder="Enter your password"
              value="password123"
              required
            />
            <small id="password-error" class="p-error">{{ validationErrors.password }}</small>
            <div class="flex justify-content-between">
              <router-link to="/reset-password" class="forgot-password mt-4">Forgot password?</router-link>
            </div>
          </div>


          <Button 
            type="submit" 
            label="Sign In" 
            icon="pi pi-sign-in" 
            class="w-full p-button-text p-button-rounded" 
            :loading="loading"
          />
        </form>
        
        <div v-if="errorMessage" class="error-message mt-3 p-error">
          {{ errorMessage }}
        </div>
      </div>
    </div>
    <Toast />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import AuthService from '../services/AuthService';

const router = useRouter();
const toast = useToast();
const email = ref('');
const password = ref('');
const rememberMe = ref(false);
const loading = ref(false);
const errorMessage = ref('');
const validationErrors = ref({
  email: '',
  password: ''
});

onMounted(() => {
  // If already logged in, redirect to home
  if (AuthService.isAuthenticated()) {
    router.push('/');
  }
});

const validateForm = () => {
  let isValid = true;
  validationErrors.value = {
    email: '',
    password: ''
  };

  // Email validation
  if (!email.value) {
    validationErrors.value.email = 'Email is required';
    isValid = false;
  } else if (!/^\S+@\S+\.\S+$/.test(email.value)) {
    validationErrors.value.email = 'Please enter a valid email address';
    isValid = false;
  }

  // Password validation
  if (!password.value) {
    validationErrors.value.password = 'Password is required';
    isValid = false;
  }

  return isValid;
};

const handleLogin = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    // Call the AuthService login method
    await AuthService.login(email.value, password.value, rememberMe.value);
    
    // Show success message
    toast.add({
      severity: 'success',
      summary: 'Logged In',
      detail: 'You have successfully logged in',
      life: 3000
    });
    
    // Redirect to home page
    router.push('/');
    
  } catch (error) {
    console.error('Login error:', error);
    
    // Handle specific error messages from the backend
    if (error.message) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = 'An unexpected error occurred. Please try again.';
    }
    
    toast.add({
      severity: 'error',
      summary: 'Login Failed',
      detail: errorMessage.value,
      life: 5000
    });
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-form {
  max-width: 480px;
  margin: 2rem auto;
}

.form-wrapper {
  padding: 1rem 0;
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

:deep(.p-password-input) {
  width: 100%;
}
</style>