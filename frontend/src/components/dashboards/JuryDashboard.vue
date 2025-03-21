<template>
    <div class="jury-dashboard">
      <h3>Jury Dashboard</h3>
      
      <div class="grid">
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-star"></i>
            </div>
            <div class="card-content">
              <h4>Evaluations</h4>
              <p>View and evaluate assigned projects.</p>
              <Button label="View Evaluations" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/jury/evaluations')" />
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
              <p>View upcoming presentations and defenses.</p>
              <Button label="View Schedule" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/jury/schedule')" />
            </div>
          </div>
        </div>
        
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-file-pdf"></i>
            </div>
            <div class="card-content">
              <h4>Reports</h4>
              <p>Access project reports and documentation.</p>
              <Button label="View Reports" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/jury/reports')" />
            </div>
          </div>
        </div>
      </div>
      
      <div class="upcoming-presentations">
        <div class="section-header">
          <h3>Upcoming Presentations</h3>
          <Button label="View All" icon="pi pi-calendar" class="p-button-text" @click="navigateTo('/jury/schedule')" />
        </div>
        
        <p v-if="!presentations.length" class="no-data">No upcoming presentations scheduled.</p>
        <div v-else class="presentations-list">
          <div v-for="(presentation, index) in presentations" :key="index" class="presentation-item">
            <div class="presentation-date">
              <div class="date-day">{{ formatDay(presentation.date) }}</div>
              <div class="date-month">{{ formatMonth(presentation.date) }}</div>
              <div class="date-time">{{ formatTime(presentation.date) }}</div>
            </div>
            <div class="presentation-details">
              <h4 class="presentation-title">{{ presentation.title }}</h4>
              <div class="presentation-student">{{ presentation.studentName }}</div>
              <div class="presentation-info">
                <span class="presentation-location">
                  <i class="pi pi-map-marker"></i> {{ presentation.location }}
                </span>
                <span class="presentation-duration">
                  <i class="pi pi-clock"></i> {{ presentation.duration }} minutes
                </span>
              </div>
            </div>
            <div class="presentation-actions">
              <Button icon="pi pi-eye" class="p-button-rounded p-button-text" @click="navigateTo(`/jury/presentations/${presentation.id}`)" />
            </div>
          </div>
        </div>
      </div>
      
      <div class="evaluation-summary">
        <h3>Evaluation Summary</h3>
        
        <div class="grid">
          <div class="col-12 md:col-4">
            <div class="summary-card">
              <div class="summary-value">{{ stats.totalAssigned }}</div>
              <div class="summary-label">Projects Assigned</div>
            </div>
          </div>
          
          <div class="col-12 md:col-4">
            <div class="summary-card">
              <div class="summary-value">{{ stats.completed }}</div>
              <div class="summary-label">Evaluations Completed</div>
            </div>
          </div>
          
          <div class="col-12 md:col-4">
            <div class="summary-card">
              <div class="summary-value">{{ stats.pending }}</div>
              <div class="summary-label">Pending Evaluations</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  // Mock data for presentations
  const presentations = ref([
    {
      id: 1,
      title: "Machine Learning for Predictive Maintenance",
      studentName: "Omar Bensouda",
      date: new Date(2025, 3, 15, 10, 0), // April 15, 2025, 10:00 AM
      location: "Room A-203",
      duration: 30
    },
    {
      id: 2,
      title: "Blockchain Applications in Supply Chain",
      studentName: "Leila Kabouri",
      date: new Date(2025, 3, 16, 14, 30), // April 16, 2025, 2:30 PM
      location: "Room B-101",
      duration: 45
    }
  ]);
  
  // Mock data for evaluation statistics
  const stats = ref({
    totalAssigned: 12,
    completed: 5,
    pending: 7
  });
  
  // Navigate to a specific route
  const navigateTo = (path) => {
    router.push(path);
  };
  
  // Format date functions
  const formatDay = (date) => {
    return new Intl.DateTimeFormat('en-US', { day: 'numeric' }).format(date);
  };
  
  const formatMonth = (date) => {
    return new Intl.DateTimeFormat('en-US', { month: 'short' }).format(date);
  };
  
  const formatTime = (date) => {
    return new Intl.DateTimeFormat('en-US', { hour: 'numeric', minute: 'numeric', hour12: true }).format(date);
  };
  </script>
  
  <style scoped>
  .jury-dashboard h3 {
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
  
  .upcoming-presentations, .evaluation-summary {
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 6px rgba(0,0,0,0.05);
    margin-bottom: 2rem;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
  }
  
  .section-header h3 {
    margin-bottom: 0;
  }
  
  .no-data {
    color: var(--text-color-secondary);
    font-style: italic;
    padding: 1rem 0;
  }
  
  .presentations-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .presentation-item {
    display: flex;
    padding: 1rem;
    border-radius: 8px;
    background-color: var(--surface-section);
    border: 1px solid var(--surface-border);
  }
  
  .presentation-date {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-width: 80px;
    margin-right: 1.5rem;
    text-align: center;
  }
  
  .date-day {
    font-size: 1.75rem;
    font-weight: 700;
    line-height: 1;
    color: var(--primary-color);
  }
  
  .date-month {
    font-size: 0.9rem;
    font-weight: 600;
    text-transform: uppercase;
    margin-bottom: 0.25rem;
  }
  
  .date-time {
    font-size: 0.8rem;
    color: var(--text-color-secondary);
    white-space: nowrap;
  }
  
  .presentation-details {
    flex: 1;
  }
  
  .presentation-title {
    margin-top: 0;
    margin-bottom: 0.25rem;
    font-size: 1.1rem;
  }
  
  .presentation-student {
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    color: var(--text-color-secondary);
  }
  
  .presentation-info {
    display: flex;
    gap: 1rem;
    font-size: 0.85rem;
  }
  
  .presentation-location, .presentation-duration {
    display: flex;
    align-items: center;
    color: var(--text-color-secondary);
  }
  
  .presentation-location i, .presentation-duration i {
    margin-right: 0.35rem;
  }
  
  .presentation-actions {
    display: flex;
    align-items: center;
  }
  
  .summary-card {
    background-color: var(--surface-section);
    border-radius: 8px;
    padding: 1.5rem;
    text-align: center;
    border: 1px solid var(--surface-border);
    height: 100%;
  }
  
  .summary-value {
    font-size: 2rem;
    font-weight: 700;
    color: var(--primary-color);
    margin-bottom: 0.5rem;
  }
  
  .summary-label {
    font-size: 0.9rem;
    color: var(--text-color-secondary);
  }
  </style>