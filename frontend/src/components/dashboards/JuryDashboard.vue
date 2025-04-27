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
      <!-- Welcome section -->
      <div class="welcome-card">
        <div class="welcome-info">
          <h1>Bienvenue, {{ dashboardData?.juryInfo?.prenom }} {{ dashboardData?.juryInfo?.nom }}</h1>
          <p class="welcome-subtitle">
            Membre du Jury - Année académique {{ dashboardData?.juryInfo?.anneeAcademique }}
          </p>
        </div>
        <div class="user-avatar">
          <div class="avatar-circle">
            <span>{{ getInitials(dashboardData?.juryInfo?.prenom, dashboardData?.juryInfo?.nom) }}</span>
          </div>
        </div>
      </div>
      
      <!-- Reminders section -->
      <div v-if="dashboardData?.reminders?.length > 0" class="reminders-section">
        <div class="section-header">
          <h3><i class="pi pi-exclamation-triangle"></i> Rappels importants</h3>
        </div>
        <div class="reminders-grid">
          <div 
            v-for="reminder in dashboardData.reminders" 
            :key="reminder.type"
            class="reminder-card"
            :class="'severity-' + reminder.severity"
          >
            <div class="reminder-icon">
              <i :class="getReminderIcon(reminder.type)"></i>
            </div>
            <div class="reminder-content">
              <h4>{{ reminder.title }}</h4>
              <p>{{ reminder.message }}</p>
              <div v-if="reminder.daysRemaining !== null" class="days-remaining">
                <i class="pi pi-clock"></i> 
                {{ reminder.daysRemaining === 0 ? "Aujourd'hui" : "Dans " + reminder.daysRemaining + " jours" }}
              </div>
              <Button 
                v-if="reminder.actionUrl" 
                label="Action requise"
                icon="pi pi-arrow-right"
                class="p-button-sm"
                :class="{
                  'p-button-danger': reminder.severity === 'critical',
                  'p-button-warning': reminder.severity === 'warning'
                }"
                @click="navigateTo(reminder.actionUrl)"
              />
            </div>
          </div>
        </div>
      </div>
      
      <!-- Metrics grid -->
      <div class="metrics-grid">
        <!-- Soutenances card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Soutenances</div>
            <div class="metric-badge">
              <span class="badge-value">{{ dashboardData?.soutenanceStats?.todaySoutenances }}</span>
              <span class="badge-label">aujourd'hui</span>
            </div>
          </div>
          <div class="metric-value">{{ dashboardData?.soutenanceStats?.totalSoutenances }}</div>
          <div class="metric-info">
            <ProgressBar 
              :value="calculateCompletionRate(
                dashboardData?.soutenanceStats?.evaluatedSoutenances, 
                dashboardData?.soutenanceStats?.totalSoutenances
              )" 
              class="soutenance-progress" 
            />
            <div class="progress-info">
              <span>{{ dashboardData?.soutenanceStats?.evaluatedSoutenances }} évaluées</span>
              <span class="divider">·</span>
              <span>{{ dashboardData?.soutenanceStats?.pendingSoutenances }} en attente</span>
            </div>
            <div v-if="dashboardData?.soutenanceStats?.averageEvaluationScore" class="average-score">
              <i class="pi pi-star"></i> Moyenne: {{ dashboardData.soutenanceStats.averageEvaluationScore.toFixed(1) }}/20
            </div>
          </div>
          <div class="metric-footer">
            <Button 
              icon="pi pi-arrow-right" 
              class="p-button-rounded p-button-text" 
              @click="navigateTo('/jury/soutenances')" 
            />
          </div>
        </div>
        
        <!-- Reports card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Rapports</div>
            <div class="metric-badge">
              <span class="badge-value">{{ dashboardData?.reportStats?.pendingReports }}</span>
              <span class="badge-label">en attente</span>
            </div>
          </div>
          <div class="metric-value">{{ dashboardData?.reportStats?.totalReports }}</div>
          <div class="metric-info">
            <ProgressBar 
              :value="calculateCompletionRate(
                dashboardData?.reportStats?.evaluatedReports, 
                dashboardData?.reportStats?.totalReports
              )" 
              class="report-progress" 
            />
            <div class="progress-info">
              <span>{{ dashboardData?.reportStats?.evaluatedReports }} évalués</span>
            </div>
            <div v-if="dashboardData?.reportStats?.averageReportScore" class="average-score">
              <i class="pi pi-star"></i> Moyenne: {{ dashboardData.reportStats.averageReportScore.toFixed(1) }}/20
            </div>
          </div>
          <div class="metric-footer">
            <Button 
              icon="pi pi-arrow-right" 
              class="p-button-rounded p-button-text" 
              @click="navigateTo('/jury/report-evaluation')" 
            />
          </div>
        </div>
        
        <!-- Binomes card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Binômes</div>
          </div>
          <div class="metric-value">{{ dashboardData?.binomeStats?.totalBinomes }}</div>
          <div class="metric-info">
            <div class="binome-status">
              <div class="status-pill">
                <span class="label">Évalués</span>
                <span class="value">{{ dashboardData?.binomeStats?.fullyEvaluatedBinomes }}</span>
              </div>
              <div class="status-pill">
                <span class="label">Partiels</span>
                <span class="value">{{ dashboardData?.binomeStats?.partiallyEvaluatedBinomes }}</span>
              </div>
              <div class="status-pill">
                <span class="label">En attente</span>
                <span class="value">{{ dashboardData?.binomeStats?.pendingBinomes }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Dashboard sections -->
      <div class="dashboard-sections">
        <!-- Upcoming soutenances -->
        <div class="upcoming-soutenances">
          <div class="section-header">
            <h3>Soutenances à venir</h3>
            <Button 
              label="Voir tout" 
              icon="pi pi-calendar" 
              class="p-button-text" 
              @click="navigateTo('/jury/soutenances')" 
            />
          </div>
          
          <div v-if="!dashboardData?.upcomingSoutenances?.length" class="empty-state">
            <i class="pi pi-calendar-times empty-icon"></i>
            <p>Aucune soutenance prévue dans les prochains jours</p>
          </div>
          
          <div v-else class="soutenance-list">
            <div 
              v-for="soutenance in dashboardData.upcomingSoutenances" 
              :key="soutenance.id" 
              class="soutenance-item"
            >
              <div class="soutenance-date">
                <div class="date-day">{{ formatDay(soutenance.date) }}</div>
                <div class="date-month">{{ formatMonth(soutenance.date) }}</div>
                <div class="date-time">{{ soutenance.heure }}</div>
              </div>
              <div class="soutenance-details">
                <div class="soutenance-students">
                  {{ getStudentNames(soutenance.binome) }}
                </div>
                <div class="soutenance-title">{{ soutenance.sujetTitre }}</div>
                <div class="soutenance-info">
                  <span class="soutenance-location">
                    <i class="pi pi-map-marker"></i> {{ soutenance.salle }}
                  </span>
                  <Tag 
                    v-if="soutenance.isEvaluated" 
                    severity="success" 
                    value="Évalué" 
                  />
                  <Tag 
                    v-else-if="soutenance.daysUntilSoutenance === 0" 
                    severity="warning" 
                    value="Aujourd'hui" 
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Recent activities -->
        <div class="recent-activities">
          <div class="section-header">
            <h3>Activités récentes</h3>
          </div>
          
          <div v-if="!dashboardData?.recentActivities?.length" class="empty-state">
            <i class="pi pi-history empty-icon"></i>
            <p>Aucune activité récente</p>
          </div>
          
          <div v-else class="activity-list">
            <div 
              v-for="activity in dashboardData.recentActivities" 
              :key="activity.id" 
              class="activity-item"
            >
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import ApiService from '@/services/ApiService';
import UserInfoHeader from '@/components/UserInfoHeader.vue';

// Import PrimeVue components
import Button from 'primevue/button';
import ProgressBar from 'primevue/progressbar';
import ProgressSpinner from 'primevue/progressspinner';
import Toast from 'primevue/toast';
import Tag from 'primevue/tag';

const router = useRouter();
const toast = useToast();
const dashboardData = ref(null);
const loading = ref(true);
const error = ref(null);

onMounted(() => {
  loadDashboardData();
});

const loadDashboardData = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await ApiService.get('/jury/dashboard');
    dashboardData.value = response;
  } catch (err) {
    console.error('Error loading dashboard:', err);
    error.value = 'Impossible de charger le tableau de bord. Veuillez réessayer plus tard.';
  } finally {
    loading.value = false;
  }
};

const navigateTo = (path) => {
  router.push(path);
};

const calculateCompletionRate = (completed, total) => {
  if (!total) return 0;
  return Math.round((completed / total) * 100);
};

const getInitials = (firstName, lastName) => {
  if (!firstName && !lastName) return '?';
  return (firstName?.charAt(0) || '') + (lastName?.charAt(0) || '');
};

const getReminderIcon = (type) => {
  switch (type) {
    case 'RAPPORT': return 'pi pi-file-pdf';
    case 'SOUTENANCE': return 'pi pi-calendar';
    default: return 'pi pi-info-circle';
  }
};

const getStudentNames = (binome) => {
  if (!binome) return '';
  const etudiant1 = binome.etudiant1 ? binome.etudiant1.prenom + ' ' + binome.etudiant1.nom : '';
  const etudiant2 = binome.etudiant2 ? binome.etudiant2.prenom + ' ' + binome.etudiant2.nom : '';
  return etudiant2 ? etudiant1 + ' & ' + etudiant2 : etudiant1;
};

const formatDay = (dateString) => {
  const date = new Date(dateString);
  return date.getDate();
};

const formatMonth = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('fr-FR', { month: 'short' }).toUpperCase();
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));
  
  if (diffInDays === 0) {
    return "Aujourd'hui, " + date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  } else if (diffInDays === 1) {
    return 'Hier, ' + date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  } else {
    return date.toLocaleDateString('fr-FR', { 
      day: 'numeric', 
      month: 'short', 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
};
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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

/* Reminders section */
.reminders-section {
  margin-bottom: 2rem;
}

.reminders-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.reminders-section h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-color);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.reminders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.reminder-card {
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 1rem;
  transition: transform 0.2s;
}

.reminder-card:hover {
  transform: translateY(-2px);
}

.reminder-card.severity-critical {
  border-left: 4px solid #f44336;
}

.reminder-card.severity-warning {
  border-left: 4px solid #ff9800;
}

.reminder-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 2.5rem;
  height: 2.5rem;
  background-color: rgba(var(--primary-color-rgb), 0.1);
  color: var(--primary-color);
  border-radius: 50%;
}

.reminder-content {
  flex: 1;
}

.reminder-content h4 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
  color: var(--text-color);
}

.reminder-content p {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
  margin: 0;
}

.days-remaining {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-color-secondary);
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

.metric-badge {
  background-color: rgba(var(--primary-color-rgb), 0.1);
  color: var(--primary-color);
  padding: 0.25rem 0.5rem;
  border-radius: 1rem;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge-value {
  margin-right: 0.25rem;
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

.soutenance-progress,
.report-progress {
  height: 0.5rem;
  border-radius: 0.25rem;
}

:deep(.soutenance-progress .p-progressbar-value),
:deep(.report-progress .p-progressbar-value) {
  background-color: var(--primary-color);
}

.progress-info {
  font-size: 0.85rem;
  color: var(--text-color-secondary);
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
}

.divider {
  color: var(--text-color-secondary);
}

.average-score {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  color: var(--primary-color);
  font-weight: 600;
}

.binome-status {
  display: flex;
  gap: 0.5rem;
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
  align-items: center;
  font-size: 0.8rem;
  color: var(--text-color-secondary);
}

.soutenance-location {
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

/* Loading and error states */
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--text-color-secondary);
}

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

/* Dark mode adjustments */
.dark-mode .metric-card,
.dark-mode .welcome-card,
.dark-mode .reminder-card,
.dark-mode .upcoming-soutenances,
.dark-mode .recent-activities {
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark-mode .status-pill {
  background-color: rgba(255, 255, 255, 0.08);
}

.dark-mode .soutenance-item,
.dark-mode .activity-item {
  background-color: rgba(255, 255, 255, 0.03);
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
}

@media (max-width: 576px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .metric-value {
    font-size: 1.5rem;
  }
}
</style>