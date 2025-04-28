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
          <h1>Bienvenue, {{ dashboardData?.encadrantInfo?.prenom }} {{ dashboardData?.encadrantInfo?.nom }}</h1>
          <p class="welcome-subtitle">
            Encadrant - Année académique {{ dashboardData?.encadrantInfo?.anneeAcademique }}
          </p>
        </div>
        <div class="user-avatar">
          <div class="avatar-circle">
            <span>{{ getInitials(dashboardData?.encadrantInfo?.prenom, dashboardData?.encadrantInfo?.nom) }}</span>
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
        <!-- Binomes card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Binômes Encadrés</div>
            <div class="metric-badge">
              <span class="badge-value">{{ dashboardData?.binomeStats?.activeBinomes }}</span>
              <span class="badge-label">actifs</span>
            </div>
          </div>
          <div class="metric-value">{{ dashboardData?.binomeStats?.totalBinomes }}</div>
          <div class="metric-info">
            <ProgressBar 
              :value="calculateCompletionRate(
                dashboardData?.binomeStats?.evaluatedBinomes, 
                dashboardData?.binomeStats?.totalBinomes
              )" 
              class="binome-progress" 
            />
            <div class="progress-info">
              <span>{{ dashboardData?.binomeStats?.evaluatedBinomes }} évalués</span>
              <span class="divider">·</span>
              <span>{{ dashboardData?.binomeStats?.completedProjects }} terminés</span>
            </div>
            <div v-if="dashboardData?.binomeStats?.averageProgressScore" class="average-score">
              <i class="pi pi-chart-line"></i> Progression moyenne: {{ dashboardData.binomeStats.averageProgressScore.toFixed(1) }}/20
            </div>
          </div>
          <div class="metric-footer">
            <Button 
              icon="pi pi-arrow-right" 
              class="p-button-rounded p-button-text" 
              @click="navigateTo('/encadrant/grading')" 
            />
          </div>
        </div>
        
        <!-- Documents card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Documents</div>
            <div class="metric-badge">
              <span class="badge-value">{{ dashboardData?.documentStats?.pendingReviews }}</span>
              <span class="badge-label">en attente</span>
            </div>
          </div>
          <div class="metric-value">{{ dashboardData?.documentStats?.totalDocuments }}</div>
          <div class="metric-info">
            <div class="document-metrics">
              <div class="status-pill">
                <span class="label">Revus aujourd'hui</span>
                <span class="value">{{ dashboardData?.documentStats?.reviewedToday }}</span>
              </div>
              <div class="status-pill">
                <span class="label">Cette semaine</span>
                <span class="value">{{ dashboardData?.documentStats?.documentsSubmittedThisWeek }}</span>
              </div>
              <div class="status-pill" v-if="dashboardData?.documentStats?.overdueReviews > 0">
                <span class="label urgent">En retard</span>
                <span class="value urgent">{{ dashboardData?.documentStats?.overdueReviews }}</span>
              </div>
            </div>
            <div v-if="dashboardData?.documentStats?.averageResponseTime" class="average-score">
              <i class="pi pi-clock"></i> Temps de réponse: {{ dashboardData.documentStats.averageResponseTime.toFixed(0) }}h
            </div>
          </div>
          <div class="metric-footer">
            <Button 
              icon="pi pi-arrow-right" 
              class="p-button-rounded p-button-text" 
              @click="navigateTo('/encadrant/document-evaluation')" 
            />
          </div>
        </div>
        
        <!-- Evaluations card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Évaluations</div>
            <div class="metric-badge">
              <span class="badge-value">{{ dashboardData?.evaluationStats?.averageGrade?.toFixed(1) }}</span>
              <span class="badge-label">/20</span>
            </div>
          </div>
          <div class="metric-value">{{ dashboardData?.evaluationStats?.evaluatedBinomes }}</div>
          <div class="metric-info">
            <div class="grade-distribution">
              <div class="grade-item">
                <span class="grade-count">{{ dashboardData?.evaluationStats?.gradeDistribution?.excellentCount }}</span>
                <span class="grade-label">Excellent</span>
              </div>
              <div class="grade-item">
                <span class="grade-count">{{ dashboardData?.evaluationStats?.gradeDistribution?.goodCount }}</span>
                <span class="grade-label">Bien</span>
              </div>
              <div class="grade-item">
                <span class="grade-count">{{ dashboardData?.evaluationStats?.gradeDistribution?.fairCount }}</span>
                <span class="grade-label">Passable</span>
              </div>
              <div class="grade-item" v-if="dashboardData?.evaluationStats?.gradeDistribution?.failCount > 0">
                <span class="grade-count urgent">{{ dashboardData?.evaluationStats?.gradeDistribution?.failCount }}</span>
                <span class="grade-label urgent">À améliorer</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Dashboard sections -->
      <div class="dashboard-sections">
        <!-- Pending documents -->
        <div class="pending-documents">
          <div class="section-header">
            <h3>Documents en attente</h3>
            <Button 
              label="Voir tout" 
              icon="pi pi-file-pdf" 
              class="p-button-text" 
              @click="navigateTo('/encadrant/document-evaluation')" 
            />
          </div>
          
          <div v-if="!dashboardData?.pendingDocuments?.length" class="empty-state">
            <i class="pi pi-check-circle empty-icon"></i>
            <p>Tous les documents ont été révisés</p>
          </div>
          
          <div v-else class="document-list">
            <div 
              v-for="document in dashboardData.pendingDocuments" 
              :key="document.id" 
              class="document-item"
            >
              <div class="document-indicator" :class="'urgency-' + document.urgency.toLowerCase()">
                <i class="pi pi-file-pdf"></i>
              </div>
              <div class="document-details">
                <div class="document-title">{{ document.titre }}</div>
                <div class="document-binome">
                  {{ getStudentNames(document.binome) }}
                </div>
                <div class="document-info">
                  <span class="document-date">
                    <i class="pi pi-calendar"></i> {{ formatDate(document.dateSoumission) }}
                  </span>
                  <Tag 
                    :value="document.daysWaiting + ' jours'" 
                    :severity="getUrgencySeverity(document.urgency)" 
                  />
                </div>
              </div>
              <Button 
                icon="pi pi-pencil" 
                class="p-button-text p-button-rounded" 
                @click="navigateTo('/encadrant/document-evaluation')" 
              />
            </div>
          </div>
        </div>
        
        <!-- Upcoming soutenances -->
        <div class="upcoming-soutenances">
          <div class="section-header">
            <h3>Soutenances à venir</h3>
            <Button 
              label="Voir tout" 
              icon="pi pi-calendar" 
              class="p-button-text" 
              @click="navigateTo('/encadrant/soutenances')" 
            />
          </div>
          
          <div v-if="!dashboardData?.upcomingSoutenances?.length" class="empty-state">
            <i class="pi pi-calendar-times empty-icon"></i>
            <p>Aucune soutenance prévue</p>
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
                  <ProgressBar 
                    :value="soutenance.binome.projectProgress" 
                    :showValue="false" 
                    class="project-progress-bar" 
                  />
                </div>
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
    const response = await ApiService.get('/encadrant/dashboard');
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
    case 'PENDING_EVALUATIONS': return 'pi pi-pencil';
    case 'PENDING_REVIEWS': return 'pi pi-file-pdf';
    case 'UPCOMING_SOUTENANCE': return 'pi pi-calendar';
    default: return 'pi pi-info-circle';
  }
};

const getUrgencySeverity = (urgency) => {
  switch (urgency.toUpperCase()) {
    case 'HIGH': return 'danger';
    case 'MEDIUM': return 'warning';
    case 'LOW': return 'info';
    default: return 'info';
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
  return date.toLocaleDateString('fr-FR', { 
    day: 'numeric', 
    month: 'short', 
    hour: '2-digit', 
    minute: '2-digit' 
  });
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

.document-metrics {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 0.5rem;
}

.status-pill {
  flex: 1;
  min-width: 30%;
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

.status-pill .urgent {
  color: #f44336;
}

.grade-distribution {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.grade-item {
  flex: 1;
  min-width: 40%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.5rem;
  background-color: rgba(var(--primary-color-rgb), 0.05);
  border-radius: 0.5rem;
}

.grade-count {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--primary-color);
}

.grade-label {
  font-size: 0.75rem;
  color: var(--text-color-secondary);
}

.grade-count.urgent {
  color: #f44336;
}

.grade-label.urgent {
  color: #f44336;
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

.pending-documents,
.upcoming-soutenances {
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

/* Document list */
.document-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.document-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background-color: var(--surface-hover);
  transition: transform 0.2s;
}

.document-item:hover {
  transform: translateX(5px);
}

.document-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 3rem;
  height: 3rem;
  border-radius: 0.5rem;
}

.document-indicator.urgency-high {
  background-color: rgba(244, 67, 54, 0.1);
  color: #f44336;
}

.document-indicator.urgency-medium {
  background-color: rgba(255, 152, 0, 0.1);
  color: #ff9800;
}

.document-indicator.urgency-low {
  background-color: rgba(76, 175, 80, 0.1);
  color: #4caf50;
}

.document-details {
  flex: 1;
  min-width: 0;
}

.document-title {
  font-weight: 600;
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.document-binome {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
}

.document-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  font-size: 0.8rem;
}

.document-date {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--text-color-secondary);
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
  transition: transform 0.2s;
}

.soutenance-item:hover {
  transform: translateY(-2px);
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

.project-progress-bar {
  width: 100px;
  height: 6px;
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
.dark-mode .pending-documents,
.dark-mode .upcoming-soutenances {
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark-mode .status-pill {
  background-color: rgba(255, 255, 255, 0.08);
}

.dark-mode .document-item,
.dark-mode .soutenance-item {
  background-color: rgba(255, 255, 255, 0.03);
}

.dark-mode .grade-item {
  background-color: rgba(255, 255, 255, 0.05);
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
  
  .document-metrics {
    flex-direction: column;
  }
  
  .status-pill {
    min-width: 100%;
  }
  
  .grade-distribution {
    flex-direction: column;
  }
  
  .grade-item {
    min-width: 100%;
  }
}
</style>