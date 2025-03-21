<template>
    <div class="container">
      <div class="component-card register-form">
        <h1 class="text-center">Create Account</h1>
        <div class="form-wrapper">
          <form @submit.prevent="handleRegister">
            <!-- Personal Information -->
            <h3>Personal Information</h3>
            
            <div class="form-row">
              <div class="form-group mb-3 p-field">
                <label for="firstName">First Name*</label>
                <InputText 
                  id="firstName" 
                  v-model="user.firstName" 
                  class="w-full"
                  :class="{ 'p-invalid': validationErrors.firstName }" 
                  aria-describedby="firstName-error"
                  placeholder="Enter your first name"
                  required
                />
                <small id="firstName-error" class="p-error">{{ validationErrors.firstName }}</small>
              </div>
              
              <div class="form-group mb-3 p-field">
                <label for="lastName">Last Name*</label>
                <InputText 
                  id="lastName" 
                  v-model="user.lastName" 
                  class="w-full"
                  :class="{ 'p-invalid': validationErrors.lastName }" 
                  aria-describedby="lastName-error"
                  placeholder="Enter your last name"
                  required
                />
                <small id="lastName-error" class="p-error">{{ validationErrors.lastName }}</small>
              </div>
            </div>
  
            <div class="form-group mb-3">
              <label for="email">Email*</label>
              <InputText 
                id="email" 
                v-model="user.email" 
                type="email" 
                class="w-full"
                :class="{ 'p-invalid': validationErrors.email }" 
                aria-describedby="email-error"
                placeholder="Enter your email"
                required
              />
              <small id="email-error" class="p-error">{{ validationErrors.email }}</small>
            </div>
  
            <!-- Account Credentials -->
            <h3>Account Credentials</h3>
            
            <div class="form-group mb-3">
              <label for="password">Password*</label>
              <Password 
                id="password" 
                v-model="user.password" 
                class="w-full"
                :class="{ 'p-invalid': validationErrors.password }"
                toggleMask
                aria-describedby="password-error"
                placeholder="Create a password"
                required
              />
              <small id="password-error" class="p-error">{{ validationErrors.password }}</small>
              <small class="form-help-text">
                Password must be at least 8 characters and include letters, numbers, and special characters.
              </small>
            </div>
  
            <div class="form-group mb-3">
              <label for="confirmPassword">Confirm Password*</label>
              <Password 
                id="confirmPassword" 
                v-model="confirmPassword" 
                class="w-full"
                :class="{ 'p-invalid': validationErrors.confirmPassword }"
                toggleMask
                :feedback="false"
                aria-describedby="confirmPassword-error"
                placeholder="Confirm your password"
                required
              />
              <small id="confirmPassword-error" class="p-error">{{ validationErrors.confirmPassword }}</small>
            </div>
  
            <!-- Role Selection -->
            <div class="form-group mb-3">
              <label for="role">Role*</label>
              <Dropdown
                id="role"
                v-model="user.role"
                :options="roles"
                optionLabel="name"
                optionValue="value"
                placeholder="Select your role"
                class="w-full"
                :class="{ 'p-invalid': validationErrors.role }"
                aria-describedby="role-error"
                required
              />
              <small id="role-error" class="p-error">{{ validationErrors.role }}</small>
            </div>
            
            <!-- Terms and Conditions -->
            <div class="form-group mb-3">
              <div class="p-field-checkbox">
                <Checkbox id="terms" v-model="acceptTerms" :binary="true" :class="{ 'p-invalid': validationErrors.terms }" />
                <label for="terms" class="ml-2">I agree to the <a href="#" @click.prevent="showTerms">Terms and Conditions</a>*</label>
              </div>
              <small class="p-error">{{ validationErrors.terms }}</small>
            </div>
  
            <Button 
              type="submit" 
              label="Register" 
              icon="pi pi-user-plus" 
              class="w-full" 
              :loading="loading"
            />
  
            <div class="text-center mt-3">
              Already have an account? <router-link to="/login">Sign in</router-link>
            </div>
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
  import { ref, reactive } from 'vue';
  import { useRouter } from 'vue-router';
  import { useToast } from 'primevue/usetoast';
  import AuthService from '../services/AuthService';
  import Dropdown from 'primevue/dropdown';
  
  const router = useRouter();
  const toast = useToast();
  const loading = ref(false);
  const errorMessage = ref('');
  const confirmPassword = ref('');
  const acceptTerms = ref(false);
  
  const roles = [
    { name: 'Student', value: 'STUDENT' },
    { name: 'Professor', value: 'PROFESSOR' },
    { name: 'Administrator', value: 'ADMIN' }
  ];
  
  const user = reactive({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: ''
  });
  
  const validationErrors = reactive({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: '',
    terms: ''
  });
  
  const validateForm = () => {
    let isValid = true;
    
    // Reset validation errors
    Object.keys(validationErrors).forEach(key => {
      validationErrors[key] = '';
    });
  
    // Validate first name
    if (!user.firstName) {
      validationErrors.firstName = 'First name is required';
      isValid = false;
    }
  
    // Validate last name
    if (!user.lastName) {
      validationErrors.lastName = 'Last name is required';
      isValid = false;
    }
  
    // Validate email
    if (!user.email) {
      validationErrors.email = 'Email is required';
      isValid = false;
    } else if (!/^\S+@\S+\.\S+$/.test(user.email)) {
      validationErrors.email = 'Please enter a valid email address';
      isValid = false;
    }
  
    // Validate password
    if (!user.password) {
      validationErrors.password = 'Password is required';
      isValid = false;
    } else if (user.password.length < 8) {
      validationErrors.password = 'Password must be at least 8 characters';
      isValid = false;
    } else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(user.password)) {
      validationErrors.password = 'Password must include letters, numbers, and special characters';
      isValid = false;
    }
  
    // Validate confirm password
    if (!confirmPassword.value) {
      validationErrors.confirmPassword = 'Please confirm your password';
      isValid = false;
    } else if (confirmPassword.value !== user.password) {
      validationErrors.confirmPassword = 'Passwords do not match';
      isValid = false;
    }
  
    // Validate role
    if (!user.role) {
      validationErrors.role = 'Please select a role';
      isValid = false;
    }
  
    // Validate terms acceptance
    if (!acceptTerms.value) {
      validationErrors.terms = 'You must accept the terms and conditions';
      isValid = false;
    }
  
    return isValid;
  };
  
  const handleRegister = async () => {
    if (!validateForm()) {
      return;
    }
  
    try {
      loading.value = true;
      errorMessage.value = '';
  
      // Call the AuthService register method
      await AuthService.register(user);
      
      // Show success message
      toast.add({
        severity: 'success',
        summary: 'Registration Successful',
        detail: 'Your account has been created successfully',
        life: 3000
      });
      
      // Redirect to login page
      router.push('/login');
      
    } catch (error) {
      console.error('Registration error:', error);
      
      // Handle specific error messages from the backend
      if (error.message) {
        errorMessage.value = error.message;
      } else {
        errorMessage.value = 'An unexpected error occurred. Please try again.';
      }
      
      toast.add({
        severity: 'error',
        summary: 'Registration Failed',
        detail: errorMessage.value,
        life: 5000
      });
    } finally {
      loading.value = false;
    }
  };
  
  const showTerms = () => {
    // You would normally show a modal with terms and conditions
    toast.add({
      severity: 'info',
      summary: 'Terms and Conditions',
      detail: 'Terms and conditions would be displayed here',
      life: 3000
    });
  };
  </script>
  
  <style scoped>
  .register-form {
    max-width: 600px;
    margin: 2rem auto;
  }
  
  .form-wrapper {
    padding: 1rem 0;
  }
  
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
  
  @media (max-width: 640px) {
    .form-row {
      grid-template-columns: 1fr;
    }
  }
  
  .form-group {
    margin-bottom: 1.5rem;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
  }
  
  h3 {
    margin-top: 1.5rem;
    margin-bottom: 1rem;
    font-size: 1.25rem;
    border-bottom: 1px solid var(--border-color);
    padding-bottom: 0.5rem;
  }
  
  .form-help-text {
    display: block;
    margin-top: 0.5rem;
    font-size: 0.875rem;
    color: var(--text-color);
    opacity: 0.8;
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