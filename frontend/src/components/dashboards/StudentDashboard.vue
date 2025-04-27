<template>
  <div class="dashboard-container">
    <Toast position="top-right" />
    
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
      <!-- Welcome section - Full width -->
      <div class="welcome-card">
        <div class="welcome-info">
          <h1>Bienvenue, {{ dashboardData?.studentInfo?.prenom }} {{ dashboardData?.studentInfo?.nom }}</h1>
          <p class="welcome-subtitle">Année académique {{ dashboardData?.studentInfo?.anneeAcademique }} - {{ dashboardData?.studentInfo?.filiere }}</p>
        </div>
        <div class="student-avatar">
          <div class="avatar-circle">
            <span>{{ getInitials(dashboardData?.studentInfo?.prenom, dashboardData?.studentInfo?.nom) }}</span>
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
            :class="`severity-${reminder.severity}`"
          >
            <div class="reminder-icon">
              <i :class="getReminderIcon(reminder.type)"></i>
            </div>
            <div class="reminder-content">
              <h4 class="truncate-title">{{ reminder.title }}</h4>
              <p>{{ reminder.message }}</p>
              <div v-if="reminder.daysRemaining !== null" class="days-remaining">
                <i class="pi pi-clock"></i> Plus que {{ reminder.daysRemaining }} jours
              </div>
              <Button 
                v-if="reminder.actionUrl" 
                :label="reminder.type === 'RAPPORT' ? 'Déposer le rapport' : 'Action requise'"
                :icon="reminder.type === 'RAPPORT' ? 'pi pi-upload' : 'pi pi-arrow-right'"
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
      
      <!-- Project Overview Cards Grid -->
      <div class="metrics-grid">
        <!-- Binome info -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Binôme</div>
          </div>
          <div v-if="dashboardData?.binomeInfo?.hasBinome">
            <div v-if="dashboardData.binomeInfo.isSolo" class="metric-value">
              <i class="pi pi-user"></i> Travail individuel
            </div>
            <div v-else class="partner-info">
              <div class="metric-value">En binôme</div>
              <div class="metric-info">
                <div class="partner-details">
                  <div class="partner-name truncate-text">{{ dashboardData.binomeInfo.partner.prenom }} {{ dashboardData.binomeInfo.partner.nom }}</div>
                  <div class="email truncate-text">{{ dashboardData.binomeInfo.partner.email }}</div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="metric-info">
            <Tag severity="warning" value="Pas de binôme" />
            <Button 
              v-if="dashboardData?.quickActions?.canChooseBinome"
              label="Choisir un binôme" 
              icon="pi pi-users" 
              class="p-button-sm p-button-warning mt-2"
              @click="navigateTo('/etudiant/binome')"
            />
          </div>
          
        </div>
        
        <!-- Sujet info -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Sujet</div>
          </div>
          <div v-if="dashboardData?.sujetInfo?.hasSujet">
            <div class="metric-value word-wrap-text">{{ dashboardData.sujetInfo.titre }}</div>
            <div class="metric-info">
              <Tag :value="dashboardData.sujetInfo.theme" severity="info" />
              <p class="sujet-description word-wrap-text">{{ dashboardData.sujetInfo.description }}</p>
            </div>
          </div>
          <div v-else class="metric-info">
            <Tag severity="warning" value="Pas de sujet" />
            <Button 
              v-if="dashboardData?.quickActions?.canChooseSujet"
              label="Choisir un sujet" 
              icon="pi pi-book" 
              class="p-button-sm p-button-warning mt-2"
              @click="navigateTo('/etudiant/sujet')"
            />
          </div>
          
        </div>
        
        <!-- Encadrant info -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Encadrant</div>
          </div>
          <div v-if="dashboardData?.encadrantInfo?.hasEncadrant">
            <div class="metric-value">Superviseur assigné</div>
            <div class="metric-info">
              <div class="encadrant-info">
                <div class="encadrant-name truncate-text">
                  {{ dashboardData.encadrantInfo.prenom }} {{ dashboardData.encadrantInfo.nom }}
                </div>
                <div class="encadrant-email truncate-text">
                  <i class="pi pi-envelope"></i> {{ dashboardData.encadrantInfo.email }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="metric-info">
            <Tag severity="warning" value="Pas encore attribué" />
          </div>
          <div class="metric-footer">
            <Button v-if="dashboardData?.encadrantInfo?.hasEncadrant" icon="pi pi-envelope" class="p-button-rounded p-button-text" />
          </div>
        </div>
        
        <!-- Documents & Report card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Documents & Rapport</div>
            <div class="metric-badge">
              <span class="badge-value">{{ dashboardData?.documentStats?.submittedDocuments || 0 }}</span>
            </div>
          </div>
          
          <div class="document-metrics">
            <div class="status-pill">
              <span class="label">Soumis</span>
              <span class="value">{{ dashboardData?.documentStats?.submittedDocuments || 0 }}</span>
            </div>
            <div class="status-pill">
              <span class="label">Évalués</span>
              <span class="value">{{ dashboardData?.documentStats?.reviewedDocuments || 0 }}</span>
            </div>
          </div>
          
          <div class="metric-info">
            <h4 class="sub-title">Rapport final</h4>
            <div v-if="dashboardData?.documentStats?.hasSubmittedReport">
              <div class="status-badge">
                <i class="pi pi-check-circle"></i>
                <Tag severity="success" value="Rapport déposé" />
              </div>
              <div v-if="dashboardData.documentStats.reportCanBeModified" class="report-notice">
                <i class="pi pi-info-circle"></i> Vous pouvez encore modifier votre rapport
              </div>
              <div v-else class="report-notice locked">
                <i class="pi pi-lock"></i> Rapport verrouillé (moins de 3 jours avant la soutenance)
              </div>
            </div>
            <div v-else>
              <div class="status-badge">
                <i class="pi pi-exclamation-triangle"></i>
                <Tag severity="warning" value="Rapport non déposé" />
              </div>
            </div>
          </div>
          
          <div class="metric-footer">
            <Button 
              v-if="dashboardData?.quickActions?.canSubmitDocuments"
              label="Documents" 
              icon="pi pi-upload" 
              class="p-button-text p-button-sm"
              @click="navigateTo('/etudiant/documents')"
            />
            <Button 
              label="Rapport" 
              icon="pi pi-file-pdf" 
              class="p-button-text p-button-sm"
              @click="navigateTo('/etudiant/rapport')"
            />
          </div>
        </div>
        
        <!-- Soutenance card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Soutenance</div>
          </div>
          
          <div v-if="dashboardData?.soutenanceInfo?.isScheduled" class="metric-info">
            <div class="soutenance-date-display">
              <div class="date-calendar">
                <div class="date-day">{{ formatDay(dashboardData.soutenanceInfo.date) }}</div>
                <div class="date-month">{{ formatMonth(dashboardData.soutenanceInfo.date) }}</div>
                <div class="date-time">{{ dashboardData.soutenanceInfo.heure }}</div>
              </div>
              <div class="soutenance-location truncate-text">
                <i class="pi pi-map-marker"></i> {{ dashboardData.soutenanceInfo.salle }}
              </div>
            </div>
            
            <div class="jury-info">
              <h4 class="sub-title">Jury</h4>
              <ul class="jury-list">
                <li class="truncate-text"><i class="pi pi-user"></i> {{ dashboardData.soutenanceInfo.jury1 }}</li>
                <li class="truncate-text"><i class="pi pi-user"></i> {{ dashboardData.soutenanceInfo.jury2 }}</li>
              </ul>
            </div>
            
            <div v-if="dashboardData.soutenanceInfo.daysUntilSoutenance >= 0" class="countdown">
              <Tag 
                :severity="dashboardData.soutenanceInfo.daysUntilSoutenance <= 7 ? 'warning' : 'info'"
                :value="dashboardData.soutenanceInfo.daysUntilSoutenance <= 7 ? 'Imminent' : 'À venir'"
              />
              <div class="countdown-value">
                {{ dashboardData.soutenanceInfo.daysUntilSoutenance }} jours
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="pi pi-calendar-times empty-icon"></i>
            <p>Soutenance non planifiée</p>
          </div>
          
        </div>
        
        <!-- Notes card -->
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-title">Notes</div>
            <div v-if="dashboardData?.currentNoteStatus?.noteFinale" class="metric-badge">
              <span class="badge-value">{{ dashboardData?.currentNoteStatus?.noteFinale }}</span>
              <span class="badge-label">/20</span>
            </div>
          </div>
          
          <!-- Current year notes -->
          <div v-if="dashboardData?.currentNoteStatus?.allNotesPublished" class="metric-info">
            <h4 class="sub-title">Année {{ dashboardData.studentInfo.anneeAcademique }}</h4>
            <div class="grade-distribution">
              <div class="grade-item" v-if="dashboardData.currentNoteStatus.hasNoteRapport">
                <span class="grade-label">Rapport:</span>
                <span class="grade-count">{{ dashboardData.currentNoteStatus.noteRapport }}</span>
              </div>
              <div class="grade-item" v-if="dashboardData.currentNoteStatus.hasNoteSoutenance">
                <span class="grade-label">Soutenance:</span>
                <span class="grade-count">{{ dashboardData.currentNoteStatus.noteSoutenance }}</span>
              </div>
              <div class="grade-item" v-if="dashboardData.currentNoteStatus.hasNoteEncadrant">
                <span class="grade-label">Encadrant:</span>
                <span class="grade-count">{{ dashboardData.currentNoteStatus.noteEncadrant }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="pi pi-clock empty-icon"></i>
            <p>Notes en attente de publication</p>
          </div>
          
          <!-- Note history -->
          <div v-if="dashboardData?.noteHistory?.length > 1" class="note-history">
            <h4 class="sub-title">Historique</h4>
            <div class="history-list">
              <div v-for="note in dashboardData.noteHistory.slice(1)" :key="note.anneeAcademique" class="history-item">
                <div class="history-year">{{ note.anneeAcademique }}</div>
                <div class="history-note">{{ note.noteFinale }}/20 - {{ note.mention }}</div>
              </div>
            </div>
          </div>
          <div class="metric-footer">
            <Button 
              v-if="dashboardData?.currentNoteStatus?.allNotesPublished" 
              icon="pi pi-arrow-right" 
              class="p-button-rounded p-button-text" 
              @click="navigateTo('/etudiant/resultats')" 
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import ApiService from '@/services/ApiService';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';
import Toast from 'primevue/toast';

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
    const response = await ApiService.get('/etudiant/dashboard');
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

const formatDay = (dateString) => {
  const date = new Date(dateString);
  return date.getDate();
};

const formatMonth = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('fr-FR', { month: 'short' }).toUpperCase();
};

// Get initials for avatar
const getInitials = (firstName, lastName) => {
  if (!firstName && !lastName) return '?';
  return (firstName?.charAt(0) || '') + (lastName?.charAt(0) || '');
};

// Get appropriate icon for reminder type
const getReminderIcon = (type) => {
  switch (type) {
    case 'RAPPORT': return 'pi pi-file-pdf';
    case 'SOUTENANCE': return 'pi pi-calendar';
    case 'DOCUMENT': return 'pi pi-file';
    case 'BINOME': return 'pi pi-users';
    case 'SUJET': return 'pi pi-book';
    default: return 'pi pi-info-circle';
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

/* Word wrap fix */
.word-wrap-text {
  overflow-wrap: break-word;
  word-wrap: break-word;
  word-break: break-all;
  hyphens: auto;
  max-width: 100%;
}

/* Welcome card - top section */
.welcome-card {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
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

/* Main grid layout */
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

.metric-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

/* Binome and travail individuel */
.metric-card .pi-user {
  margin-right: 0.5rem;
}

.partner-info, .encadrant-info {
  margin-top: 0.5rem;
}

.partner-name, .encadrant-name {
  color: var(--text-color);
  font-weight: 600;
}

.email {
  color: var(--text-color-secondary);
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* Sujet card */
.sujet-description {
  color: var(--text-color-secondary);
  font-size: 0.85rem;
  margin-top: 0.5rem;
  max-height: 100px;
  overflow-y: auto;
}

/* Documents card */
.document-metrics {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
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

.sub-title {
  font-size: 0.9rem;
  color: var(--text-color);
  margin: 0.75rem 0 0.5rem;
  font-weight: normal;
}

/* Soutenance card */
.date-calendar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--primary-color);
  color: white;
  border-radius: 0.5rem;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.75rem;
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

.soutenance-location {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.5rem;
  color: var(--text-color-secondary);
}

.jury-info {
  margin-top: 1rem;
}

.jury-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.jury-list li {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background-color: var(--surface-hover);
  border-radius: 0.5rem;
  font-size: 0.85rem;
}

.countdown {
  margin-top: 1rem;
  text-align: center;
}

.countdown-value {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--primary-color);
  text-align: center;
  margin-top: 0.5rem;
}

/* Document status */
.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-badge i.pi-check-circle {
  color: #43b581;
}

.status-badge i.pi-exclamation-triangle {
  color: #faa61a;
}

.report-notice {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.5rem;
  background-color: rgba(67, 181, 129, 0.1);
  color: #43b581;
  border-radius: 0.5rem;
  font-size: 0.85rem;
}

.report-notice.locked {
  background-color: rgba(250, 166, 26, 0.1);
  color: #faa61a;
}

/* Notes card */
.grade-distribution {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.grade-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
}

.grade-label {
  color: var(--text-color-secondary);
}

.grade-count {
  font-weight: 600;
  color: var(--text-color);
}

.history-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  border-bottom: 1px solid var(--surface-border);
  font-size: 0.85rem;
}

.history-note {
  color: var(--primary-color);
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

/* Loading and empty states */
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

/* Text truncation */
.truncate-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.truncate-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

/* Dark mode adjustments */
.dark-mode .metric-card,
.dark-mode .welcome-card,
.dark-mode .reminder-card {
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark-mode .status-pill {
  background-color: rgba(255, 255, 255, 0.08);
}

.dark-mode .jury-list li {
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

  .student-avatar {
    margin-left: 0;
  }
  
  .metric-card {
    padding: 1rem;
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