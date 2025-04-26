<template>
  <div class="student-dashboard">
    <Toast position="top-right" />
    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
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
      <div class="welcome-section">
        <h1>Bienvenue, {{ dashboardData?.studentInfo?.prenom }} {{ dashboardData?.studentInfo?.nom }}</h1>
        <p class="welcome-subtitle">Année académique {{ dashboardData?.studentInfo?.anneeAcademique }} - {{ dashboardData?.studentInfo?.filiere }}</p>
      </div>
      
      <!-- Reminders section -->
      <div v-if="dashboardData?.reminders?.length > 0" class="reminders-section">
        <h3><i class="pi pi-exclamation-triangle"></i> Rappels importants</h3>
        <div class="reminders-grid">
          <div 
            v-for="reminder in dashboardData.reminders" 
            :key="reminder.type"
            class="reminder-card"
            :class="`severity-${reminder.severity}`"
          >
            <h4>{{ reminder.title }}</h4>
            <p>{{ reminder.message }}</p>
            <div v-if="reminder.daysRemaining !== null" class="days-remaining">
              Plus que {{ reminder.daysRemaining }} jours
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
      
      <!-- Main content grid -->
      <div class="main-content-grid">
        <!-- Project overview card -->
        <div class="dashboard-card project-overview">
          <h3><i class="pi pi-book"></i> Mon Projet</h3>
          
          <!-- Binome info -->
          <div class="section">
            <h4>Binôme</h4>
            <div v-if="dashboardData?.binomeInfo?.hasBinome">
              <div v-if="dashboardData.binomeInfo.isSolo" class="info-badge solo">
                <i class="pi pi-user"></i> Travail individuel
              </div>
              <div v-else class="partner-info">
                <div class="info-badge">
                  <i class="pi pi-users"></i> En binôme avec:
                </div>
                <div class="partner-details">
                  {{ dashboardData.binomeInfo.partner.prenom }} {{ dashboardData.binomeInfo.partner.nom }}
                  <br>
                  <span class="email">{{ dashboardData.binomeInfo.partner.email }}</span>
                </div>
              </div>
            </div>
            <div v-else>
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
          <div class="section">
            <h4>Sujet</h4>
            <div v-if="dashboardData?.sujetInfo?.hasSujet">
              <div class="sujet-title">{{ dashboardData.sujetInfo.titre }}</div>
              <div class="sujet-theme">
                <Tag :value="dashboardData.sujetInfo.theme" severity="info" />
              </div>
              <p class="sujet-description">{{ dashboardData.sujetInfo.description }}</p>
            </div>
            <div v-else>
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
          <div class="section">
            <h4>Encadrant</h4>
            <div v-if="dashboardData?.encadrantInfo?.hasEncadrant">
              <div class="encadrant-info">
                <div class="encadrant-name">
                  {{ dashboardData.encadrantInfo.prenom }} {{ dashboardData.encadrantInfo.nom }}
                </div>
                <div class="encadrant-email">
                  <i class="pi pi-envelope"></i> {{ dashboardData.encadrantInfo.email }}
                </div>
              </div>
            </div>
            <div v-else>
              <Tag severity="warning" value="Pas encore attribué" />
            </div>
          </div>
        </div>
        
        <!-- Documents & Report card -->
        <div class="dashboard-card documents-card">
          <h3><i class="pi pi-file"></i> Documents & Rapport</h3>
          
          <div class="documents-stats">
            <div class="stat-item">
              <div class="stat-value">{{ dashboardData?.documentStats?.submittedDocuments || 0 }}</div>
              <div class="stat-label">Documents soumis</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ dashboardData?.documentStats?.reviewedDocuments || 0 }}</div>
              <div class="stat-label">Documents évalués</div>
            </div>
          </div>
          
          <div class="report-status">
            <h4>Rapport final</h4>
            <div v-if="dashboardData?.documentStats?.hasSubmittedReport">
              <Tag severity="success" value="Rapport déposé" />
              <div v-if="dashboardData.documentStats.reportCanBeModified" class="report-notice">
                <i class="pi pi-info-circle"></i> Vous pouvez encore modifier votre rapport
              </div>
              <div v-else class="report-notice locked">
                <i class="pi pi-lock"></i> Rapport verrouillé (moins de 3 jours avant la soutenance)
              </div>
            </div>
            <div v-else>
              <Tag severity="warning" value="Rapport non déposé" />
            </div>
          </div>
          
          <div class="document-actions">
            <Button 
              v-if="dashboardData?.quickActions?.canSubmitDocuments"
              label="Soumettre des documents" 
              icon="pi pi-upload" 
              class="p-button-sm"
              @click="navigateTo('/etudiant/documents')"
            />
            <Button 
              v-if="dashboardData?.quickActions?.canSubmitReport"
              label="Gérer le rapport" 
              icon="pi pi-file-pdf" 
              class="p-button-sm p-button-primary"
              @click="navigateTo('/etudiant/rapport')"
            />
          </div>
        </div>
        
        <!-- Soutenance card -->
        <div class="dashboard-card soutenance-card">
          <h3><i class="pi pi-calendar"></i> Soutenance</h3>
          
          <div v-if="dashboardData?.soutenanceInfo?.isScheduled">
            <div class="soutenance-date">
              <div class="date-display">
                <div class="day">{{ formatDay(dashboardData.soutenanceInfo.date) }}</div>
                <div class="month">{{ formatMonth(dashboardData.soutenanceInfo.date) }}</div>
              </div>
              <div class="date-details">
                <div class="time">{{ dashboardData.soutenanceInfo.heure }}</div>
                <div class="location">{{ dashboardData.soutenanceInfo.salle }}</div>
              </div>
            </div>
            
            <div class="jury-info">
              <h4>Jury</h4>
              <ul class="jury-list">
                <li><i class="pi pi-user"></i> {{ dashboardData.soutenanceInfo.jury1 }}</li>
                <li><i class="pi pi-user"></i> {{ dashboardData.soutenanceInfo.jury2 }}</li>
              </ul>
            </div>
            
            <div v-if="dashboardData.soutenanceInfo.daysUntilSoutenance >= 0" class="countdown">
              <Tag 
                :severity="dashboardData.soutenanceInfo.daysUntilSoutenance <= 7 ? 'warning' : 'info'"
                :value="`Dans ${dashboardData.soutenanceInfo.daysUntilSoutenance} jours`"
              />
            </div>
          </div>
          <div v-else class="no-soutenance">
            <i class="pi pi-calendar-times"></i>
            <p>Soutenance non planifiée</p>
          </div>
        </div>
        
        <!-- Notes card -->
        <div class="dashboard-card notes-card">
          <h3><i class="pi pi-star"></i> Notes</h3>
          
          <!-- Current year notes -->
          <div v-if="dashboardData?.currentNoteStatus?.allNotesPublished">
            <h4>Année {{ dashboardData.studentInfo.anneeAcademique }}</h4>
            <div class="notes-grid">
              <div class="note-item" v-if="dashboardData.currentNoteStatus.hasNoteRapport">
                <div class="note-label">Rapport</div>
                <div class="note-value">{{ dashboardData.currentNoteStatus.noteRapport }}/20</div>
              </div>
              <div class="note-item" v-if="dashboardData.currentNoteStatus.hasNoteSoutenance">
                <div class="note-label">Soutenance</div>
                <div class="note-value">{{ dashboardData.currentNoteStatus.noteSoutenance }}/20</div>
              </div>
              <div class="note-item" v-if="dashboardData.currentNoteStatus.hasNoteEncadrant">
                <div class="note-label">Encadrant</div>
                <div class="note-value">{{ dashboardData.currentNoteStatus.noteEncadrant }}/20</div>
              </div>
              <div class="note-item final">
                <div class="note-label">Note finale</div>
                <div class="note-value">{{ dashboardData.currentNoteStatus.noteFinale }}/20</div>
              </div>
            </div>
          </div>
          <div v-else class="notes-pending">
            <i class="pi pi-clock"></i>
            <p>Notes en attente de publication</p>
          </div>
          
          <!-- Note history -->
          <div v-if="dashboardData?.noteHistory?.length > 1" class="note-history">
            <h4>Historique</h4>
            <div v-for="note in dashboardData.noteHistory.slice(1)" :key="note.anneeAcademique" class="history-item">
              <div class="history-year">{{ note.anneeAcademique }}</div>
              <div class="history-note">{{ note.noteFinale }}/20 - {{ note.mention }}</div>
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
</script>

<style scoped>
.student-dashboard {
  padding: 1.5rem;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.error-container i {
  font-size: 3rem;
  color: var(--red-500);
  margin-bottom: 1rem;
}

.welcome-section {
  margin-bottom: 2rem;
}

.welcome-section h1 {
  margin: 0;
  font-size: 2.5rem;
  color: var(--text-color);
}

.welcome-subtitle {
  margin-top: 0.5rem;
  color: var(--text-color-secondary);
  font-size: 1.1rem;
}

.reminders-section {
  margin-bottom: 2rem;
}

.reminders-section h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.reminders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.reminder-card {
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  border-left: 4px solid;
}

.reminder-card.severity-critical {
  border-left-color: var(--red-500);
  background-color: var(--red-50);
}

.reminder-card.severity-warning {
  border-left-color: var(--yellow-500);
  background-color: var(--yellow-50);
}

.reminder-card.severity-info {
  border-left-color: var(--blue-500);
  background-color: var(--blue-50);
}

.reminder-card h4 {
  margin: 0 0 0.5rem;
  color: var(--text-color);
}

.reminder-card p {
  margin: 0 0 1rem;
  color: var(--text-color-secondary);
}

.days-remaining {
  font-weight: 600;
  margin-bottom: 1rem;
}

.main-content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
}

.dashboard-card {
  background-color: var(--surface-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.dashboard-card h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 1.5rem;
  color: var(--primary-color);
}

.section {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--surface-border);
}

.section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section h4 {
  margin: 0 0 0.75rem;
  color: var(--text-color);
}

.info-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  background-color: var(--surface-ground);
  border-radius: 1rem;
  margin-bottom: 0.5rem;
}

.info-badge.solo {
  background-color: var(--blue-50);
  color: var(--blue-700);
}

.partner-details {
  margin-left: 1rem;
}

.partner-details .email {
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}

.sujet-title {
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.sujet-theme {
  margin-bottom: 0.5rem;
}

.sujet-description {
  color: var(--text-color-secondary);
  margin: 0;
}

.encadrant-name {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.encadrant-email {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}

.documents-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background-color: var(--surface-ground);
  border-radius: 8px;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--primary-color);
}

.stat-label {
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}

.report-status {
  margin-bottom: 1.5rem;
}

.report-notice {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.5rem;
  background-color: var(--blue-50);
  color: var(--blue-700);
  border-radius: 4px;
  font-size: 0.9rem;
}

.report-notice.locked {
  background-color: var(--yellow-50);
  color: var(--yellow-700);
}

.document-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.soutenance-date {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.date-display {
  text-align: center;
  padding: 1rem;
  background-color: var(--primary-color);
  color: white;
  border-radius: 8px;
  min-width: 80px;
}

.date-display .day {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.date-display .month {
  font-size: 0.9rem;
  text-transform: uppercase;
}

.date-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.date-details .time {
  font-size: 1.25rem;
  font-weight: 600;
}

.date-details .location {
  color: var(--text-color-secondary);
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
}

.countdown {
  margin-top: 1rem;
  text-align: center;
}

.no-soutenance {
  text-align: center;
  padding: 2rem;
  color: var(--text-color-secondary);
}

.no-soutenance i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.note-item {
  text-align: center;
  padding: 1rem;
  background-color: var(--surface-ground);
  border-radius: 8px;
}

.note-item.final {
  background-color: var(--primary-color);
  color: white;
}

.note-label {
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.note-value {
  font-size: 1.25rem;
  font-weight: 700;
}

.notes-pending {
  text-align: center;
  padding: 2rem;
  color: var(--text-color-secondary);
}

.notes-pending i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.note-history {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}

.history-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  border-bottom: 1px solid var(--surface-border);
}

.history-item:last-child {
  border-bottom: none;
}

.history-year {
  font-weight: 600;
}

.mt-2 {
  margin-top: 0.5rem;
}

@media (max-width: 768px) {
  .main-content-grid {
    grid-template-columns: 1fr;
  }
  
  .documents-stats {
    grid-template-columns: 1fr;
  }
  
  .notes-grid {
    grid-template-columns: 1fr;
  }
}
</style>