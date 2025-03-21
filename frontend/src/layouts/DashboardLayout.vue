<template>
  <div class="dashboard-layout">
    <header class="dashboard-header">
      <div class="container">
        <div class="header-content">
          <div class="logo">
            <h1>PFE Management</h1>
          </div>
          <div class="user-menu">
            <Button @click="toggleProfileMenu" class="p-button-text" icon="pi pi-user">
              {{ user?.email }}
            </Button>
            <Menu ref="profileMenu" :model="profileMenuItems" :popup="true" />
          </div>
        </div>
      </div>
    </header>

    <div class="dashboard-container">
      <aside class="sidebar">
        <div class="sidebar-content">
          <div class="user-info">
            <i class="pi pi-user user-icon"></i>
            <div class="user-details">
              <div class="user-role">{{ formatRole(user?.role) }}</div>
              <div class="user-email">{{ user?.email }}</div>
            </div>
          </div>

          <Menu :model="menuItems" />
        </div>
      </aside>

      <main class="main-content">
        <div class="container">
          <slot></slot>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import AuthService from '../services/AuthService';
import Menu from 'primevue/menu';

const router = useRouter();
const toast = useToast();
const profileMenu = ref(null);
const showProfileMenu = ref(false);

// Get the current user from AuthService
const user = ref(AuthService.getCurrentUser());

// Format role from SNAKE_CASE to Title Case
const formatRole = (role) => {
  if (!role) return 'User';
  
  return role
    .toLowerCase()
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

// Toggle profile menu
const toggleProfileMenu = (event) => {
  profileMenu.value.toggle(event);
};

// Profile menu items
const profileMenuItems = [
  {
    label: 'Profile',
    icon: 'pi pi-user',
    command: () => router.push('/profile')
  },
  {
    label: 'Settings',
    icon: 'pi pi-cog',
    command: () => router.push('/settings')
  },
  {
    separator: true
  },
  {
    label: 'Logout',
    icon: 'pi pi-sign-out',
    command: () => {
      AuthService.logout();
      toast.add({
        severity: 'info',
        summary: 'Logged Out',
        detail: 'You have been logged out successfully',
        life: 3000
      });
      router.push('/login');
    }
  }
];

// Menu items based on user role
const menuItems = computed(() => {
  const role = user.value?.role;
  
  // Default menu items for all users
  const items = [
    {
      label: 'Dashboard',
      icon: 'pi pi-home',
      command: () => router.push('/dashboard')
    }
  ];

  // Role-specific menu items
  if (role === 'ETUDIANT') {
    items.push(
      {
        label: 'My PFE',
        icon: 'pi pi-file',
        command: () => router.push('/student/pfe')
      },
      {
        label: 'Submissions',
        icon: 'pi pi-upload',
        command: () => router.push('/student/submissions')
      },
      {
        label: 'Schedule',
        icon: 'pi pi-calendar',
        command: () => router.push('/student/schedule')
      }
    );
  } else if (role === 'ENCADRANT') {
    items.push(
      {
        label: 'My Students',
        icon: 'pi pi-users',
        command: () => router.push('/supervisor/students')
      },
      {
        label: 'Review Submissions',
        icon: 'pi pi-check-square',
        command: () => router.push('/supervisor/reviews')
      },
      {
        label: 'Schedule',
        icon: 'pi pi-calendar',
        command: () => router.push('/supervisor/schedule')
      }
    );
  } else if (role === 'CHEF_DE_DEPARTEMENT') {
    items.push(
      {
        label: 'Management',
        icon: 'pi pi-cog',
        items: [
          {
            label: 'Manage Users',
            icon: 'pi pi-users',
            command: () => router.push('/management/comptes')
          },
          {
            label: 'Manage Binômes',
            icon: 'pi pi-sitemap',
            command: () => router.push('/management/binomes')
          },
          {
            label: 'Manage Subjects',
            icon: 'pi pi-book',
            command: () => router.push('/management/sujets')
          },
          {
            label: 'Manage Defenses',
            icon: 'pi pi-calendar',
            command: () => router.push('/management/soutenances')
          }
        ]
      },
      {
        label: 'Reports',
        icon: 'pi pi-chart-bar',
        command: () => router.push('/hod/reports')
      },
      {
        label: 'Settings',
        icon: 'pi pi-sliders-h',
        command: () => router.push('/hod/settings')
      }
    );
  } else if (role === 'JURY') {
    items.push(
      {
        label: 'Evaluations',
        icon: 'pi pi-star',
        command: () => router.push('/jury/evaluations')
      },
      {
        label: 'Schedule',
        icon: 'pi pi-calendar',
        command: () => router.push('/jury/schedule')
      },
      {
        label: 'Reports',
        icon: 'pi pi-file-pdf',
        command: () => router.push('/jury/reports')
      }
    );
  }
  
  return items;
});

// Check if user is still authenticated on mount
onMounted(() => {
  if (!AuthService.isAuthenticated()) {
    router.push('/login');
  }
});
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.dashboard-header {
  background-color: var(--primary-color);
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  margin: 0;
  font-size: 1.5rem;
  color: white;
}

.user-menu {
  display: flex;
  align-items: center;
}

.user-menu :deep(.p-button) {
  color: white;
}

.user-menu :deep(.p-button:hover) {
  background: rgba(255, 255, 255, 0.1);
}

.dashboard-container {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 250px;
  background-color: var(--surface-card);
  border-right: 1px solid var(--surface-border);
  overflow-y: auto;
  z-index: 90;
}

.sidebar-content {
  padding: 1.5rem 0;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 0 1rem 1.5rem 1rem;
  margin-bottom: 1rem;
  border-bottom: 1px solid var(--surface-border);
}

.user-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  width: 40px;
  height: 40px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 50%;
  margin-right: 0.75rem;
}

.user-details {
  overflow: hidden;
}

.user-role {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.user-email {
  font-size: 0.8rem;
  color: var(--text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.sidebar :deep(.p-menu) {
  width: 100%;
  border: none;
  background: transparent;
  padding: 0;
}

.sidebar :deep(.p-menu .p-menuitem-link) {
  padding: 0.75rem 1rem;
  border-radius: 0;
}

.sidebar :deep(.p-menu .p-menuitem-link:not(.p-disabled):hover) {
  background-color: var(--surface-hover);
}

.sidebar :deep(.p-menu .p-menuitem-link .p-menuitem-icon) {
  margin-right: 0.75rem;
  color: var(--primary-color);
}

.sidebar :deep(.p-submenu-header) {
  background: transparent;
  color: var(--text-color-secondary);
  font-weight: 600;
  padding: 1rem;
  margin: 0;
}

.main-content {
  flex: 1;
  background-color: var(--surface-ground);
  padding: 2rem 0;
  overflow-y: auto;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Responsive styles */
@media (max-width: 992px) {
  .sidebar {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--surface-border);
    order: 2;
  }
  
  .main-content {
    order: 1;
  }
  
  .sidebar-content {
    padding: 1rem 0;
  }
  
  .user-info {
    padding: 0 1rem 1rem 1rem;
    margin-bottom: 0.5rem;
  }
}
</style>