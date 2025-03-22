<template>
  <div class="soutenance-management">
    <Toast />
    <ConfirmDialog />
    
    <div class="header-section">
      <div class="title-row">
        <h1 class="page-title">Gestion des Soutenances</h1>
      </div>
      <div class="filter-row">
        <Dropdown 
          v-model="selectedFiliere" 
          :options="filieres" 
          optionLabel="nom" 
          optionValue="id"
          placeholder="Sélectionner une filière" 
          class="filiere-dropdown"
          @change="handleFiliereChange" 
        />
      </div>
    </div>
    
    <div class="action-bar">
      <div class="search-row">
        <span class="p-input-icon-left search-container">
          <i class="pi pi-search" />
          <InputText 
            v-model="searchQuery" 
            placeholder="Rechercher un binôme..." 
            class="search-input"
          />
        </span>
      </div>
    </div>

    <!-- Binomes Table with Soutenance Status -->
    <Card class="table-card">
      <template #content>
        <DataTable 
          :value="filteredBinomes" 
          :loading="loading" 
          responsiveLayout="scroll"
          stripedRows 
          :paginator="filteredBinomes.length > 10" 
          :rows="10" 
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
          :rowsPerPageOptions="[10, 20, 50]"
          currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} binômes"
          emptyMessage="Aucun binôme trouvé"
        >
          <Column field="etudiant1" header="Étudiant 1" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ getStudentName(slotProps.data.etudiant1) }}
            </template>
          </Column>
          <Column field="etudiant2" header="Étudiant 2" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ slotProps.data.etudiant2 ? getStudentName(slotProps.data.etudiant2) : '-' }}
            </template>
          </Column>
          <Column field="encadrant" header="Encadrant" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ getEncadrantName(slotProps.data.encadrant) }}
            </template>
          </Column>
          <Column field="sujet.titre" header="Sujet" sortable style="min-width: 12rem"></Column>
          <Column field="soutenance" header="Status" sortable style="min-width: 10rem">
            <template #body="slotProps">
              <Tag v-if="slotProps.data.soutenance" severity="success" value="Programmée"></Tag>
              <Tag v-else severity="warning" value="Non programmée"></Tag>
            </template>
          </Column>
          <Column field="soutenance" header="Date & Heure" sortable style="min-width: 12rem">
            <template #body="slotProps">
              <template v-if="slotProps.data.soutenance">
                {{ formatDate(slotProps.data.soutenance.date) }} à {{ slotProps.data.soutenance.heure }}
              </template>
              <span v-else>-</span>
            </template>
          </Column>
          <Column field="soutenance" header="Salle & Jury" sortable style="min-width: 15rem">
            <template #body="slotProps">
              <template v-if="slotProps.data.soutenance">
                Salle: {{ slotProps.data.soutenance.salle.nom }} <br>
                Jury: {{ getJuryName(slotProps.data.soutenance.jury1) }}, {{ getJuryName(slotProps.data.soutenance.jury2) }}
              </template>
              <span v-else>-</span>
            </template>
          </Column>
          <Column header="Actions" :exportable="false" style="min-width: 8rem">
            <template #body="slotProps">
              <div class="action-buttons">
                <Button 
                  v-if="slotProps.data.soutenance"
                  icon="pi pi-pencil" 
                  class="p-button-rounded p-button-outlined p-button-info mr-2" 
                  @click="openEditModal(slotProps.data)" 
                  tooltip="Modifier"
                  tooltipOptions="top"
                />
                <Button 
                  v-else
                  icon="pi pi-calendar-plus" 
                  class="p-button-rounded p-button-outlined p-button-success mr-2" 
                  @click="openScheduleModal(slotProps.data)" 
                  tooltip="Programmer"
                  tooltipOptions="top"
                />
                <Button 
                  v-if="slotProps.data.soutenance"
                  icon="pi pi-trash" 
                  class="p-button-rounded p-button-outlined p-button-danger" 
                  @click="confirmCancelSoutenance(slotProps.data)" 
                  tooltip="Annuler"
                  tooltipOptions="top"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>
  
    <!-- Schedule/Edit Soutenance Dialog -->
    <Dialog 
      v-model:visible="showSoutenanceModal" 
      :header="currentBinome?.soutenance ? 'Modifier la Soutenance' : 'Programmer une Soutenance'" 
      :modal="true" 
      class="soutenance-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div v-if="currentBinome" class="field-container">
        <div class="binome-info">
          <h3>Binôme</h3>
          <p><strong>Étudiant 1:</strong> {{ getStudentName(currentBinome.etudiant1) }}</p>
          <p v-if="currentBinome.etudiant2"><strong>Étudiant 2:</strong> {{ getStudentName(currentBinome.etudiant2) }}</p>
          <p><strong>Encadrant:</strong> {{ getEncadrantName(currentBinome.encadrant) }}</p>
          <p><strong>Sujet:</strong> {{ currentBinome.sujet.titre }}</p>
        </div>
        
        <div class="p-field">
          <label for="date">Date <span class="required">*</span></label>
          <Calendar 
            id="date" 
            v-model="soutenanceForm.date" 
            dateFormat="dd/mm/yy"
            class="w-full" 
            placeholder="Date de soutenance"
            required
            :minDate="today"
            @date-select="checkForConflicts"
          />
        </div>
        
        <div class="p-field">
          <label for="heure">Heure <span class="required">*</span></label>
          <Calendar 
            id="heure" 
            v-model="soutenanceForm.heure" 
            timeOnly 
            hourFormat="24" 
            class="w-full" 
            placeholder="Heure de soutenance"
            required
            @date-select="checkForConflicts"
          />
        </div>
        
        <div class="p-field">
          <label for="salle">Salle <span class="required">*</span></label>
          <Dropdown 
            id="salle" 
            v-model="soutenanceForm.salleId" 
            :options="availableSalles" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner une salle" 
            class="w-full"
            required
            @change="checkForConflicts"
          />
          <small v-if="conflicts.salle" class="p-error">{{ conflicts.salle }}</small>
        </div>
        
        <div class="p-field">
          <label for="jury1">Jury 1 <span class="required">*</span></label>
          <Dropdown 
            id="jury1" 
            v-model="soutenanceForm.jury1Id" 
            :options="jurys" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner le premier membre du jury" 
            class="w-full"
            required
            :filter="true"
            filterPlaceholder="Rechercher un membre du jury"
            @change="checkForConflicts"
          />
          <small v-if="conflicts.jury1" class="p-error">{{ conflicts.jury1 }}</small>
        </div>
        
        <div class="p-field">
          <label for="jury2">Jury 2 <span class="required">*</span></label>
          <Dropdown 
            id="jury2" 
            v-model="soutenanceForm.jury2Id" 
            :options="jurys.filter(j => j.id !== soutenanceForm.jury1Id)" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner le second membre du jury" 
            class="w-full"
            required
            :filter="true"
            filterPlaceholder="Rechercher un membre du jury"
            @change="checkForConflicts"
          />
          <small v-if="conflicts.jury2" class="p-error">{{ conflicts.jury2 }}</small>
        </div>
      </div>
      
      <template #footer>
        <Button 
          label="Annuler" 
          icon="pi pi-times" 
          class="p-button-text" 
          @click="closeSoutenanceModal" 
        />
        <Button 
          label="Enregistrer" 
          icon="pi pi-check" 
          class="p-button-primary" 
          @click="saveSoutenance" 
          :loading="submitting"
          :disabled="hasConflicts"
        />
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import ApiService from '@/services/ApiService';

// Import PrimeVue components
import Toast from 'primevue/toast';
import ConfirmDialog from 'primevue/confirmdialog';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import Calendar from 'primevue/calendar';
import Tag from 'primevue/tag';

// Component state
const binomes = ref([]);
const soutenances = ref([]);
const filieres = ref([]);
const jurys = ref([]);
const salles = ref([]);
const selectedFiliere = ref(null);
const today = ref(new Date());
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref('');

// Current binome and soutenance state
const currentBinome = ref(null);
const showSoutenanceModal = ref(false);

// Form state
const soutenanceForm = ref({
  id: null,
  date: null,
  heure: null,
  salleId: null,
  jury1Id: null,
  jury2Id: null
});

// Conflict tracking
const conflicts = ref({
  salle: null,
  jury1: null,
  jury2: null
});

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed properties
const filteredBinomes = computed(() => {
  if (!binomes.value.length) return [];
  
  let filtered = binomes.value;
  
  // Filter by selected filiere
  if (selectedFiliere.value) {
    filtered = filtered.filter(binome => {
      return binome.filiereId === selectedFiliere.value;
    });
  }
  
  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(binome => {
      return (
        (binome.etudiant1.nom.toLowerCase().includes(query) || binome.etudiant1.prenom.toLowerCase().includes(query)) ||
        (binome.etudiant2 && (binome.etudiant2.nom.toLowerCase().includes(query) || binome.etudiant2.prenom.toLowerCase().includes(query))) ||
        (binome.encadrant.nom.toLowerCase().includes(query) || binome.encadrant.prenom.toLowerCase().includes(query)) ||
        (binome.sujet.titre.toLowerCase().includes(query))
      );
    });
  }
  
  return filtered;
});

const availableSalles = computed(() => {
  return salles.value;
});

const hasConflicts = computed(() => {
  return conflicts.value.salle || conflicts.value.jury1 || conflicts.value.jury2;
});

// Fetch data on component mount
onMounted(async () => {
  await Promise.all([
    fetchBinomes(),
    fetchSoutenances(),
    fetchFilieres(),
    fetchJurys(),
    fetchSalles()
  ]);
  
  // Set default selected filiere
  if (filieres.value.length > 0) {
    selectedFiliere.value = filieres.value[0].id;
  }
  
  // Associate soutenances with binomes
  associateSoutenancesWithBinomes();
});

// Data loading functions
async function fetchBinomes() {
  loading.value = true;
  try {
    const response = await ApiService.get('/binome');
    
    // Process binomes to add filiere info
    binomes.value = await Promise.all(response.map(async (binome) => {
      // Find etudiant's filiere
      let filiereId = null;
      let filiereName = null;
      
      try {
        const etudiantResponse = await ApiService.get(`/etudiant/utilisateur/${binome.etudiant1.id}`);
        if (etudiantResponse && etudiantResponse.filiere) {
          filiereId = etudiantResponse.filiere.id;
          filiereName = etudiantResponse.filiere.nom;
        }
      } catch (error) {
        console.error("Error fetching etudiant info:", error);
      }
      
      return {
        ...binome,
        filiereId,
        filiereName,
        soutenance: null // Will be populated later
      };
    }));
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des binômes');
  } finally {
    loading.value = false;
  }
}

async function fetchSoutenances() {
  try {
    const response = await ApiService.get('/soutenance');
    soutenances.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des soutenances');
  }
}

async function fetchFilieres() {
  try {
    const response = await ApiService.get('/filiere');
    filieres.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des filières');
  }
}

async function fetchJurys() {
  try {
    const response = await ApiService.get('/utilisateurs', { role: 'JURY' });
    jurys.value = response.map(jury => ({
      ...jury,
      fullName: `${jury.prenom} ${jury.nom}`
    }));
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des jurys');
  }
}

async function fetchSalles() {
  try {
    const response = await ApiService.get('/salle');
    salles.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des salles');
  }
}

// Associate soutenances with binomes
function associateSoutenancesWithBinomes() {
  if (!binomes.value.length || !soutenances.value.length) return;
  
  binomes.value.forEach(binome => {
    const matchingSoutenance = soutenances.value.find(s => s.binome.id === binome.id);
    if (matchingSoutenance) {
      binome.soutenance = matchingSoutenance;
    }
  });
}

// Event handlers
function handleFiliereChange() {
  // Nothing specific needed here as filteredBinomes already uses selectedFiliere
}

// Helper methods
function getStudentName(student) {
  if (!student) return 'N/A';
  return `${student.prenom} ${student.nom}`;
}

function getEncadrantName(encadrant) {
  if (!encadrant) return 'N/A';
  return `${encadrant.prenom} ${encadrant.nom}`;
}

function getJuryName(jury) {
  if (!jury) return 'N/A';
  return `${jury.prenom} ${jury.nom}`;
}

function formatDate(date) {
  if (!date) return 'N/A';
  
  // If it's already a string in a date format, return it formatted
  if (typeof date === 'string') {
    return new Date(date).toLocaleDateString('fr-FR');
  }
  
  // If it's a Date object
  if (date instanceof Date) {
    return date.toLocaleDateString('fr-FR');
  }
  
  return 'N/A';
}

// Format time from ISO or Date object to HH:MM
function formatTime(time) {
  if (!time) return '';
  
  if (time instanceof Date) {
    return time.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
  
  // Handle string time formats
  if (typeof time === 'string') {
    // If it's an ISO time string with date
    if (time.includes('T')) {
      return new Date(time).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }
    
    // If it's already in HH:MM format
    if (time.includes(':')) {
      return time;
    }
  }
  
  return '';
}

// Conflict checking
function checkForConflicts() {
  resetConflicts();
  
  if (!soutenanceForm.value.date || !soutenanceForm.value.heure) {
    return;
  }
  
  const formDate = soutenanceForm.value.date;
  const formHeure = soutenanceForm.value.heure;
  
  // Format date and time for comparison
  const selectedDate = formatDateForApi(formDate);
  const selectedTime = formatTimeForApi(formHeure);
  
  // Check for conflicts with existing soutenances
  soutenances.value.forEach(soutenance => {
    // Skip the current soutenance being edited
    if (soutenanceForm.value.id && soutenance.id === soutenanceForm.value.id) {
      return;
    }
    
    const soutenanceDate = typeof soutenance.date === 'string' ? soutenance.date : formatDateForApi(soutenance.date);
    const soutenanceTime = typeof soutenance.heure === 'string' ? soutenance.heure : formatTimeForApi(soutenance.heure);
    
    // Only check if dates match
    if (soutenanceDate === selectedDate && soutenanceTime === selectedTime) {
      // Check salle conflict
      if (soutenanceForm.value.salleId && soutenance.salle.id === soutenanceForm.value.salleId) {
        conflicts.value.salle = `La salle est déjà réservée à cette date et heure pour une autre soutenance`;
      }
      
      // Check jury1 conflict
      if (soutenanceForm.value.jury1Id && 
          (soutenance.jury1.id === soutenanceForm.value.jury1Id || soutenance.jury2.id === soutenanceForm.value.jury1Id)) {
        conflicts.value.jury1 = `Ce membre du jury est déjà assigné à une autre soutenance à cette date et heure`;
      }
      
      // Check jury2 conflict
      if (soutenanceForm.value.jury2Id && 
          (soutenance.jury1.id === soutenanceForm.value.jury2Id || soutenance.jury2.id === soutenanceForm.value.jury2Id)) {
        conflicts.value.jury2 = `Ce membre du jury est déjà assigné à une autre soutenance à cette date et heure`;
      }
    }
  });
  
  // Check if jury1 and jury2 are the same
  if (soutenanceForm.value.jury1Id && 
      soutenanceForm.value.jury2Id && 
      soutenanceForm.value.jury1Id === soutenanceForm.value.jury2Id) {
    conflicts.value.jury2 = `Les deux membres du jury doivent être différents`;
  }
}

function resetConflicts() {
  conflicts.value = {
    salle: null,
    jury1: null,
    jury2: null
  };
}

// Modal functions
function openScheduleModal(binome) {
  currentBinome.value = binome;
  
  // Reset form
  soutenanceForm.value = {
    id: null,
    date: new Date(),
    heure: new Date(),
    salleId: null,
    jury1Id: null,
    jury2Id: null
  };
  
  resetConflicts();
  showSoutenanceModal.value = true;
}

function openEditModal(binome) {
  currentBinome.value = binome;
  const soutenance = binome.soutenance;
  
  // Convert string date/time to Date objects if needed
  let dateObj = soutenance.date;
  if (typeof soutenance.date === 'string') {
    dateObj = new Date(soutenance.date);
  }
  
  let timeObj = soutenance.heure;
  if (typeof soutenance.heure === 'string') {
    const [hours, minutes] = soutenance.heure.split(':');
    timeObj = new Date();
    timeObj.setHours(parseInt(hours, 10), parseInt(minutes, 10));
  }
  
  // Populate form
  soutenanceForm.value = {
    id: soutenance.id,
    date: dateObj,
    heure: timeObj,
    salleId: soutenance.salle.id,
    jury1Id: soutenance.jury1.id,
    jury2Id: soutenance.jury2.id
  };
  
  resetConflicts();
  showSoutenanceModal.value = true;
}

function closeSoutenanceModal() {
  showSoutenanceModal.value = false;
  currentBinome.value = null;
  resetConflicts();
}

// CRUD operations
async function saveSoutenance() {
  if (!validateSoutenanceForm()) return;
  
  submitting.value = true;
  try {
    // Prepare data
    const formattedDate = formatDateForApi(soutenanceForm.value.date);
    const formattedTime = formatTimeForApi(soutenanceForm.value.heure);
    
    const soutenanceData = {
      date: formattedDate,
      heure: formattedTime,
      salle: { id: soutenanceForm.value.salleId },
      binome: { id: currentBinome.value.id },
      jury1: { id: soutenanceForm.value.jury1Id },
      jury2: { id: soutenanceForm.value.jury2Id }
    };
    
    let response;
    if (soutenanceForm.value.id) {
      // Update existing soutenance
      soutenanceData.id = soutenanceForm.value.id;
      response = await ApiService.put(`/api/soutenance/${soutenanceForm.value.id}`, soutenanceData);
      
      // Update in local collections
      const index = soutenances.value.findIndex(s => s.id === soutenanceForm.value.id);
      if (index !== -1) {
        soutenances.value[index] = response;
      }
      
      toast.add({
        severity: 'success',
        summary: 'Soutenance modifiée',
        detail: 'La soutenance a été mise à jour avec succès',
        life: 3000
      });
    } else {
      // Create new soutenance
      response = await ApiService.post('/api/soutenance', soutenanceData);
      soutenances.value.push(response);
      
      toast.add({
        severity: 'success',
        summary: 'Soutenance programmée',
        detail: 'La soutenance a été programmée avec succès',
        life: 3000
      });
    }
    
    // Update the binome's soutenance
    if (currentBinome.value) {
      currentBinome.value.soutenance = response;
    }
    
    // Close the modal
    closeSoutenanceModal();
  } catch (error) {
    handleApiError(error, 'Erreur lors de l\'enregistrement de la soutenance');
  } finally {
    submitting.value = false;
  }
}

function confirmCancelSoutenance(binome) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir annuler la soutenance du binôme ${getStudentName(binome.etudiant1)}${binome.etudiant2 ? ` et ${getStudentName(binome.etudiant2)}` : ''}?`,
    header: 'Confirmation d\'annulation',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => cancelSoutenance(binome),
    reject: () => {/* do nothing */}
  });
}

async function cancelSoutenance(binome) {
  if (!binome.soutenance) return;
  
  try {
    await ApiService.delete(`/api/soutenance/${binome.soutenance.id}`);
    
    // Update local data
    soutenances.value = soutenances.value.filter(s => s.id !== binome.soutenance.id);
    binome.soutenance = null;
    
    toast.add({
      severity: 'success',
      summary: 'Soutenance annulée',
      detail: 'La soutenance a été annulée avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de l\'annulation de la soutenance');
  }
}

// Validation and formatting helpers
function validateSoutenanceForm() {
  if (hasConflicts) {
    showValidationError('Il y a des conflits de programmation. Veuillez les résoudre avant de continuer.');
    return false;
  }
  
  if (!soutenanceForm.value.date) {
    showValidationError('La date est obligatoire');
    return false;
  }
  
  if (!soutenanceForm.value.heure) {
    showValidationError('L\'heure est obligatoire');
    return false;
  }
  
  if (!soutenanceForm.value.salleId) {
    showValidationError('La salle est obligatoire');
    return false;
  }
  
  if (!soutenanceForm.value.jury1Id) {
    showValidationError('Le premier membre du jury est obligatoire');
    return false;
  }
  
  if (!soutenanceForm.value.jury2Id) {
    showValidationError('Le second membre du jury est obligatoire');
    return false;
  }
  
  return true;
}

// Format date to YYYY-MM-DD for API
function formatDateForApi(date) {
  if (!date) return null;
  
  if (date instanceof Date) {
    return date.toISOString().split('T')[0];
  }
  
  return date;
}

// Format time to HH:MM for API
function formatTimeForApi(time) {
  if (!time) return null;
  
  if (time instanceof Date) {
    return time.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
  
  return time;
}

// Error handling
function showValidationError(message) {
  toast.add({
    severity: 'warn',
    summary: 'Validation',
    detail: message,
    life: 5000
  });
}

function handleApiError(error, defaultMessage) {
  console.error(defaultMessage, error);
  
  let errorMessage = defaultMessage;
  if (error.response && error.response.data && error.response.data.message) {
    errorMessage = error.response.data.message;
  }
  
  toast.add({
    severity: 'error',
    summary: 'Erreur',
    detail: errorMessage,
    life: 5000
  });
}
</script>

<style scoped>
.soutenance-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

.header-section {
  margin-bottom: 2rem;
}

.title-row {
  margin-bottom: 1rem;
}

.page-title {
  color: var(--primary-color);
  font-size: 2rem;
  margin: 0;
}

.filter-row {
  display: flex;
}

.filiere-dropdown {
  min-width: 250px;
}

.action-bar {
  display: flex;
  flex-direction: column;
  margin-bottom: 1.5rem;
}

.search-row {
  width: 100%;
  margin-bottom: 0.75rem;
}

.search-container {
  width: 100%;
  display: block;
}

.search-input {
  width: 100%;
}

.table-card {
  margin-bottom: 2rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.field-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.p-field {
  margin-bottom: 0;
}

.p-field label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.required {
  color: red;
}

.binome-info {
  background-color: var(--surface-ground);
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 0.5rem;
}

.binome-info h3 {
  margin-top: 0;
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
  color: var(--primary-color);
}

.binome-info p {
  margin: 0.3rem 0;
}

.p-error {
  display: block;
  margin-top: 0.25rem;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .soutenance-management {
    padding: 1rem;
  }
  
  .title-row, .filter-row {
    flex-direction: column;
  }
  
  .filiere-dropdown {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: row;
  }
}
</style>