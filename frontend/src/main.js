import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import PrimeVue from 'primevue/config';
import Aura from '@primeuix/themes/aura';
import 'primevue/resources/primevue.min.css'; // Core styles
import 'primeicons/primeicons.css'; // PrimeIcons
import 'primeflex/primeflex.css'; // Optional - PrimeFlex for utilities


const app = createApp(App);

app.use(router)

app.mount('#app')

app.use(PrimeVue, {
    theme: {
        preset: Aura
    }
});