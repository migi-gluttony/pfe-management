<template>
  <div class="dashboard-container">
    <Toast />
    
    <!-- Loading state -->
    <div v-if="loading" class="loading-indicator">
      <ProgressSpinner />
      <p>Chargement du tableau de bord...</p>
    </div>
    
    <!-- Error state -->
    <div v-else-if="error" class="error-container">
      <i class="pi pi-exclamation-circle"></i>
      <h3>Erreur de chargement</h3>
      <p>{{ error }}</p>
      <Button label="Réessayer" @click="loadDashboardData" />
    </div>
    
    <!-- Dashboard content -->
    <div v-else class="dashboard-content">
      <!-- User Info Header -->
      <div class="welcome-card">
        <div class="welcome-info">
          <h1>Bienvenue, Chef de Département</h1>
          <p class="welcome-subtitle">Année académique {{ currentYear }}</p>
        </div>
        <div class="user-avatar">
          <div class="avatar-circle">
            <span>CD</span>
          </div>
        </div>
      </div>
      
      <!-- Dashboard Stats Cards -->
      <div class="metrics-grid">
        <!-- First row -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Gestion des Comptes</div>
          </div>
          <div class="metric-value">{{ stats.totalUsers }} comptes</div>
          <div class="metric-counts">
            <div class="count-item">
              <span class="count-label">Étudiants:</span>
              <span class="count-value">{{ stats.totalStudents }}</span>
            </div>
            <div class="count-item">
              <span class="count-label">Encadrants:</span>
              <span class="count-value">{{ stats.totalSupervisors }}</span>
            </div>
            <div class="count-item">
              <span class="count-label">Jurys:</span>
              <span class="count-value">{{ stats.totalJuries }}</span>
            </div>
          </div>
          <div class="metric-footer">
            <Button icon="pi pi-arrow-right" class="p-button-rounded p-button-text" @click="navigateTo('/management/comptes')" />
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Gestion des Binômes</div>
          </div>
          <div class="metric-value">{{ stats.totalBinomes }} binômes</div>
          <div class="metric-info">
            <ProgressBar :value="calculateCompletionRate(stats.binomesWithSoutenance, stats.totalBinomes)" class="binome-progress" />
            <div class="progress-info">
              <span>{{ stats.binomesWithSoutenance }} binômes avec soutenance planifiée</span>
            </div>
          </div>
          <div class="metric-footer">
            <Button icon="pi pi-arrow-right" class="p-button-rounded p-button-text" @click="navigateTo('/management/binomes')" />
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Gestion des Sujets</div>
          </div>
          <div class="metric-value">{{ stats.totalSujets }} sujets</div>
          <div class="metric-info">
            <div v-if="stats.pendingSuggestions > 0" class="suggestion-alert">
              <i class="pi pi-exclamation-circle"></i>
              <span>{{ stats.pendingSuggestions }} suggestions en attente</span>
            </div>
          </div>
          <div class="metric-footer">
            <Button icon="pi pi-arrow-right" class="p-button-rounded p-button-text" @click="navigateTo('/management/sujets')" />
          </div>
        </div>
        
        <!-- Second row -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Soutenances</div>
          </div>
          <div class="metric-value">{{ stats.totalSoutenances }} soutenances</div>
          <div class="metric-info soutenance-status">
            <div class="status-pill">
              <span class="label">Planifiées</span>
              <span class="value">{{ stats.plannedSoutenances }}</span>
            </div>
            <div class="status-pill">
              <span class="label">Réalisées</span>
              <span class="value">{{ stats.completedSoutenances }}</span>
            </div>
          </div>
          <div class="metric-footer">
            <Button icon="pi pi-arrow-right" class="p-button-rounded p-button-text" @click="navigateTo('/management/soutenances')" />
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Résultats</div>
          </div>
          <div class="metric-value">{{ stats.averageGrade.toFixed(1) }} / 20</div>
          <div class="metric-info">
            <div class="grade-distribution">
              <div class="grade-item">
                <span class="grade-label">Très bien:</span>
                <span class="grade-count">{{ stats.honorsCount }}</span>
              </div>
              <div class="grade-item">
                <span class="grade-label">Bien:</span>
                <span class="grade-count">{{ stats.goodCount }}</span>
              </div>
              <div class="grade-item">
                <span class="grade-label">Passable:</span>
                <span class="grade-count">{{ stats.passCount }}</span>
              </div>
            </div>
          </div>
          <div class="metric-footer">
            <Button icon="pi pi-arrow-right" class="p-button-rounded p-button-text" @click="navigateTo('/management/notes-management')" />
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Filières</div>
          </div>
          <div class="metric-value">{{ stats.totalFilieres }} filières</div>
          <div class="metric-info">
            <div v-for="(filiere, index) in topFilieres" :key="index" class="filiere-item">
              <span class="filiere-name">{{ filiere.nom }}</span>
              <span class="filiere-count">{{ filiere.etudiantCount }} étudiants</span>
            </div>
          </div>
          <div class="metric-footer">
            <Button icon="pi pi-arrow-right" class="p-button-rounded p-button-text" @click="navigateTo('/management/comptes')" />
          </div>
        </div>
      </div>
      
      <!-- Dashboard Content Sections -->
      <div class="dashboard-sections">
        <!-- Upcoming Soutenances Section -->
        <div class="upcoming-soutenances">
          <div class="section-header">
            <h3>Soutenances à venir</h3>
            <Button label="Voir tout" icon="pi pi-calendar" class="p-button-text" @click="navigateTo('/management/soutenances')" />
          </div>
          
          <div v-if="loadingItems.soutenances" class="loading-indicator">
            <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="4" />
            <span>Chargement des soutenances...</span>
          </div>
          
          <div v-else-if="upcomingSoutenances.length === 0" class="empty-state">
            <i class="pi pi-calendar-times empty-icon"></i>
            <p>Aucune soutenance planifiée pour les prochains jours</p>
          </div>
          
          <div v-else class="soutenance-list">
            <div v-for="(soutenance, index) in upcomingSoutenances" :key="index" class="soutenance-item">
              <div class="soutenance-date">
                <div class="date-day">{{ formatDay(soutenance.date) }}</div>
                <div class="date-month">{{ formatMonth(soutenance.date) }}</div>
                <div class="date-time">{{ soutenance.heure }}</div>
              </div>
              <div class="soutenance-details">
                <div class="soutenance-students">
                  {{ getStudentNames(soutenance.binome) }}
                </div>
                <div class="soutenance-title">{{ soutenance.binome?.sujet?.titre || 'Pas de titre' }}</div>
                <div class="soutenance-info">
                  <span class="soutenance-location">
                    <i class="pi pi-map-marker"></i> {{ soutenance.salle?.nom || 'Salle non définie' }}
                  </span>
                  <span class="soutenance-jury">
                    <i class="pi pi-users"></i> {{ getJuryNames(soutenance) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Recent Activities Section -->
        <div class="recent-activities">
          <div class="section-header">
            <h3>Activités récentes</h3>
          </div>
          
          <div v-if="loadingItems.activities" class="loading-indicator">
            <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="4" />
            <span>Chargement des activités...</span>
          </div>
          
          <div v-else-if="activities.length === 0" class="empty-state">
            <i class="pi pi-history empty-icon"></i>
            <p>Aucune activité récente à afficher</p>
          </div>
          
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import ApiService from '@/services/ApiService';

// Import PrimeVue components
import Button from 'primevue/button';
import ProgressBar from 'primevue/progressbar';
import ProgressSpinner from 'primevue/progressspinner';
import Toast from 'primevue/toast';

const router = useRouter();
const toast = useToast();
const currentYear = ref('');

// Loading states
const loading = ref(true);
const loadingItems = ref({
  stats: true,
  soutenances: true,
  activities: true
});

// Error state
const error = ref(null);

// Dashboard data
const stats = ref({
  totalUsers: 0,
  totalStudents: 0,
  totalSupervisors: 0,
  totalJuries: 0,
  totalBinomes: 0,
  binomesWithSoutenance: 0,
  totalSujets: 0,
  pendingSuggestions: 0,
  totalSoutenances: 0,
  plannedSoutenances: 0,
  completedSoutenances: 0,
  averageGrade: 0,
  honorsCount: 0,
  goodCount: 0,
  passCount: 0,
  totalFilieres: 0,
  filieres: []
});

// Top filieres data
const topFilieres = computed(() => {
  if (!stats.value.filieres) return [];
  return stats.value.filieres.slice(0, 3);
});

// Upcoming soutenances
const upcomingSoutenances = ref([]);

// Recent activities
const activities = ref([]);

// Calculate completion rate
const calculateCompletionRate = (completed, total) => {
  if (total === 0) return 0;
  return Math.round((completed / total) * 100);
};

// Helper methods for displaying student and jury names
const getStudentNames = (binome) => {
  if (!binome) return 'Étudiants non définis';
  
  const etudiant1 = binome.etudiant1 ? `${binome.etudiant1.prenom} ${binome.etudiant1.nom}` : '';
  const etudiant2 = binome.etudiant2 ? `${binome.etudiant2.prenom} ${binome.etudiant2.nom}` : '';
  
  if (etudiant1 && etudiant2) {
    return `${etudiant1} & ${etudiant2}`;
  } else if (etudiant1) {
    return etudiant1;
  } else {
    return 'Étudiants non définis';
  }
};

const getJuryNames = (soutenance) => {
  if (!soutenance.jury1 && !soutenance.jury2) return 'Jury non défini';
  
  const jury1 = soutenance.jury1 ? `${soutenance.jury1.prenom.charAt(0)}. ${soutenance.jury1.nom}` : '';
  const jury2 = soutenance.jury2 ? `${soutenance.jury2.prenom.charAt(0)}. ${soutenance.jury2.nom}` : '';
  
  if (jury1 && jury2) {
    return `${jury1}, ${jury2}`;
  } else if (jury1) {
    return jury1;
  } else if (jury2) {
    return jury2;
  }
};

// Date formatting utilities
const formatDay = (date) => {
  if (!date) return '';
  const dateObj = new Date(date);
  return new Intl.DateTimeFormat('fr-FR', { day: 'numeric' }).format(dateObj);
};

const formatMonth = (date) => {
  if (!date) return '';
  const dateObj = new Date(date);
  return new Intl.DateTimeFormat('fr-FR', { month: 'short' }).format(dateObj);
};

const formatDate = (date) => {
  if (!date) return '';
  const now = new Date();
  const dateObj = new Date(date);
  const diffInDays = Math.floor((now - dateObj) / (1000 * 60 * 60 * 24));
  
  if (diffInDays === 0) {
    return "Aujourd'hui, " + new Intl.DateTimeFormat('fr-FR', {
      hour: 'numeric',
      minute: 'numeric'
    }).format(dateObj);
  } else if (diffInDays === 1) {
    return 'Hier, ' + new Intl.DateTimeFormat('fr-FR', {
      hour: 'numeric',
      minute: 'numeric'
    }).format(dateObj);
  } else {
    return new Intl.DateTimeFormat('fr-FR', {
      day: 'numeric',
      month: 'short',
      hour: 'numeric',
      minute: 'numeric'
    }).format(dateObj);
  }
};

// Navigation method
const navigateTo = (path) => {
  router.push(path);
};

// Load all dashboard data
const loadDashboardData = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // Load dashboard stats
    await fetchDashboardStats();
    
    // Load upcoming soutenances
    await fetchUpcomingSoutenances();
    
    // Load recent activities
    await fetchRecentActivities();
    
    loading.value = false;
  } catch (err) {
    console.error('Error loading dashboard data:', err);
    error.value = 'Impossible de charger les données du tableau de bord. Veuillez réessayer plus tard.';
    loading.value = false;
  }
};

// API data fetching methods
const fetchDashboardStats = async () => {
  loadingItems.value.stats = true;
  try {
    const response = await ApiService.get('/chef_de_departement/dashboard/stats');
    
    if (response && Object.keys(response).length > 0) {
      stats.value = response;
      
      // Set current year - in a real app this would come from the API
      currentYear.value = new Date().getFullYear() + '/' + (new Date().getFullYear() + 1);
    } else {
      throw new Error('Empty response received from API');
    }
  } catch (error) {
    console.error('Error fetching dashboard stats:', error);
    throw error;
  } finally {
    loadingItems.value.stats = false;
  }
};

const fetchUpcomingSoutenances = async () => {
  loadingItems.value.soutenances = true;
  try {
    const response = await ApiService.get('/chef_de_departement/dashboard/upcoming-soutenances');
    
    if (response && Array.isArray(response)) {
      upcomingSoutenances.value = response;
    } else {
      throw new Error('Invalid response format for upcoming soutenances');
    }
  } catch (error) {
    console.error('Error fetching upcoming soutenances:', error);
    throw error;
  } finally {
    loadingItems.value.soutenances = false;
  }
};

const fetchRecentActivities = async () => {
  loadingItems.value.activities = true;
  try {
    const response = await ApiService.get('/chef_de_departement/dashboard/activities');
    
    if (response && Array.isArray(response)) {
      activities.value = response;
    } else {
      throw new Error('Invalid response format for recent activities');
    }
  } catch (error) {
    console.error('Error fetching recent activities:', error);
    throw error;
  } finally {
    loadingItems.value.activities = false;
  }
};

// Fetch all data on component mount
onMounted(() => {
  loadDashboardData();
});
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Loading indicator */
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--text-color-secondary);
}

/* Error container */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
}

.error-container i {
  font-size: 3rem;
  color: #f44336;
  margin-bottom: 1rem;
}

.error-container h3 {
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.error-container p {
  color: var(--text-color-secondary);
  margin-bottom: 1rem;
}

/* Welcome card */
.welcome-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
}

/* Metrics grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.metric-card {
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  transition: transform 0.2s, box-shadow 0.2s;
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.metric-title {
  font-weight: 600;
  color: var(--text-color);
  font-size: 1.1rem;
}

.metric-value {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: var(--primary-color);
}

.metric-info {
  flex: 1;
  margin-bottom: 1rem;
}

.binome-progress {
  height: 0.5rem;
  border-radius: 0.25rem;
}

:deep(.binome-progress .p-progressbar-value) {
  background-color: var(--primary-color);
}

.progress-info {
  font-size: 0.85rem;
  color: var(--text-color-secondary);
  margin-top: 0.5rem;
}

.suggestion-alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #f59e0b;
  font-size: 0.9rem;
  padding: 0.5rem;
  border-radius: 0.25rem;
  background-color: rgba(245, 158, 11, 0.1);
}

.metric-counts {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.count-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}

.count-label {
  color: var(--text-color-secondary);
}

.count-value {
  font-weight: 600;
  color: var(--text-color);
}

.soutenance-status {
  display: flex;
  gap: 1rem;
}

.status-pill {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.75rem;
  background-color: rgba(var(--primary-color-rgb), 0.1);
  border-radius: 0.5rem;
}

.status-pill .label {
  font-size: 0.8rem;
  color: var(--text-color-secondary);
  margin-bottom: 0.25rem;
}

.status-pill .value {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--primary-color);
}

.grade-distribution {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.grade-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}

.grade-label {
  color: var(--text-color-secondary);
}

.grade-count {
  font-weight: 600;
  color: var(--text-color);
}

.filiere-item {
  display: flex;
  justify-content: space-between;
  padding: 0.25rem 0;
  border-bottom: 1px solid var(--surface-border);
  font-size: 0.9rem;
}

.filiere-item:last-child {
  border-bottom: none;
}

.filiere-name {
  color: var(--text-color);
}

.filiere-count {
  font-weight: 600;
  color: var(--primary-color);
}

.metric-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

/* Dashboard sections */
.dashboard-sections {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.upcoming-soutenances,
.recent-activities {
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--surface-border);
  padding-bottom: 0.75rem;
}

.section-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-color);
  font-weight: 600;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--text-color-secondary);
  text-align: center;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  opacity: 0.6;
}

/* Soutenance list */
.soutenance-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.soutenance-item {
  display: flex;
  gap: 1rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background-color: var(--surface-hover);
  transition: transform 0.2s, box-shadow 0.2s;
}

.soutenance-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.soutenance-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 3.5rem;
  background-color: var(--primary-color);
  color: white;
  border-radius: 0.5rem;
  padding: 0.5rem;
  text-align: center;
}

.date-day {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.date-month {
  font-size: 0.8rem;
  text-transform: uppercase;
  margin-bottom: 0.25rem;
}

.date-time {
  font-size: 0.75rem;
  padding: 0.15rem 0.5rem;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 1rem;
}

.soutenance-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.soutenance-students {
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.25rem;
}

.soutenance-title {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
  margin-bottom: 0.5rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.soutenance-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--text-color-secondary);
}

.soutenance-location, 
.soutenance-jury {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* Activity list */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background-color: var(--surface-hover);
  transition: transform 0.2s ease;
}

.activity-item:hover {
  transform: translateX(5px);
}

.activity-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 2.5rem;
  height: 2.5rem;
  background-color: rgba(var(--primary-color-rgb), 0.1);
  color: var(--primary-color);
  border-radius: 50%;
}

.activity-content {
  flex: 1;
}

.activity-description {
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.activity-time {
  font-size: 0.8rem;
  color: var(--text-color-secondary);
}

/* Dark mode adjustments */
.dark-mode .metric-card,
.dark-mode .upcoming-soutenances,
.dark-mode .recent-activities,
.dark-mode .welcome-card {
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark-mode .soutenance-item,
.dark-mode .activity-item {
  background-color: rgba(255, 255, 255, 0.03);
}

.dark-mode .suggestion-alert {
  background-color: rgba(245, 158, 11, 0.2);
}

.dark-mode .status-pill {
  background-color: rgba(255, 255, 255, 0.08);
}

/* Responsive adjustments */
@media (max-width: 992px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-info {
    margin-bottom: 1rem;
  }
  
  .user-avatar {
    margin-left: 0;
  }
  
  .dashboard-sections {
    grid-template-columns: 1fr;
  }
  
  .soutenance-info {
    flex-direction: column;
    gap: 0.25rem;
  }
}

@media (max-width: 576px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .metric-card {
    padding: 1rem;
  }
  
  .metric-value {
    font-size: 1.5rem;
  }
  
  .soutenance-item {
    flex-direction: column;
  }
  
  .soutenance-date {
    flex-direction: row;
    width: 100%;
    min-width: unset;
    gap: 0.5rem;
    padding: 0.5rem;
  }
  
  .date-day,
  .date-month {
    font-size: 1rem;
  }
}

.welcome-card h1 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-color);
  font-weight: 600;
}

.welcome-subtitle {
  margin-top: 0.25rem;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
  font-weight: 500;
}
</style>