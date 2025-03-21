<template>
    <div class="dashboard-layout">
      <header class="dashboard-header">
        <div class="container">
          <div class="header-content">
            <div class="logo">
              <h1>PFE Management</h1>
            </div>
            <div class="user-menu">
              <Button @click="showProfileMenu = !showProfileMenu" class="p-button-text" icon="pi pi-user">
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
  import { ref, onMounted, computed } from 'vue';
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
        }
      );
    } else if (role === 'CHEF_DE_DEPARTEMENT') {
      items.push(
        {
          label: 'Manage Users',
          icon: 'pi pi-users',
          command: () => router.push('/hod/users')
        },
        {
          label: 'Manage Projects',
          icon: 'pi pi-folder',
          command: () => router.push('/hod/projects')
        },
        {
          label: 'Reports',
          icon: 'pi pi-chart-bar',
          command: () => router.push('/hod/reports')
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
        }
      );
    }
    
    return items;
  });
  
  // Watch for profile menu toggle
  watch(showProfileMenu, (newValue) => {
    if (newValue) {
      profileMenu.value.toggle(event);
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
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .logo h1 {
    margin: 0;
    font-size: 1.5rem;
  }
  
  .dashboard-container {
    display: flex;
    flex: 1;
  }
  
  .sidebar {
    width: 250px;
    background-color: var(--surface-card);
    border-right: 1px solid var(--surface-border);
  }
  
  .sidebar-content {
    padding: 1.5rem 1rem;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--surface-border);
  }
  
  .user-icon {
    font-size: 2rem;
    background-color: var(--primary-color);
    color: white;
    border-radius: 50%;
    padding: 0.5rem;
    margin-right: 0.75rem;
  }
  
  .user-details {
    font-size: 0.875rem;
  }
  
  .user-role {
    font-weight: 600;
    margin-bottom: 0.25rem;
  }
  
  .user-email {
    color: var(--text-color-secondary);
    font-size: 0.8rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 180px;
  }
  
  .main-content {
    flex: 1;
    padding: 2rem 0;
    background-color: var(--surface-ground);
  }
  
  @media (max-width: 768px) {
    .dashboard-container {
      flex-direction: column;
    }
    
    .sidebar {
      width: 100%;
      border-right: none;
      border-bottom: 1px solid var(--surface-border);
    }
  }
  </style>