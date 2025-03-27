import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'

// Import PrimeIcons
import 'primeicons/primeicons.css'

// Import for the PrimeVue components and service mostely used in this project
// services 
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'

// components
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Toast from 'primevue/toast';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import Select from 'primevue/select';
import ConfirmDialog from 'primevue/confirmdialog';

const app = createApp(App)

// PrimeVue Config  with dark mode support
app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
        darkModeSelector: '.dark-mode', // CSS class for dark mode
        }
    } 
})

// baseURL for API
axios.defaults.baseURL = import.meta.env.VITE_API_URL || '/api'


// register primevue components
app.component('Button', Button);
app.component('InputText', InputText);
app.component('Toast', Toast);
app.component('Card', Card);
app.component('DataTable', DataTable);
app.component('Column', Column);
app.component('Dialog', Dialog);
app.component('Select', Select);
app.component('ConfirmDialog', ConfirmDialog);

// register primevue services
app.use(ToastService)
app.use(ConfirmationService)

// use router
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


app.mount('#app')
