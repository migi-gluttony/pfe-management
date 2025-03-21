<template>
    <div class="student-dashboard">
      <h3>Student Dashboard</h3>
      
      <div class="grid">
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-file"></i>
            </div>
            <div class="card-content">
              <h4>My PFE Project</h4>
              <p>View and manage your final year project details.</p>
              <Button label="View Project" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/student/pfe')" />
            </div>
          </div>
        </div>
        
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-upload"></i>
            </div>
            <div class="card-content">
              <h4>Submissions</h4>
              <p>Upload documents and view submission history.</p>
              <Button label="View Submissions" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/student/submissions')" />
            </div>
          </div>
        </div>
        
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-calendar"></i>
            </div>
            <div class="card-content">
              <h4>Schedule</h4>
              <p>View important dates and upcoming deadlines.</p>
              <Button label="View Schedule" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/student/schedule')" />
            </div>
          </div>
        </div>
      </div>
      
      <div class="upcoming-deadlines">
        <h3>Upcoming Deadlines</h3>
        <p v-if="!deadlines.length" class="no-data">No upcoming deadlines at the moment.</p>
        <ul v-else class="deadline-list">
          <li v-for="(deadline, index) in deadlines" :key="index" class="deadline-item">
            <div class="deadline-date">{{ formatDate(deadline.date) }}</div>
            <div class="deadline-details">
              <h5>{{ deadline.title }}</h5>
              <p>{{ deadline.description }}</p>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  // Mock data for deadlines (in a real app, this would come from an API)
  const deadlines = ref([
    {
      title: 'Project Proposal',
      description: 'Submit your initial project proposal',
      date: new Date(2025, 3, 15) // April 15, 2025
    },
    {
      title: 'Midterm Report',
      description: 'Submit your progress report',
      date: new Date(2025, 5, 10) // June 10, 2025
    }
  ]);
  
  // Navigate to a specific route
  const navigateTo = (path) => {
    router.push(path);
  };
  
  // Format date for display
  const formatDate = (date) => {
    return new Intl.DateTimeFormat('en-US', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    }).format(date);
  };
  </script>
  
  <style scoped>
  .student-dashboard h3 {
    margin-bottom: 1.5rem;
    color: var(--text-color);
  }
  
  .dashboard-card {
    display: flex;
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 6px rgba(0,0,0,0.05);
    margin-bottom: 1.5rem;
    height: 100%;
  }
  
  .card-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 3rem;
    height: 3rem;
    background-color: var(--primary-color);
    color: white;
    border-radius: 50%;
    margin-right: 1rem;
  }
  
  .card-icon i {
    font-size: 1.25rem;
  }
  
  .card-content {
    flex: 1;
  }
  
  .card-content h4 {
    margin-top: 0;
    margin-bottom: 0.5rem;
    color: var(--text-color);
  }
  
  .card-content p {
    margin-bottom: 1rem;
    color: var(--text-color-secondary);
  }
  
  .upcoming-deadlines {
    margin-top: 2rem;
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  }
  
  .no-data {
    color: var(--text-color-secondary);
    font-style: italic;
  }
  
  .deadline-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .deadline-item {
    display: flex;
    padding: 1rem 0;
    border-bottom: 1px solid var(--surface-border);
  }
  
  .deadline-item:last-child {
    border-bottom: none;
  }
  
  .deadline-date {
    min-width: 120px;
    font-weight: 600;
    color: var(--primary-color);
  }
  
  .deadline-details h5 {
    margin-top: 0;
    margin-bottom: 0.25rem;
  }
  
  .deadline-details p {
    margin: 0;
    color: var(--text-color-secondary);
  }
  </style>