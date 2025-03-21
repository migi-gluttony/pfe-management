import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { guest: true }

    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { guest: true }
    },
    {
      path: '/reset-password',
      name: 'resetPassword',
      component: ResetPasswordView,
      meta: { guest: true }
    }
  ]
})

// Navigation guard to check authentication status
router.beforeEach((to, from, next) => {
  // This is a simplified example - in a real application, 
  // you would check if the user is actually authenticated
  const isAuthenticated = localStorage.getItem('token') !== null || sessionStorage.getItem('token') !== null;
  
  // If route requires authentication and user is not authenticated
  if (to.matched.some(record => record.meta.requiresAuth) && !isAuthenticated) {
    next({ name: 'login' });
  } 
  // If route is for guests only and user is authenticated
  else if (to.matched.some(record => record.meta.guest) && isAuthenticated) {
    next({ name: 'home' });
  } 
  // Otherwise proceed normally
  else {
    next();
  }
})

export default router