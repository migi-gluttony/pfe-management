import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import ToastService from 'primevue/toastservice'

// Import PrimeVue components
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Checkbox from 'primevue/checkbox'
import Toast from 'primevue/toast'
import Tooltip from 'primevue/tooltip'

// Create app instance
const app = createApp(App)

// Configure PrimeVue with dark mode support
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.dark-mode', // CSS class for dark mode
    }
  }
})

// Add global properties
app.config.globalProperties.$axios = axios

// Use PrimeVue services
app.use(ToastService)

// Register PrimeVue components
app.component('Button', Button)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Checkbox', Checkbox)
app.component('Toast', Toast)

// Register directives
app.directive('tooltip', Tooltip)

// Use router
app.use(router)

// Initialize theme based on saved preference or system preference
const initTheme = () => {
  const savedTheme = localStorage.getItem('app-theme-preference')
  
  if (savedTheme === 'dark') {
    document.documentElement.classList.add('dark-mode')
  } else if (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    // If no saved preference, use system preference
    document.documentElement.classList.add('dark-mode')
  }
}

initTheme()

// Mount the app
app.mount('#app')