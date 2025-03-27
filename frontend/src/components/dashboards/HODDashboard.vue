<template>
  <div class="dashboard-container">
    <Toast />
    <!-- User Info Header -->
    
    
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
            <span class="filiere-name">{{ filiere.name }}</span>
            <span class="filiere-count">{{ filiere.count }} étudiants</span>
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
        
        <div v-if="loading.soutenances" class="loading-indicator">
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
        
        <div v-if="loading.activities" class="loading-indicator">
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import ApiService from '@/services/ApiService';
import AuthService from '@/services/AuthService';
import UserInfoHeader from '@/components/UserInfoHeader.vue';

// Import PrimeVue components
import Button from 'primevue/button';
import ProgressBar from 'primevue/progressbar';
import ProgressSpinner from 'primevue/progressspinner';
import Toast from 'primevue/toast';

const router = useRouter();
const toast = useToast();
const searchQuery = ref('');

// Loading states
const loading = ref({
  stats: true,
  soutenances: true,
  activities: true
});

// Dashboard stats
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
  totalFilieres: 0
});

// Top filieres data
const topFilieres = ref([]);

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

// API data fetching methods
const fetchDashboardStats = async () => {
  loading.value.stats = true;
  try {
    const response = await ApiService.get('/chef_de_departement/dashboard/stats');
    
    // Check if we have a valid response, if not use sample data
    if (response && Object.keys(response).length > 0) {
      stats.value = {
        ...stats.value,
        ...response
      };
      
      // Extract top filieres if available
      if (response.filieres && Array.isArray(response.filieres)) {
        topFilieres.value = response.filieres.slice(0, 3).map(f => ({
          name: f.nom,
          count: f.etudiantCount
        }));
      }
    } else {
      // Use sample data if API returns empty
      console.warn('Using sample data for dashboard stats as API returned empty response');
      useSampleStats();
    }
  } catch (error) {
    console.error('Error fetching dashboard stats:', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les statistiques du tableau de bord',
      life: 3000
    });
    useSampleStats();
  } finally {
    loading.value.stats = false;
  }
};

const fetchUpcomingSoutenances = async () => {
  loading.value.soutenances = true;
  try {
    const response = await ApiService.get('/chef_de_departement/dashboard/upcoming-soutenances');
    
    if (response && Array.isArray(response) && response.length > 0) {
      upcomingSoutenances.value = response;
    } else {
      // Try the regular soutenances endpoint and filter for future dates
      const allSoutenances = await ApiService.get('/chef_de_departement/soutenances');
      
      if (Array.isArray(allSoutenances)) {
        // Filter soutenances for the upcoming week
        const today = new Date();
        const nextWeek = new Date();
        nextWeek.setDate(today.getDate() + 7);
        
        upcomingSoutenances.value = allSoutenances
          .filter(s => {
            const soutenanceDate = new Date(s.date);
            return soutenanceDate >= today && soutenanceDate <= nextWeek;
          })
          .sort((a, b) => new Date(a.date) - new Date(b.date))
          .slice(0, 5);
      } else {
        console.warn('Using sample soutenances data as API returned invalid response');
        useSampleSoutenances();
      }
    }
  } catch (error) {
    console.error('Error fetching upcoming soutenances:', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les soutenances à venir',
      life: 3000
    });
    useSampleSoutenances();
  } finally {
    loading.value.soutenances = false;
  }
};

const fetchRecentActivities = async () => {
  loading.value.activities = true;
  try {
    const response = await ApiService.get('/chef_de_departement/dashboard/activities');
    
    if (response && Array.isArray(response) && response.length > 0) {
      activities.value = response;
    } else {
      console.warn('Using sample activities data as API returned invalid response');
      useSampleActivities();
    }
  } catch (error) {
    console.error('Error fetching recent activities:', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les activités récentes',
      life: 3000
    });
    useSampleActivities();
  } finally {
    loading.value.activities = false;
  }
};

// Navigation method
const navigateTo = (path) => {
  router.push(path);
};

// Search handler
const handleSearchQuery = (query) => {
  searchQuery.value = query;
  // In a real application, you would filter your data based on the query
  console.log('Search query updated:', query);
};

// Sample data for development or when API fails
const useSampleStats = () => {
  stats.value = {
    totalUsers: 145,
    totalStudents: 120,
    totalSupervisors: 15,
    totalJuries: 10,
    totalBinomes: 68,
    binomesWithSoutenance: 42,
    totalSujets: 92,
    pendingSuggestions: 5,
    totalSoutenances: 42,
    plannedSoutenances: 12,
    completedSoutenances: 30,
    averageGrade: 15.7,
    honorsCount: 15,
    goodCount: 20,
    passCount: 7,
    totalFilieres: 4
  };
  
  topFilieres.value = [
    { name: 'Génie Informatique', count: 45 },
    { name: 'Génie Civil', count: 35 },
    { name: 'Génie Électrique', count: 25 }
  ];
};

const useSampleSoutenances = () => {
  // Generate dates for the next 5 days
  const today = new Date();
  
  upcomingSoutenances.value = [
    {
      id: 1,
      date: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1),
      heure: '10:00',
      salle: { id: 1, nom: 'Salle A-203' },
      jury1: { id: 1, nom: 'Bensouda', prenom: 'Omar' },
      jury2: { id: 2, nom: 'Alaoui', prenom: 'Fatima' },
      binome: {
        id: 1,
        etudiant1: { id: 1, nom: 'Amrani', prenom: 'Karim' },
        etudiant2: { id: 2, nom: 'Tazi', prenom: 'Salma' },
        sujet: { id: 1, titre: 'Intelligence Artificielle pour la Détection des Anomalies' }
      }
    },
    {
      id: 2,
      date: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2),
      heure: '14:30',
      salle: { id: 2, nom: 'Amphithéâtre B' },
      jury1: { id: 3, nom: 'Mansouri', prenom: 'Ahmed' },
      jury2: { id: 4, nom: 'Benjelloun', prenom: 'Sara' },
      binome: {
        id: 2,
        etudiant1: { id: 3, nom: 'El Fassi', prenom: 'Mohammed' },
        etudiant2: null,
        sujet: { id: 2, titre: 'Optimisation des Systèmes Distribués' }
      }
    },
    {
      id: 3,
      date: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 3),
      heure: '09:00',
      salle: { id: 3, nom: 'Salle C-105' },
      jury1: { id: 5, nom: 'Rami', prenom: 'Younes' },
      jury2: { id: 6, nom: 'Khalil', prenom: 'Nadia' },
      binome: {
        id: 3,
        etudiant1: { id: 5, nom: 'Idrissi', prenom: 'Youssef' },
        etudiant2: { id: 6, nom: 'Daoudi', prenom: 'Malika' },
        sujet: { id: 3, titre: 'Application Mobile pour le Suivi Médical' }
      }
    }
  ];
};

const useSampleActivities = () => {
  const now = new Date();
  
  activities.value = [
    {
      id: 1,
      description: 'Ahmed El Mansouri a ajouté un nouvel utilisateur',
      timestamp: new Date(now.getFullYear(), now.getMonth(), now.getDate(), now.getHours() - 2, 30),
      icon: 'pi pi-user-plus'
    },
    {
      id: 2,
      description: 'Nouvelle proposition de sujet par Sara Benjelloun',
      timestamp: new Date(now.getFullYear(), now.getMonth(), now.getDate(), now.getHours() - 5, 15),
      icon: 'pi pi-file'
    },
    {
      id: 3,
      description: 'Modification du planning des soutenances',
      timestamp: new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 16, 45),
      icon: 'pi pi-calendar'
    },
    {
      id: 4,
      description: 'Rapport de fin de semestre généré',
      timestamp: new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 10, 0),
      icon: 'pi pi-chart-bar'
    }
  ];
};

// Fetch all data on component mount
onMounted(async () => {
  // Fetch all dashboard data
  await Promise.all([
    fetchDashboardStats(),
    fetchUpcomingSoutenances(),
    fetchRecentActivities()
  ]);
});
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--text-color-secondary);
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
.dark-mode .recent-activities {
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
</style>