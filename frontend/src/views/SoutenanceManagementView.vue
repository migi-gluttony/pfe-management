<template>
  <div class="soutenance-management">
    <Toast />
    <ConfirmDialog />
    <!-- Add the UserInfoHeader component -->
    <UserInfoHeader 
      searchPlaceholder="Rechercher un binôme..." 
      :initialSearchValue="searchQuery"
      @search="handleHeaderSearch" 
    />
    <div class="header-section">
  <div class="title-filter-group">
    <h1 class="page-title">Gestion des Soutenances</h1>
    <Dropdown 
      v-model="selectedFiliere" 
      :options="filieres" 
      optionLabel="nom" 
      optionValue="id"
      placeholder="Filtrer par filière" 
      class="filter-dropdown"
      @change="handleFiliereChange" 
    />
  </div>
  <div class="action-buttons">
    <!-- No action button in this view, but space is reserved -->
  </div>
</div>
    <div class="action-bar">
      
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
          <Column field="binome.etudiant1" header="Étudiant 1" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ getStudentName(slotProps.data.binome?.etudiant1) }}
            </template>
          </Column>
          <Column field="binome.etudiant2" header="Étudiant 2" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ slotProps.data.binome?.etudiant2 ? getStudentName(slotProps.data.binome.etudiant2) : '-' }}
            </template>
          </Column>
          <Column field="binome.encadrant" header="Encadrant" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ getEncadrantName(slotProps.data.binome?.encadrant) }}
            </template>
          </Column>
          <Column field="binome.sujet.titre" header="Sujet" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ slotProps.data.binome?.sujet?.titre || '-' }}
            </template>
          </Column>
          <Column field="date" header="Date & Heure" sortable style="min-width: 12rem">
            <template #body="slotProps">
              <template v-if="slotProps.data.id">
                {{ formatDate(slotProps.data.date) }} à {{ slotProps.data.heure }}
              </template>
              <span v-else>-</span>
            </template>
          </Column>
          <Column field="salle" header="Salle & Jury" sortable style="min-width: 15rem">
            <template #body="slotProps">
              <template v-if="slotProps.data.id">
                Salle: {{ slotProps.data.salle?.nom }} <br>
                Jury: {{ getJuryName(slotProps.data.jury1) }}, {{ getJuryName(slotProps.data.jury2) }}
              </template>
              <span v-else>-</span>
            </template>
          </Column>
          <Column header="Actions" :exportable="false" style="min-width: 8rem">
            <template #body="slotProps">
              <div class="action-buttons">
                <Button 
                  v-if="slotProps.data.id"
                  icon="pi pi-pencil" 
                  class="p-button-rounded p-button-outlined p-button-info mr-2" 
                  @click="handleEditButtonClick(slotProps.data.id)"
                  :disabled="loading" 
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
                  v-if="slotProps.data.id"
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
      :header="currentSoutenance?.id ? 'Modifier la Soutenance' : 'Programmer une Soutenance'" 
      :modal="true" 
      class="soutenance-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div v-if="currentSoutenance" class="field-container">
        <div class="binome-info">
          <h3>Binôme</h3>
          <p><strong>Étudiant 1:</strong> {{ getStudentName(currentSoutenance.binome?.etudiant1) }}</p>
          <p v-if="currentSoutenance.binome?.etudiant2"><strong>Étudiant 2:</strong> {{ getStudentName(currentSoutenance.binome.etudiant2) }}</p>
          <p><strong>Encadrant:</strong> {{ getEncadrantName(currentSoutenance.binome?.encadrant) }}</p>
          <p><strong>Sujet:</strong> {{ currentSoutenance.binome?.sujet?.titre }}</p>
          <p><strong>Filière:</strong> {{ currentSoutenance.binome?.filiereName }}</p>
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
            @date-select="validateSoutenanceRequest"
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
            @date-select="validateSoutenanceRequest"
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
            @change="validateSoutenanceRequest"
          />
          <small v-if="validationErrors.salleId" class="p-error">{{ validationErrors.salleId }}</small>
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
            @change="validateSoutenanceRequest"
          />
          <small v-if="validationErrors.jury1Id" class="p-error">{{ validationErrors.jury1Id }}</small>
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
            @change="validateSoutenanceRequest"
          />
          <small v-if="validationErrors.jury2Id" class="p-error">{{ validationErrors.jury2Id }}</small>
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
          :disabled="!isFormValid"
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
import UserInfoHeader from '@/components/UserInfoHeader.vue';

// Import all required PrimeVue components
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
const soutenances = ref([]);
const binomes = ref([]);
const filieres = ref([]);
const jurys = ref([]);
const salles = ref([]);
const selectedFiliere = ref(null);
const today = ref(new Date());
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref('');

// Current soutenance state
const currentSoutenance = ref(null);
const showSoutenanceModal = ref(false);

// Form state
const soutenanceForm = ref({
  id: null,
  date: null,
  heure: null,
  binomeId: null,
  salleId: null,
  jury1Id: null,
  jury2Id: null
});

// Validation state
const validationErrors = ref({});
const isFormValid = computed(() => {
  return Object.keys(validationErrors.value).length === 0 &&
    soutenanceForm.value.date &&
    soutenanceForm.value.heure &&
    soutenanceForm.value.binomeId &&
    soutenanceForm.value.salleId &&
    soutenanceForm.value.jury1Id &&
    soutenanceForm.value.jury2Id;
});

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed properties
const filteredBinomes = computed(() => {
  if (!soutenances.value.length && !binomes.value.length) return [];
  
  // Create a merged array of soutenances and binomes without soutenances
  const mergedData = [...soutenances.value];
  
  // Add binomes that don't have soutenances yet
  binomes.value.forEach(binome => {
    const hasSoutenance = soutenances.value.some(s => s.binome?.id === binome.id);
    if (!hasSoutenance) {
      // Create an empty soutenance object with binome info
      mergedData.push({ binome });
    }
  });
  
  let filtered = mergedData;
  
  // Filter by selected filiere
  if (selectedFiliere.value) {
    filtered = filtered.filter(item => {
      return item.binome?.filiereName && filieres.value.some(f => 
        f.id === selectedFiliere.value && f.nom === item.binome.filiereName
      );
    });
  }
  
  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(item => {
      const binome = item.binome;
      if (!binome) return false;
      
      return (
        (binome.etudiant1?.nom?.toLowerCase().includes(query) || binome.etudiant1?.prenom?.toLowerCase().includes(query)) ||
        (binome.etudiant2 && (binome.etudiant2.nom?.toLowerCase().includes(query) || binome.etudiant2.prenom?.toLowerCase().includes(query))) ||
        (binome.encadrant?.nom?.toLowerCase().includes(query) || binome.encadrant?.prenom?.toLowerCase().includes(query)) ||
        (binome.sujet?.titre?.toLowerCase().includes(query))
      );
    });
  }
  
  return filtered;
});

const availableSalles = computed(() => {
  return salles.value;
});

// Fetch data on component mount
onMounted(async () => {
  await Promise.all([
    fetchSoutenances(),
    fetchBinomes(),
    fetchFilieres(),
    fetchJurys(),
    fetchSalles()
  ]);
  
  // Set default selected filiere if available
  if (filieres.value.length > 0) {
    selectedFiliere.value = filieres.value[0].id;
  }
});

// Data loading functions
async function fetchSoutenances() {
  loading.value = true;
  try {
    console.log('Fetching soutenances...');
    const response = await ApiService.get('/chef_de_departement/soutenances');
    console.log('Soutenances raw response:', response);
    
    // Check response structure and add safeguards
    if (Array.isArray(response)) {
      soutenances.value = response;
    } else if (response && Array.isArray(response.soutenances)) {
      soutenances.value = response.soutenances;
    } else {
      console.warn('Unexpected soutenances response format:', response);
      soutenances.value = response || [];
    }
    
    console.log('Processed soutenances:', soutenances.value);
    
    // Validate that we have all necessary nested objects
    soutenances.value.forEach((soutenance, index) => {
      if (!soutenance.binome) {
        console.warn(`Soutenance #${index} is missing binome data:`, soutenance);
      }
      if (!soutenance.salle) {
        console.warn(`Soutenance #${index} is missing salle data:`, soutenance);
      }
      if (!soutenance.jury1) {
        console.warn(`Soutenance #${index} is missing jury1 data:`, soutenance);
      }
      if (!soutenance.jury2) {
        console.warn(`Soutenance #${index} is missing jury2 data:`, soutenance);
      }
    });
  } catch (error) {
    console.error('Error fetching soutenances:', error);
    handleApiError(error, 'Erreur lors du chargement des soutenances');
  } finally {
    loading.value = false;
  }
}

async function fetchBinomes() {
  try {
    const response = await ApiService.get('/chef_de_departement/binomes');
    binomes.value = response.binomes || [];
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des binômes');
  }
}

async function fetchFilieres() {
  try {
    const response = await ApiService.get('/chef_de_departement/sujets');
    filieres.value = response.filieres || [];
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des filières');
  }
}

async function fetchJurys() {
  try {
    // Try with query parameter in URL directly, which is more reliable
    const response = await ApiService.get('/chef_de_departement/comptes?role=JURY');
    
    // If response has a comptes property, use it; otherwise use the response directly
    const juryData = response.comptes || response || [];
    
    jurys.value = juryData.map(jury => ({
      ...jury,
      fullName: `${jury.prenom} ${jury.nom}`
    }));
    
    console.log('Jurys loaded:', jurys.value);
  } catch (error) {
    console.error('Raw error loading jurys:', error);
    handleApiError(error, 'Erreur lors du chargement des jurys');
  }
}

async function fetchSalles() {
  try {
    console.log('Fetching salles data...');
    // Try using the chef_de_departement prefix path since it could be restricted
    const response = await ApiService.get('/chef_de_departement/salles');
    console.log('Salles response:', response);
    
    // Check different response formats
    if (Array.isArray(response)) {
      salles.value = response;
    } else if (response && Array.isArray(response.salles)) {
      salles.value = response.salles;
    } else {
      console.warn('Unexpected salles response format, trying alternative endpoint');
      // Try the alternative direct endpoint
      const altResponse = await ApiService.get('/salle');
      if (Array.isArray(altResponse)) {
        salles.value = altResponse;
      } else {
        console.error('Failed to parse salles from both endpoints');
        salles.value = [];
      }
    }
    
    console.log('Processed salles:', salles.value);
  } catch (error) {
    console.error('Error fetching salles:', error);
    // Try fallback endpoint if first one fails
    try {
      console.log('Trying fallback salles endpoint...');
      const fallbackResponse = await ApiService.get('/salle');
      salles.value = Array.isArray(fallbackResponse) ? fallbackResponse : [];
      console.log('Fallback salles loaded:', salles.value);
    } catch (fallbackError) {
      console.error('Fallback salles fetch also failed:', fallbackError);
      handleApiError(error, 'Erreur lors du chargement des salles');
      salles.value = [];
    }
  }
}

// Validation functions
async function validateSoutenanceRequest() {
  // Reset validation errors
  validationErrors.value = {};
  
  // Check if all required fields are present before validating
  if (!soutenanceForm.value.date || !soutenanceForm.value.heure || 
      !soutenanceForm.value.binomeId || !soutenanceForm.value.salleId || 
      !soutenanceForm.value.jury1Id || !soutenanceForm.value.jury2Id) {
    return;
  }
  
  try {
    // Create validation request
    const formattedDate = formatDateForApi(soutenanceForm.value.date);
    const formattedTime = formatTimeForApi(soutenanceForm.value.heure);
    
    console.log('Validating with data:', {
      date: formattedDate,
      heure: formattedTime,
      binomeId: soutenanceForm.value.binomeId,
      salleId: soutenanceForm.value.salleId,
      jury1Id: soutenanceForm.value.jury1Id,
      jury2Id: soutenanceForm.value.jury2Id
    });
    
    // Use the correct DTO format for validation requests
    const validationRequest = soutenanceForm.value.id 
      ? {
        id: soutenanceForm.value.id,
        date: formattedDate,
        heure: formattedTime,
        salleId: soutenanceForm.value.salleId,
        binomeId: soutenanceForm.value.binomeId,
        jury1Id: soutenanceForm.value.jury1Id,
        jury2Id: soutenanceForm.value.jury2Id
      }
      : {
        date: formattedDate,
        heure: formattedTime,
        salleId: soutenanceForm.value.salleId,
        binomeId: soutenanceForm.value.binomeId,
        jury1Id: soutenanceForm.value.jury1Id,
        jury2Id: soutenanceForm.value.jury2Id
      };
    
    let endpoint = '/chef_de_departement/soutenances/validate';
    if (soutenanceForm.value.id) {
      // For updates, use the specific validation endpoint
      endpoint = `/chef_de_departement/soutenances/${soutenanceForm.value.id}/validate`;
    }
    
    const response = await ApiService.post(endpoint, validationRequest);
    console.log('Validation response:', response);
    
    if (response && !response.valid && response.errors) {
      // Map errors to form fields
      response.errors.forEach(error => {
        validationErrors.value[error.field] = error.message;
      });
    }
  } catch (error) {
    console.error('Raw validation error:', error);
    handleApiError(error, 'Erreur lors de la validation de la soutenance');
  }
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

// Format time from Date object to HH:MM
function formatTimeForApi(time) {
  if (!time) return '';
  
  try {
    if (time instanceof Date) {
      // Ensure we have a valid Date object
      if (isNaN(time.getTime())) {
        console.error('Invalid Date object for time:', time);
        return '';
      }
      
      // Format as HH:MM
      const hours = time.getHours().toString().padStart(2, '0');
      const minutes = time.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    }
    
    if (typeof time === 'string') {
      // If it's already a string in HH:MM format, return it
      if (/^\d{1,2}:\d{2}$/.test(time)) {
        return time;
      }
      
      // Try to parse it as a date string
      const dateObj = new Date(time);
      if (!isNaN(dateObj.getTime())) {
        const hours = dateObj.getHours().toString().padStart(2, '0');
        const minutes = dateObj.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
      }
    }
    
    console.error('Unhandled time format:', time, typeof time);
    return '';
  } catch (error) {
    console.error('Error formatting time:', error);
    return '';
  }
}

// Format date to YYYY-MM-DD for API
function formatDateForApi(date) {
  if (!date) return null;
  
  try {
    // Handle Date objects
    if (date instanceof Date) {
      // Verify it's a valid date
      if (isNaN(date.getTime())) {
        console.error('Invalid Date object:', date);
        return null;
      }
      
      // Format as YYYY-MM-DD
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
    
    // Handle string dates
    if (typeof date === 'string') {
      // Check if it's already in YYYY-MM-DD format
      if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
        return date;
      }
      
      // Try to convert other formats to YYYY-MM-DD
      const parsedDate = new Date(date);
      if (!isNaN(parsedDate.getTime())) {
        const year = parsedDate.getFullYear();
        const month = (parsedDate.getMonth() + 1).toString().padStart(2, '0');
        const day = parsedDate.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
      }
    }
    
    console.error('Unhandled date format:', date, typeof date);
    return null;
  } catch (error) {
    console.error('Error formatting date:', error);
    return null;
  }
}

// Modal functions
function openScheduleModal(item) {
  currentSoutenance.value = item;
  
  // Reset form
  soutenanceForm.value = {
    id: null,
    date: new Date(),
    heure: new Date(),
    binomeId: item.binome?.id,
    salleId: null,
    jury1Id: null,
    jury2Id: null
  };
  
  validationErrors.value = {};
  showSoutenanceModal.value = true;
}

// Completely new implementation for edit button handling
async function handleEditButtonClick(soutenanceId) {
  loading.value = true;
  
  try {
    console.log(`Edit button clicked for soutenance ID: ${soutenanceId}`);
    
    // Direct fetch of the specific soutenance by ID
    const soutenanceData = await ApiService.get(`/chef_de_departement/soutenances/${soutenanceId}`);
    console.log('Fetched soutenance data:', soutenanceData);
    
    if (!soutenanceData || !soutenanceData.id) {
      throw new Error('Invalid soutenance data returned from API');
    }
    
    // Store the full soutenance data
    currentSoutenance.value = soutenanceData;
    
    // Extract date and time
    let dateObj = new Date();
    if (soutenanceData.date) {
      try {
        dateObj = new Date(soutenanceData.date);
      } catch (e) {
        console.error('Error parsing date:', e);
      }
    }
    
    let timeObj = new Date();
    if (soutenanceData.heure) {
      try {
        if (soutenanceData.heure.includes(':')) {
          const [hours, minutes] = soutenanceData.heure.split(':');
          timeObj = new Date();
          timeObj.setHours(parseInt(hours, 10), parseInt(minutes, 10));
        } else {
          timeObj = new Date(soutenanceData.heure);
        }
      } catch (e) {
        console.error('Error parsing time:', e);
      }
    }
    
    // Initialize form with the data
    soutenanceForm.value = {
      id: soutenanceData.id,
      date: dateObj,
      heure: timeObj,
      binomeId: soutenanceData.binome?.id || null,
      salleId: soutenanceData.salle?.id || null,
      jury1Id: soutenanceData.jury1?.id || null,
      jury2Id: soutenanceData.jury2?.id || null
    };
    
    console.log('Form initialized with:', soutenanceForm.value);
    
    // Make sure we have all the necessary data
    if (!soutenanceForm.value.binomeId || 
        !soutenanceForm.value.salleId || 
        !soutenanceForm.value.jury1Id || 
        !soutenanceForm.value.jury2Id) {
      console.warn('Some required fields are missing:', soutenanceForm.value);
      
      // Alert the user
      toast.add({
        severity: 'warn',
        summary: 'Avertissement',
        detail: 'Certaines données de la soutenance sont manquantes. L\'édition pourrait ne pas fonctionner correctement.',
        life: 5000
      });
    }
    
    // Clear any validation errors
    validationErrors.value = {};
    
    // Open the modal
    showSoutenanceModal.value = true;
    
  } catch (error) {
    console.error('Error initializing edit form:', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les données de la soutenance',
      life: 5000
    });
  } finally {
    loading.value = false;
  }
}

function closeSoutenanceModal() {
  showSoutenanceModal.value = false;
  currentSoutenance.value = null;
  validationErrors.value = {};
}

// CRUD operations
async function saveSoutenance() {
  submitting.value = true;
  try {
    // Prepare data
    const formattedDate = formatDateForApi(soutenanceForm.value.date);
    const formattedTime = formatTimeForApi(soutenanceForm.value.heure);
    
    console.log('Saving with formatted date and time:', { formattedDate, formattedTime });
    
    // Make sure we have all required fields
    if (!formattedDate || !formattedTime || !soutenanceForm.value.salleId || 
        !soutenanceForm.value.binomeId || !soutenanceForm.value.jury1Id || 
        !soutenanceForm.value.jury2Id) {
      const missingFields = [];
      if (!formattedDate) missingFields.push('date');
      if (!formattedTime) missingFields.push('heure');
      if (!soutenanceForm.value.salleId) missingFields.push('salle');
      if (!soutenanceForm.value.binomeId) missingFields.push('binôme');
      if (!soutenanceForm.value.jury1Id) missingFields.push('jury 1');
      if (!soutenanceForm.value.jury2Id) missingFields.push('jury 2');
      
      throw new Error(`Champs manquants: ${missingFields.join(', ')}`);
    }
    
    // Create request that matches the SoutenanceAddRequest or SoutenanceUpdateRequest DTO
    // Use exactly the field names as expected by the API
    const soutenanceRequest = {
      date: formattedDate,
      heure: formattedTime,
      salleId: soutenanceForm.value.salleId,
      binomeId: soutenanceForm.value.binomeId,
      jury1Id: soutenanceForm.value.jury1Id,
      jury2Id: soutenanceForm.value.jury2Id
    };
    
    if (soutenanceForm.value.id) {
      soutenanceRequest.id = soutenanceForm.value.id;
    }
    
    console.log('Sending soutenance request:', soutenanceRequest);
    
    let response;
    if (soutenanceForm.value.id) {
      // Update existing soutenance
      response = await ApiService.put(`/chef_de_departement/soutenances/${soutenanceForm.value.id}`, soutenanceRequest);
      console.log('Update response:', response);
      
      toast.add({
        severity: 'success',
        summary: 'Soutenance modifiée',
        detail: 'La soutenance a été mise à jour avec succès',
        life: 3000
      });
    } else {
      // Create new soutenance
      response = await ApiService.post('/chef_de_departement/soutenances', soutenanceRequest);
      console.log('Create response:', response);
      
      toast.add({
        severity: 'success',
        summary: 'Soutenance programmée',
        detail: 'La soutenance a été programmée avec succès',
        life: 3000
      });
    }
    
    // Close the modal
    closeSoutenanceModal();
    
    // Refresh the data to ensure we have the latest state
    await fetchSoutenances();
    
  } catch (error) {
    console.error('Error saving soutenance:', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: error.message || 'Erreur lors de l\'enregistrement de la soutenance',
      life: 5000
    });
  } finally {
    submitting.value = false;
  }
}

function confirmCancelSoutenance(soutenance) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir annuler la soutenance du binôme ${getStudentName(soutenance.binome?.etudiant1)}${soutenance.binome?.etudiant2 ? ` et ${getStudentName(soutenance.binome.etudiant2)}` : ''}?`,
    header: 'Confirmation d\'annulation',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => cancelSoutenance(soutenance),
    reject: () => {/* do nothing */}
  });
}

async function cancelSoutenance(soutenance) {
  if (!soutenance.id) return;
  
  try {
    await ApiService.delete(`/chef_de_departement/soutenances/${soutenance.id}`);
    
    // Update local data
    soutenances.value = soutenances.value.filter(s => s.id !== soutenance.id);
    
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

// Error handling
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
   margin: 0 auto;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

.header-section {
  margin-bottom: 2rem;
}

.page-title {
  color: var(--primary-color);
  font-size: 2rem;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.filiere-dropdown {
  display: inline-flex;
  min-width: 200px;
}

.action-bar {
  display: flex;
  justify-content: flex-end; /* Right-align the button */
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
  
  .page-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .action-buttons {
    flex-direction: row;
  }
}


/* 
  Copy these styles into each management view's <style> section 
  to maintain consistent header styling across all views
*/

/* Header section styling */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.dark-mode .header-section {
  border-bottom-color: #333;
}

.title-filter-group {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.page-title {
  margin: 0;
  font-size: 2rem;
  font-weight: 600;
  color: var(--primary-color);
  white-space: nowrap;
}

.filter-dropdown {
  /* min-width: 220px;
  height: 38px; */
  font-size: 1.5rem;
}

:deep(.filter-dropdown .p-dropdown-label) {
  display: flex;
  align-items: center;
}

:deep(.filter-dropdown .p-dropdown-trigger) {
  width: 2.5rem;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
}

.action-btn {
  height: 38px;
  min-width: 150px;
  font-weight: 500;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* Dark mode adaptations */
.dark-mode .filter-dropdown {
  background-color: #222;
  border-color: #444;
}

.dark-mode .filter-dropdown :deep(.p-dropdown-label) {
  color: #f0f0f0;
}

.dark-mode .filter-dropdown :deep(.p-dropdown-trigger-icon) {
  color: #aaa;
}

/* Responsive styles */
@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .title-filter-group {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
    gap: 0.75rem;
  }
  
  .filter-dropdown {
    width: 100%;
    min-width: 100%;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: flex-end;
  }
  
  .action-btn {
    width: 100%;
  }
}
</style>