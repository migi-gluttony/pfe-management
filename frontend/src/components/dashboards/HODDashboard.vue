<template>
  <div class="hod-dashboard">
    <h3>Department Head Dashboard</h3>
    
    <div class="grid">
      <div class="col-12 md:col-6 lg:col-3">
        <div class="dashboard-card">
          <div class="card-icon">
            <i class="pi pi-users"></i>
          </div>
          <div class="card-content">
            <h4>Manage Users</h4>
            <p>Add, edit, or remove users from the system.</p>
            <Button label="Manage Users" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/management/comptes')" />
          </div>
        </div>
      </div>
      
      <div class="col-12 md:col-6 lg:col-3">
        <div class="dashboard-card">
          <div class="card-icon">
            <i class="pi pi-sitemap"></i>
          </div>
          <div class="card-content">
            <h4>Manage Binômes</h4>
            <p>Assign and manage student pairings.</p>
            <Button label="Manage Binômes" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/management/binomes')" />
          </div>
        </div>
      </div>
      
      <div class="col-12 md:col-6 lg:col-3">
        <div class="dashboard-card">
          <div class="card-icon">
            <i class="pi pi-book"></i>
          </div>
          <div class="card-content">
            <h4>Manage Subjects</h4>
            <p>Create and assign PFE subjects.</p>
            <Button label="Manage Subjects" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/management/sujets')" />
          </div>
        </div>
      </div>
      
      <div class="col-12 md:col-6 lg:col-3">
        <div class="dashboard-card">
          <div class="card-icon">
            <i class="pi pi-calendar"></i>
          </div>
          <div class="card-content">
            <h4>Manage Defenses</h4>
            <p>Schedule and organize PFE defense sessions.</p>
            <Button label="Manage Defenses" icon="pi pi-arrow-right" class="p-button-sm" @click="navigateTo('/management/soutenances')" />
          </div>
        </div>
      </div>
    </div>
    
    <div class="grid">
      <div class="col-12 lg:col-8">
        <div class="department-stats">
          <h3>Department Overview</h3>
          
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-label">Students</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-value">{{ stats.totalSupervisors }}</div>
              <div class="stat-label">Supervisors</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-value">{{ stats.totalProjects }}</div>
              <div class="stat-label">Projects</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-value">{{ stats.completionRate }}%</div>
              <div class="stat-label">Completion Rate</div>
            </div>
          </div>
          
          <div class="project-status-chart">
            <h4>Project Status Distribution</h4>
            <div class="chart-container">
              <!-- Placeholder for chart -->
              <div class="placeholder-chart">
                <div class="chart-bar" style="height: 60%;">
                  <span class="bar-label">In Progress</span>
                  <span class="bar-value">60%</span>
                </div>
                <div class="chart-bar" style="height: 25%;">
                  <span class="bar-label">Completed</span>
                  <span class="bar-value">25%</span>
                </div>
                <div class="chart-bar" style="height: 10%;">
                  <span class="bar-label">Not Started</span>
                  <span class="bar-value">10%</span>
                </div>
                <div class="chart-bar" style="height: 5%;">
                  <span class="bar-label">Delayed</span>
                  <span class="bar-value">5%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-12 lg:col-4">
        <div class="recent-activities">
          <h3>Recent Activities</h3>
          
          <div v-if="!activities.length" class="no-data">No recent activities.</div>
          <div v-else class="activity-list">
            <div v-for="(activity, index) in activities" :key="index" class="activity-item">
              <div class="activity-icon">
                <i :class="activity.icon"></i>
              </div>
              <div class="activity-content">
                <div class="activity-description">{{ activity.description }}</div>
                <div class="activity-time">{{ formatDate(activity.timestamp) }}</div>
              </div>
            </div>
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

// Mock data for department statistics
const stats = ref({
  totalStudents: 120,
  totalSupervisors: 15,
  totalProjects: 45,
  completionRate: 68
});

// Mock data for recent activities
const activities = ref([
  {
    description: 'Ahmed El Mansouri added a new user',
    timestamp: new Date(2025, 2, 29, 14, 30), // March 29, 2025, 2:30 PM
    icon: 'pi pi-user-plus'
  },
  {
    description: 'New project proposal submitted by Sara Benjelloun',
    timestamp: new Date(2025, 2, 29, 11, 15), // March 29, 2025, 11:15 AM
    icon: 'pi pi-file'
  },
  {
    description: 'Supervisor meeting schedule updated',
    timestamp: new Date(2025, 2, 28, 16, 45), // March 28, 2025, 4:45 PM
    icon: 'pi pi-calendar'
  },
  {
    description: 'End of semester report generated',
    timestamp: new Date(2025, 2, 28, 10, 0), // March 28, 2025, 10:00 AM
    icon: 'pi pi-chart-bar'
  }
]);

// Navigate to a specific route
const navigateTo = (path) => {
  router.push(path);
};

// Format date for display
const formatDate = (date) => {
  const now = new Date();
  const diffInDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));
  
  if (diffInDays === 0) {
    return 'Today, ' + new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: 'numeric'
    }).format(date);
  } else if (diffInDays === 1) {
    return 'Yesterday, ' + new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: 'numeric'
    }).format(date);
  } else {
    return new Intl.DateTimeFormat('en-US', {
      day: 'numeric',
      month: 'short',
      hour: 'numeric',
      minute: 'numeric'
    }).format(date);
  }
};
</script>

<style scoped>
.hod-dashboard h3 {
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

.department-stats, .recent-activities {
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  height: 100%;
}

.stats-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  flex: 1;
  min-width: 120px;
  background-color: var(--surface-section);
  border-radius: 6px;
  padding: 1rem;
  text-align: center;
  border: 1px solid var(--surface-border);
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-color-secondary);
}

.project-status-chart {
  margin-top: 2rem;
}

.project-status-chart h4 {
  margin-bottom: 1rem;
}

.chart-container {
  height: 250px;
  padding: 1rem 0;
}

.placeholder-chart {
  display: flex;
  height: 100%;
  align-items: flex-end;
  gap: 1.5rem;
  padding: 0 1rem;
}

.chart-bar {
  flex: 1;
  background-color: var(--primary-color);
  border-radius: 6px 6px 0 0;
  position: relative;
  min-width: 60px;
  transition: height 0.3s ease;
}

.chart-bar:nth-child(2) {
  background-color: var(--green-500);
}

.chart-bar:nth-child(3) {
  background-color: var(--blue-500);
}

.chart-bar:nth-child(4) {
  background-color: var(--orange-500);
}

.bar-label {
  position: absolute;
  bottom: -25px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 0.75rem;
  color: var(--text-color-secondary);
  white-space: nowrap;
}

.bar-value {
  position: absolute;
  top: -25px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 0.875rem;
  font-weight: 600;
}

.no-data {
  color: var(--text-color-secondary);
  font-style: italic;
  padding: 1rem 0;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  padding: 0.75rem;
  border-radius: 6px;
  background-color: var(--surface-section);
  border: 1px solid var(--surface-border);
}

.activity-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  background-color: var(--primary-color-lighter, rgba(99, 102, 241, 0.1));
  color: var(--primary-color);
  border-radius: 50%;
  margin-right: 0.75rem;
}

.activity-content {
  flex: 1;
}

.activity-description {
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.activity-time {
  font-size: 0.8rem;
  color: var(--text-color-secondary);
}
</style>