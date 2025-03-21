<template>
    <div class="supervisor-dashboard">
      <h3>Supervisor Dashboard</h3>
      
      <div class="grid">
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-users"></i>
            </div>
            <div class="card-content">
              <h4>My Students</h4>
              <p>View and manage the students you are supervising.</p>
              <Button label="View Students" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/supervisor/students')" />
            </div>
          </div>
        </div>
        
        <div class="col-12 md:col-6 lg:col-4">
          <div class="dashboard-card">
            <div class="card-icon">
              <i class="pi pi-check-square"></i>
            </div>
            <div class="card-content">
              <h4>Review Submissions</h4>
              <p>Review student submissions and provide feedback.</p>
              <Button label="View Submissions" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/supervisor/reviews')" />
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
              <p>View important dates and schedule meetings.</p>
              <Button label="View Schedule" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/supervisor/schedule')" />
            </div>
          </div>
        </div>
      </div>
      
      <div class="recent-submissions">
        <div class="d-flex align-items-center justify-content-between">
          <h3>Recent Submissions</h3>
          <Button label="View All" icon="pi pi-list" class="p-button-text" @click="navigateTo('/supervisor/reviews')" />
        </div>
        
        <p v-if="!submissions.length" class="no-data">No recent submissions to review.</p>
        <div v-else class="submission-list">
          <div v-for="(submission, index) in submissions" :key="index" class="submission-item">
            <div class="submission-header">
              <div class="student-info">
                <span class="student-name">{{ submission.studentName }}</span>
                <span class="submission-type">{{ submission.type }}</span>
              </div>
              <div class="submission-date">{{ formatDate(submission.date) }}</div>
            </div>
            <div class="submission-title">{{ submission.title }}</div>
            <div class="submission-footer">
              <Tag :severity="getStatusSeverity(submission.status)">{{ submission.status }}</Tag>
              <Button label="Review" icon="pi pi-eye" class="p-button-sm" @click="navigateTo(`/supervisor/reviews/${submission.id}`)" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import Tag from 'primevue/tag';
  
  const router = useRouter();
  
  // Mock data for submissions (in a real app, this would come from an API)
  const submissions = ref([
    {
      id: 1,
      studentName: 'Mohammed Alami',
      type: 'Report',
      title: 'Initial Project Proposal',
      date: new Date(2025, 2, 28), // March 28, 2025
      status: 'Pending'
    },
    {
      id: 2,
      studentName: 'Sara Benjelloun',
      type: 'Document',
      title: 'Literature Review',
      date: new Date(2025, 2, 25), // March 25, 2025
      status: 'Reviewed'
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
  
  // Get severity for status tag
  const getStatusSeverity = (status) => {
    switch (status) {
      case 'Pending':
        return 'warning';
      case 'Reviewed':
        return 'success';
      case 'Rejected':
        return 'danger';
      default:
        return 'info';
    }
  };
  </script>
  
  <style scoped>
  .supervisor-dashboard h3 {
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
  
  .recent-submissions {
    margin-top: 2rem;
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  }
  
  .d-flex {
    display: flex;
  }
  
  .align-items-center {
    align-items: center;
  }
  
  .justify-content-between {
    justify-content: space-between;
  }
  
  .recent-submissions h3 {
    margin-bottom: 1rem;
  }
  
  .no-data {
    color: var(--text-color-secondary);
    font-style: italic;
  }
  
  .submission-list {
    display: grid;
    gap: 1rem;
  }
  
  .submission-item {
    padding: 1rem;
    border: 1px solid var(--surface-border);
    border-radius: 4px;
    background-color: var(--surface-section);
  }
  
  .submission-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
  }
  
  .student-info {
    display: flex;
    align-items: center;
  }
  
  .student-name {
    font-weight: 600;
    margin-right: 0.5rem;
  }
  
  .submission-type {
    font-size: 0.85rem;
    color: var(--text-color-secondary);
    background-color: var(--surface-ground);
    padding: 0.2rem 0.5rem;
    border-radius: 4px;
  }
  
  .submission-date {
    font-size: 0.85rem;
    color: var(--text-color-secondary);
  }
  
  .submission-title {
    font-weight: 500;
    margin-bottom: 0.75rem;
  }
  
  .submission-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.5rem;
  }
  </style>