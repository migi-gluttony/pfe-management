<template>
    <div class="container">
      <header class="dashboard-header">
        <div class="header-content">
          <div class="welcome-section">
            <h1>Welcome, {{ user.firstName || 'User' }}</h1>
            <p>{{ greeting }}</p>
          </div>
          <div class="actions">
            <ThemeToggle />
            <Button 
              icon="pi pi-sign-out" 
              label="Logout" 
              class="p-button-rounded p-button-outlined ml-2" 
              @click="logout"
            />
          </div>
        </div>
      </header>
  
      <!-- Student Dashboard -->
      <div v-if="userRole === 'STUDENT'" class="dashboard-content">
        <div class="card-grid">
          <!-- Projects Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-folder-open mr-2"></i>
              My Projects
            </h2>
            <div v-if="projects.length === 0" class="empty-state">
              <i class="pi pi-folder empty-icon"></i>
              <p>You don't have any active projects yet.</p>
              <Button 
                label="Start New Project" 
                icon="pi pi-plus" 
                class="p-button-outlined mt-3"
                @click="openNewProjectDialog"
              />
            </div>
            <div v-else class="projects-list">
              <div v-for="project in projects" :key="project.id" class="project-item">
                <div class="project-info">
                  <h3>{{ project.title }}</h3>
                  <p class="project-description">{{ project.description }}</p>
                  <div class="project-meta">
                    <span class="status-badge" :class="getStatusClass(project.status)">
                      {{ project.status }}
                    </span>
                    <span class="project-date">
                      <i class="pi pi-calendar mr-1"></i>
                      Due: {{ formatDate(project.dueDate) }}
                    </span>
                  </div>
                </div>
                <div class="project-actions">
                  <Button 
                    icon="pi pi-eye" 
                    class="p-button-rounded p-button-text" 
                    @click="viewProject(project)"
                    v-tooltip="'View Details'"
                  />
                </div>
              </div>
              <Button 
                label="Start New Project" 
                icon="pi pi-plus" 
                class="p-button-outlined mt-3"
                @click="openNewProjectDialog"
              />
            </div>
          </div>
  
          <!-- Supervisor Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-user mr-2"></i>
              My Supervisor
            </h2>
            <div v-if="!supervisor" class="empty-state">
              <i class="pi pi-user empty-icon"></i>
              <p>No supervisor assigned yet.</p>
            </div>
            <div v-else class="supervisor-info">
              <div class="supervisor-avatar">
                <i class="pi pi-user"></i>
              </div>
              <div class="supervisor-details">
                <h3>{{ supervisor.title }} {{ supervisor.firstName }} {{ supervisor.lastName }}</h3>
                <p>{{ supervisor.department }}</p>
                <p>{{ supervisor.email }}</p>
                <Button 
                  icon="pi pi-envelope" 
                  label="Contact" 
                  class="p-button-sm mt-2" 
                  @click="contactSupervisor"
                />
              </div>
            </div>
          </div>
  
          <!-- Upcoming Tasks Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-check-square mr-2"></i>
              Upcoming Tasks
            </h2>
            <div v-if="tasks.length === 0" class="empty-state">
              <i class="pi pi-check-square empty-icon"></i>
              <p>No upcoming tasks.</p>
            </div>
            <div v-else class="tasks-list">
              <div v-for="task in tasks" :key="task.id" class="task-item">
                <div class="task-checkbox">
                  <Checkbox v-model="task.completed" :binary="true" @change="updateTask(task)" />
                </div>
                <div class="task-content">
                  <span class="task-title" :class="{ 'completed': task.completed }">
                    {{ task.title }}
                  </span>
                  <span class="task-due-date">
                    Due: {{ formatDate(task.dueDate) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  
      <!-- Professor Dashboard -->
      <div v-else-if="userRole === 'PROFESSOR'" class="dashboard-content">
        <div class="card-grid">
          <!-- Supervised Projects Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-folder-open mr-2"></i>
              Supervised Projects
            </h2>
            <div v-if="supervisedProjects.length === 0" class="empty-state">
              <i class="pi pi-folder empty-icon"></i>
              <p>You are not supervising any projects yet.</p>
            </div>
            <div v-else class="projects-list">
              <div v-for="project in supervisedProjects" :key="project.id" class="project-item">
                <div class="project-info">
                  <h3>{{ project.title }}</h3>
                  <p class="project-description">{{ project.description }}</p>
                  <div class="project-meta">
                    <span class="project-student">
                      <i class="pi pi-user mr-1"></i>
                      {{ project.studentName }}
                    </span>
                    <span class="status-badge" :class="getStatusClass(project.status)">
                      {{ project.status }}
                    </span>
                    <span class="project-date">
                      <i class="pi pi-calendar mr-1"></i>
                      Due: {{ formatDate(project.dueDate) }}
                    </span>
                  </div>
                </div>
                <div class="project-actions">
                  <Button 
                    icon="pi pi-eye" 
                    class="p-button-rounded p-button-text" 
                    @click="viewProject(project)"
                    v-tooltip="'View Details'"
                  />
                </div>
              </div>
            </div>
          </div>
  
          <!-- Students Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-users mr-2"></i>
              My Students
            </h2>
            <div v-if="students.length === 0" class="empty-state">
              <i class="pi pi-users empty-icon"></i>
              <p>You don't have any assigned students yet.</p>
            </div>
            <div v-else class="students-list">
              <div v-for="student in students" :key="student.id" class="student-item">
                <div class="student-info">
                  <h3>{{ student.firstName }} {{ student.lastName }}</h3>
                  <p>{{ student.email }}</p>
                  <p>{{ student.department }}</p>
                </div>
                <div class="student-actions">
                  <Button 
                    icon="pi pi-envelope" 
                    class="p-button-rounded p-button-text" 
                    @click="contactStudent(student)"
                    v-tooltip="'Contact Student'"
                  />
                  <Button 
                    icon="pi pi-list" 
                    class="p-button-rounded p-button-text" 
                    @click="viewStudentProjects(student)"
                    v-tooltip="'View Projects'"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  
      <!-- Admin Dashboard -->
      <div v-else-if="userRole === 'ADMIN'" class="dashboard-content">
        <div class="card-grid">
          <!-- Users Management Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-users mr-2"></i>
              Users Management
            </h2>
            <div class="admin-stats">
              <div class="stat-item">
                <span class="stat-value">{{ stats.totalStudents }}</span>
                <span class="stat-label">Students</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.totalProfessors }}</span>
                <span class="stat-label">Professors</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.totalProjects }}</span>
                <span class="stat-label">Projects</span>
              </div>
            </div>
            <div class="admin-actions">
              <Button 
                label="Manage Users" 
                icon="pi pi-users" 
                class="p-button-outlined mt-3"
                @click="navigateTo('/admin/users')"
              />
            </div>
          </div>
  
          <!-- Projects Management Card -->
          <div class="component-card">
            <h2>
              <i class="pi pi-folder-open mr-2"></i>
              Projects Management
            </h2>
            <div class="projects-stats">
              <div class="progress-item">
                <div class="progress-label">
                  <span>In Progress</span>
                  <span>{{ stats.inProgressProjects }}</span>
                </div>
                <ProgressBar :value="stats.inProgressPercentage" />
              </div>
              <div class="progress-item">
                <div class="progress-label">
                  <span>Completed</span>
                  <span>{{ stats.completedProjects }}</span>
                </div>
                <ProgressBar :value="stats.completedPercentage" />
              </div>
            </div>
            <div class="admin-actions">
              <Button 
                label="Manage Projects" 
                icon="pi pi-folder-open" 
                class="p-button-outlined mt-3"
                @click="navigateTo('/admin/projects')"
              />
            </div>
          </div>
        </div>
      </div>
  
      <Toast />
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { useToast } from 'primevue/usetoast';
  import ThemeToggle from '../components/ThemeToggle.vue';
  import AuthService from '../services/AuthService';
  import ProgressBar from 'primevue/progressbar';
  
  // Router and Toast
  const router = useRouter();
  const toast = useToast();
  
  // User data
  const user = ref({});
  const userRole = ref('');
  
  // Dashboard data
  const projects = ref([]);
  const supervisor = ref(null);
  const tasks = ref([]);
  const supervisedProjects = ref([]);
  const students = ref([]);
  const stats = reactive({
    totalStudents: 0,
    totalProfessors: 0,
    totalProjects: 0,
    inProgressProjects: 0,
    completedProjects: 0,
    inProgressPercentage: 0,
    completedPercentage: 0
  });
  
  // Time-based greeting
  const greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good morning";
    if (hour < 18) return "Good afternoon";
    return "Good evening";
  });
  
  // Initialize the dashboard
  onMounted(async () => {
    try {
      // Check authentication
      if (!AuthService.isAuthenticated()) {
        router.push('/login');
        return;
      }
  
      // Get user data
      const userData = AuthService.getCurrentUser();
      if (userData) {
        user.value = userData;
        userRole.value = userData.role;
        
        // Load appropriate dashboard data based on role
        loadDashboardData();
      } else {
        // Fetch from API if not in local storage
        try {
          const fetchedUser = await AuthService.fetchUserProfile();
          user.value = fetchedUser;
          userRole.value = fetchedUser.role;
          loadDashboardData();
        } catch (error) {
          console.error('Failed to fetch user profile:', error);
          // Handle error or redirect
          toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load your profile',
            life: 3000
          });
        }
      }
    } catch (error) {
      console.error('Dashboard initialization error:', error);
    }
  });
  
  // Load dashboard data based on user role
  const loadDashboardData = async () => {
    // This function would make API calls to fetch the appropriate data
    // For now, we'll use mock data
    
    if (userRole.value === 'STUDENT') {
      // Load student projects
      projects.value = [
        {
          id: 1,
          title: 'Implementation of a Secure Authentication System',
          description: 'Developing a multi-factor authentication system with biometric verification capabilities.',
          status: 'IN_PROGRESS',
          dueDate: '2025-06-15'
        }
      ];
      
      // Load supervisor info
      supervisor.value = {
        id: 101,
        title: 'Dr.',
        firstName: 'Ahmed',
        lastName: 'Bennani',
        department: 'Computer Science Department',
        email: 'ahmed.bennani@university.ma'
      };
      
      // Load tasks
      tasks.value = [
        {
          id: 201,
          title: 'Submit project proposal',
          dueDate: '2025-04-10',
          completed: true
        },
        {
          id: 202,
          title: 'Complete literature review',
          dueDate: '2025-04-30',
          completed: false
        },
        {
          id: 203,
          title: 'Develop prototype',
          dueDate: '2025-05-15',
          completed: false
        }
      ];
    } 
    else if (userRole.value === 'PROFESSOR') {
      // Load supervised projects
      supervisedProjects.value = [
        {
          id: 1,
          title: 'Implementation of a Secure Authentication System',
          description: 'Developing a multi-factor authentication system with biometric verification capabilities.',
          status: 'IN_PROGRESS',
          dueDate: '2025-06-15',
          studentName: 'Mohammed Alami'
        },
        {
          id: 2,
          title: 'Machine Learning for Predictive Maintenance',
          description: 'Using ML algorithms to predict equipment failures before they occur.',
          status: 'PLANNING',
          dueDate: '2025-07-30',
          studentName: 'Fatima Zahra'
        }
      ];
      
      // Load students
      students.value = [
        {
          id: 301,
          firstName: 'Mohammed',
          lastName: 'Alami',
          email: 'mohammed.alami@student.university.ma',
          department: 'Computer Science'
        },
        {
          id: 302,
          firstName: 'Fatima',
          lastName: 'Zahra',
          email: 'fatima.zahra@student.university.ma',
          department: 'Computer Science'
        }
      ];
    }
    else if (userRole.value === 'ADMIN') {
      // Load admin stats
      stats.totalStudents = 150;
      stats.totalProfessors = 35;
      stats.totalProjects = 112;
      stats.inProgressProjects = 87;
      stats.completedProjects = 25;
      stats.inProgressPercentage = Math.round((stats.inProgressProjects / stats.totalProjects) * 100);
      stats.completedPercentage = Math.round((stats.completedProjects / stats.totalProjects) * 100);
    }
  };
  
  // Helper functions
  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };
  
  const getStatusClass = (status) => {
    switch (status) {
      case 'PLANNING': return 'status-planning';
      case 'IN_PROGRESS': return 'status-in-progress';
      case 'REVIEW': return 'status-review';
      case 'APPROVED': return 'status-approved';
      case 'COMPLETED': return 'status-completed';
      default: return '';
    }
  };
  
  // Action functions
  const logout = () => {
    AuthService.logout();
    router.push('/login');
    toast.add({
      severity: 'info',
      summary: 'Logged Out',
      detail: 'You have been successfully logged out',
      life: 3000
    });
  };
  
  const openNewProjectDialog = () => {
    // This would open a dialog to create a new project
    toast.add({
      severity: 'info',
      summary: 'New Project',
      detail: 'New project creation dialog would open here',
      life: 3000
    });
  };
  
  const viewProject = (project) => {
    // This would navigate to the project details page
    toast.add({
      severity: 'info',
      summary: 'View Project',
      detail: `Viewing project: ${project.title}`,
      life: 3000
    });
  };
  
  const contactSupervisor = () => {
    // This would open a dialog to contact the supervisor
    toast.add({
      severity: 'info',
      summary: 'Contact Supervisor',
      detail: 'Contact supervisor dialog would open here',
      life: 3000
    });
  };
  
  const updateTask = (task) => {
    // This would update the task completion status
    toast.add({
      severity: 'success',
      summary: 'Task Updated',
      detail: `Task "${task.title}" ${task.completed ? 'completed' : 'reopened'}`,
      life: 3000
    });
  };
  
  const contactStudent = (student) => {
    // This would open a dialog to contact the student
    toast.add({
      severity: 'info',
      summary: 'Contact Student',
      detail: `Contacting ${student.firstName} ${student.lastName}`,
      life: 3000
    });
  };
  
  const viewStudentProjects = (student) => {
    // This would navigate to the student's projects page
    toast.add({
      severity: 'info',
      summary: 'Student Projects',
      detail: `Viewing projects for ${student.firstName} ${student.lastName}`,
      life: 3000
    });
  };
  
  const navigateTo = (route) => {
    // This would navigate to the specified route
    toast.add({
      severity: 'info',
      summary: 'Navigation',
      detail: `Navigating to ${route}`,
      life: 3000
    });
  };
  </script>
  
  <style scoped>
  .dashboard-header {
    background-color: var(--primary-color);
    color: white;
    border-radius: 0.5rem;
    margin-bottom: 1.5rem;
    padding: 1.5rem;
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .welcome-section h1 {
    margin: 0;
    font-size: 1.5rem;
  }
  
  .welcome-section p {
    margin: 0.5rem 0 0;
    opacity: 0.9;
  }
  
  .actions {
    display: flex;
    align-items: center;
  }
  
  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .component-card {
    height: 100%;
    padding: 1.5rem;
  }
  
  .component-card h2 {
    display: flex;
    align-items: center;
    margin-top: 0;
    margin-bottom: 1.5rem;
    font-size: 1.25rem;
    border-bottom: 1px solid var(--border-color);
    padding-bottom: 0.75rem;
  }
  
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem 0;
    text-align: center;
  }
  
  .empty-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
    opacity: 0.5;
  }
  
  .project-item, .task-item, .student-item {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 1rem;
    border: 1px solid var(--border-color);
    border-radius: 0.5rem;
    margin-bottom: 1rem;
  }
  
  .project-info, .student-info {
    flex: 1;
  }
  
  .project-info h3, .student-info h3 {
    margin: 0 0 0.5rem;
    font-size: 1.1rem;
  }
  
  .project-description {
    margin: 0 0 0.75rem;
    font-size: 0.9rem;
    opacity: 0.8;
  }
  
  .project-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    font-size: 0.85rem;
  }
  
  .status-badge {
    padding: 0.25rem 0.5rem;
    border-radius: 1rem;
    font-weight: 500;
  }
  
  .status-planning {
    background-color: #e3f2fd;
    color: #1565c0;
  }
  
  .status-in-progress {
    background-color: #fff8e1;
    color: #ff8f00;
  }
  
  .status-review {
    background-color: #e8f5e9;
    color: #2e7d32;
  }
  
  .status-approved {
    background-color: #e8f5e9;
    color: #2e7d32;
  }
  
  .status-completed {
    background-color: #e8f5e9;
    color: #2e7d32;
  }
  
  .project-date, .project-student {
    display: flex;
    align-items: center;
  }
  
  .supervisor-info {
    display: flex;
    align-items: flex-start;
  }
  
  .supervisor-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60px;
    height: 60px;
    background-color: var(--primary-color);
    color: white;
    border-radius: 50%;
    margin-right: 1rem;
    font-size: 1.5rem;
  }
  
  .supervisor-details h3 {
    margin: 0 0 0.5rem;
    font-size: 1.1rem;
  }
  
  .supervisor-details p {
    margin: 0 0 0.25rem;
    font-size: 0.9rem;
  }
  
  .task-item {
    align-items: center;
  }
  
  .task-checkbox {
    margin-right: 1rem;
  }
  
  .task-content {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  .task-title.completed {
    text-decoration: line-through;
    opacity: 0.6;
  }
  
  .task-due-date {
    font-size: 0.85rem;
    opacity: 0.8;
    margin-top: 0.25rem;
  }
  
  .admin-stats {
    display: flex;
    justify-content: space-around;
    margin-bottom: 1.5rem;
  }
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .stat-value {
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
  }
  
  .stat-label {
    font-size: 0.9rem;
    opacity: 0.8;
  }
  
  .progress-item {
    margin-bottom: 1rem;
  }
  
  .progress-label {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    font-size: 0.9rem;
  }
  
  .admin-actions {
    display: flex;
    justify-content: center;
  }
  
  @media (max-width: 768px) {
    .header-content {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .actions {
      margin-top: 1rem;
    }
    
    .project-item, .student-item {
      flex-direction: column;
    }
    
    .project-actions, .student-actions {
      margin-top: 1rem;
    }
    
    .admin-stats {
      flex-direction: column;
      gap: 1rem;
    }
  }
  </style>