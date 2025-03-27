import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import AuthService from '../services/AuthService'

// Management views
import BinomeManagementView from '../views/BinomeManagementView.vue'
import ComptesManagementView from '../views/ComptesManagementView.vue'
import SoutenanceManagementView from '../views/SoutenanceManagementView.vue'
import SujetManagementView from '../views/SujetManagementView.vue'
import SujetSuggestionsView from '../views/SujetSuggestionsView.vue'
import NotesManagementView from '../views/NotesManagementView.vue'


// Create router instance
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Public routes (no authentication required)
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guest: true }
    },
    // Protected routes (authentication required)
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    
    // Department Head (CHEF_DE_DEPARTEMENT) routes
    {
      path: '/management/binomes',
      name: 'binomeManagement',
      component: BinomeManagementView,
      meta: { 
        requiresAuth: true,
        requiresRole: 'CHEF_DE_DEPARTEMENT'
      }
    },
    {
      path: '/management/comptes',
      name: 'comptesManagement',
      component: ComptesManagementView,
      meta: { 
        requiresAuth: true,
        requiresRole: 'CHEF_DE_DEPARTEMENT'
      }
    },
    {
      path: '/management/soutenances',
      name: 'soutenanceManagement',
      component: SoutenanceManagementView,
      meta: { 
        requiresAuth: true,
        requiresRole: 'CHEF_DE_DEPARTEMENT'
      }
    },
    {
      path: '/management/sujets',
      name: 'sujetManagement',
      component: SujetManagementView,
      meta: { 
        requiresAuth: true,
        requiresRole: 'CHEF_DE_DEPARTEMENT'
      }
    },
    {
      path: '/management/sujet-suggestions',
      name: 'sujetSuggestions',
      component: SujetSuggestionsView,
      meta: { 
        requiresAuth: true,
        requiresRole: 'CHEF_DE_DEPARTEMENT'
      }
    },{
      path: '/management/notes-management',
      name: 'notesManagement',
      component: NotesManagementView,
      meta: { 
        requiresAuth: true,
        requiresRole: 'CHEF_DE_DEPARTEMENT'
      }
    },
    // Redirect root to dashboard
    {
      path: '/',
      redirect: '/dashboard'
    },
    
    // Catch-all route for 404
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: NotFoundView
    }
  ]
})

// Navigation guard to check authentication and role-based access
router.beforeEach((to, from, next) => {
  // Check if the user is authenticated
  const isAuthenticated = AuthService.isAuthenticated();
  const currentUser = AuthService.getCurrentUser();
  
  // Handle routes that require authentication
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      // Redirect to login if not authenticated
      next({ name: 'login', query: { redirect: to.fullPath } });
      return;
    }
    
    // Check for role requirements
    if (to.meta.requiresRole && currentUser?.role !== to.meta.requiresRole) {
      // User doesn't have the required role, redirect to dashboard
      next({ name: 'dashboard' });
      return;
    }
    
    // User is authenticated and has the required role (if any)
    next();
  } 
  // Handle routes for guests only
  else if (to.matched.some(record => record.meta.guest) && isAuthenticated) {
    // Redirect authenticated users to dashboard
    next({ name: 'dashboard' });
  } 
  // Allow access to public routes
  else {
    next();
  }
})

export default router