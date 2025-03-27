<template>
  <div class="suggestions-management">
    <Toast />
    <ConfirmDialog />
    
    <!--  the UserInfoHeader component -->
    <UserInfoHeader 
      searchPlaceholder="Rechercher un binôme..." 
      :initialSearchValue="searchQuery"
      @search="handleHeaderSearch" 
    />

    <div class="header-section">
  <div class="title-filter-group">
    <h1 class="page-title">Suggestions de Sujets PFE</h1>
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
    

    <!-- Data Table with PrimeVue -->
    <Card class="table-card">
      <template #content>
        <DataTable 
          :value="filteredSuggestions" 
          :loading="loading" 
          responsiveLayout="scroll"
          stripedRows 
          :paginator="filteredSuggestions.length > 10" 
          :rows="10" 
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
          :rowsPerPageOptions="[10, 20, 50]"
          currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} suggestions"
          emptyMessage="Aucune suggestion trouvée"
        >
          <Column field="titre" header="Titre" sortable style="min-width: 12rem"></Column>
          <Column field="theme" header="Thème" sortable style="min-width: 8rem"></Column>
          <Column field="binome.etudiant1Name" header="Étudiant 1" sortable style="min-width: 12rem"></Column>
          <Column field="binome.etudiant2Name" header="Étudiant 2" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ slotProps.data.binome.etudiant2Name || '-' }}
            </template>
          </Column>
          <Column field="status" header="Status" sortable style="min-width: 8rem">
            <template #body="slotProps">
              <span :class="getStatusClass(slotProps.data.status)">{{ getStatusLabel(slotProps.data.status) }}</span>
            </template>
          </Column>
          <Column header="Actions" :exportable="false" style="min-width: 10rem">
            <template #body="slotProps">
              <div class="action-buttons">
                <Button 
                  icon="pi pi-eye" 
                  class="p-button-rounded p-button-outlined p-button-info mr-2" 
                  @click="viewSuggestion(slotProps.data)" 
                  tooltip="Voir détails"
                  tooltipOptions="top"
                />
                <Button 
                  v-if="slotProps.data.status === 'en_attente'"
                  icon="pi pi-check" 
                  class="p-button-rounded p-button-outlined p-button-success mr-2" 
                  @click="confirmAccept(slotProps.data)" 
                  tooltip="Accepter"
                  tooltipOptions="top"
                />
                <Button 
                  v-if="slotProps.data.status === 'en_attente'"
                  icon="pi pi-times" 
                  class="p-button-rounded p-button-outlined p-button-danger" 
                  @click="confirmReject(slotProps.data)" 
                  tooltip="Refuser"
                  tooltipOptions="top"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>
    
    <!-- View Details Dialog -->
    <Dialog
      v-model:visible="showViewModal"
      :header="viewingSuggestion?.titre"
      :modal="true"
      class="suggestion-dialog"
      :style="{ width: '700px' }"
    >
      <div v-if="viewingSuggestion" class="suggestion-details">
        <div class="detail-section">
          <h3>Informations du sujet</h3>
          <div class="detail-item">
            <span class="detail-label">Thème:</span>
            <span class="detail-value">{{ viewingSuggestion.theme }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Status:</span>
            <span class="detail-value" :class="getStatusClass(viewingSuggestion.status)">
              {{ getStatusLabel(viewingSuggestion.status) }}
            </span>
          </div>
          
          <div class="detail-item description-item">
            <span class="detail-label">Description:</span>
            <p class="detail-value description-text">{{ viewingSuggestion.description }}</p>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>Informations du binôme</h3>
          <div class="detail-item">
            <span class="detail-label">Étudiant 1:</span>
            <span class="detail-value">{{ viewingSuggestion.binome.etudiant1Name }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Étudiant 2:</span>
            <span class="detail-value">{{ viewingSuggestion.binome.etudiant2Name || '-' }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Encadrant:</span>
            <span class="detail-value">{{ viewingSuggestion.binome.encadrantName }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Filière:</span>
            <span class="detail-value">{{ viewingSuggestion.binome.filiereName }}</span>
          </div>
        </div>
        
        <div v-if="viewingSuggestion.status === 'en_attente'" class="action-section">
          <Button 
            label="Accepter" 
            icon="pi pi-check" 
            class="p-button-success mr-2" 
            @click="confirmAccept(viewingSuggestion)" 
          />
          <Button 
            label="Refuser" 
            icon="pi pi-times" 
            class="p-button-danger" 
            @click="confirmReject(viewingSuggestion)" 
          />
        </div>
      </div>
      
      <template #footer>
        <Button 
          label="Fermer" 
          icon="pi pi-times" 
          class="p-button-text" 
          @click="showViewModal = false" 
        />
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import { useRouter } from 'vue-router';
import ApiService from '@/services/ApiService';
import UserInfoHeader from '@/components/UserInfoHeader.vue';

// Import PrimeVue components
import Toast from 'primevue/toast';
import ConfirmDialog from 'primevue/confirmdialog';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';

// Component state
const suggestions = ref([]);
const filieres = ref([]);
const selectedFiliere = ref(null);
const viewingSuggestion = ref(null);
const showViewModal = ref(false);
const loading = ref(false);
const searchQuery = ref('');

// Services
const toast = useToast();
const confirm = useConfirm();
const router = useRouter();

// Computed filtered suggestions
const filteredSuggestions = computed(() => {
  const query = searchQuery.value.toLowerCase();
  let filtered = [...suggestions.value];
  
  // Filter by search query
  if (query) {
    filtered = filtered.filter(suggestion => 
      suggestion.titre.toLowerCase().includes(query) || 
      suggestion.theme.toLowerCase().includes(query) ||
      suggestion.description.toLowerCase().includes(query) ||
      (suggestion.binome.etudiant1Name && suggestion.binome.etudiant1Name.toLowerCase().includes(query)) ||
      (suggestion.binome.etudiant2Name && suggestion.binome.etudiant2Name.toLowerCase().includes(query))
    );
  }
  
  // Filter by filière if selected and we have filière data
  if (selectedFiliere.value && filtered.length > 0) {
    // Check if suggestions have filiereId property in binome
    const hasFiliereId = filtered.some(suggestion => 
      suggestion.binome && suggestion.binome.filiereId !== undefined
    );
    
    if (hasFiliereId) {
      filtered = filtered.filter(suggestion => 
        suggestion.binome && suggestion.binome.filiereId === selectedFiliere.value
      );
    } else {
      // Fallback: try to filter by filière name instead
      const selectedFiliereName = filieres.value.find(f => f.id === selectedFiliere.value)?.nom;
      if (selectedFiliereName) {
        filtered = filtered.filter(suggestion => 
          suggestion.binome && suggestion.binome.filiereName === selectedFiliereName
        );
      }
    }
  }
  
  return filtered;
});

// Fetch data on component mount
onMounted(async () => {
  await Promise.all([
    fetchSuggestions(),
    fetchFilieres()
  ]);
});

// Methods for fetching data
async function fetchSuggestions() {
  loading.value = true;
  try {
    const response = await ApiService.get('/chef_de_departement/sujet-suggestions');
    suggestions.value = response;
    
    // Extract unique filières from suggestions if needed
    if (filieres.value.length === 0 && response.length > 0) {
      extractFilieresFromSuggestions(response);
    }
    
    // Add filiereId property to suggestions if missing but filière name is present
    if (filieres.value.length > 0) {
      mapFiliereIdsToSuggestions();
    }
    
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des suggestions');
    
    // Generate sample data if in development
    if (import.meta.env.DEV && suggestions.value.length === 0) {
      generateSampleData();
    }
  } finally {
    loading.value = false;
  }
}

async function fetchFilieres() {
  try {
    const response = await ApiService.get('/chef_de_departement/filieres');
    if (Array.isArray(response) && response.length > 0) {
      filieres.value = response;
      
      // Set first filière as default
      selectedFiliere.value = filieres.value[0].id;
      
      // Map filière IDs to suggestions if suggestions are already loaded
      if (suggestions.value.length > 0) {
        mapFiliereIdsToSuggestions();
      }
    }
  } catch (error) {
    console.error('Error fetching filières:', error);
    // Try to extract filières from suggestions if they're loaded
    if (suggestions.value.length > 0) {
      extractFilieresFromSuggestions(suggestions.value);
    }
  }
}

function handleFiliereChange() {
  console.log('Filière selected:', selectedFiliere.value);
}

// Helper function to extract unique filières from suggestions
function extractFilieresFromSuggestions(suggestionsData) {
  const uniqueFilieres = new Map();
  
  suggestionsData.forEach(suggestion => {
    if (suggestion.binome && suggestion.binome.filiereName) {
      // If we don't have an ID, generate one based on the index
      const id = suggestion.binome.filiereId || uniqueFilieres.size + 1;
      uniqueFilieres.set(suggestion.binome.filiereName, {
        id,
        nom: suggestion.binome.filiereName
      });
    }
  });
  
  if (uniqueFilieres.size > 0) {
    filieres.value = Array.from(uniqueFilieres.values());
    
    // Set first filière as default
    if (filieres.value.length > 0 && !selectedFiliere.value) {
      selectedFiliere.value = filieres.value[0].id;
    }
  }
}

// Helper function to add filiereId to suggestions based on filiereName
function mapFiliereIdsToSuggestions() {
  if (filieres.value.length === 0 || suggestions.value.length === 0) return;
  
  // Create a mapping of filière names to IDs
  const filiereNameToId = {};
  filieres.value.forEach(filiere => {
    filiereNameToId[filiere.nom] = filiere.id;
  });
  
  // Add filiereId to each suggestion if it has a filiereName but no filiereId
  suggestions.value.forEach(suggestion => {
    if (suggestion.binome && 
        suggestion.binome.filiereName && 
        filiereNameToId[suggestion.binome.filiereName] && 
        !suggestion.binome.filiereId) {
      suggestion.binome.filiereId = filiereNameToId[suggestion.binome.filiereName];
    }
  });
}

// Generate sample data for development
function generateSampleData() {
  console.log('Generating sample data for development');
  
  const sampleFilieres = [
    { id: 1, nom: 'Génie Informatique' },
    { id: 2, nom: 'Génie Civil' },
    { id: 3, nom: 'Génie Électrique' }
  ];
  
  const sampleSuggestions = [];
  
  for (let i = 1; i <= 15; i++) {
    const filiereIndex = (i % 3);
    const filiere = sampleFilieres[filiereIndex];
    
    sampleSuggestions.push({
      id: i,
      titre: `Sujet de PFE ${i}`,
      theme: `Thème ${i}`,
      description: `Description détaillée du sujet de PFE ${i}. Ce texte est une longue description qui explique le contexte et les objectifs du projet.`,
      status: i % 3 === 0 ? 'accepter' : (i % 3 === 1 ? 'refuser' : 'en_attente'),
      binome: {
        id: i,
        etudiant1Name: `Étudiant ${i}A`,
        etudiant2Name: i % 2 === 0 ? `Étudiant ${i}B` : null,
        encadrantName: `Encadrant ${i}`,
        filiereId: filiere.id,
        filiereName: filiere.nom
      }
    });
  }
  
  filieres.value = sampleFilieres;
  suggestions.value = sampleSuggestions;
  
  // Set default filière
  selectedFiliere.value = filieres.value[0].id;
}

// Suggestion management methods
function confirmAccept(suggestion) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir accepter la suggestion "${suggestion.titre}"? Le sujet sera attribué au binôme qui l'a proposé.`,
    header: 'Confirmation d\'acceptation',
    icon: 'pi pi-check',
    acceptClass: 'p-button-success',
    accept: () => acceptSuggestion(suggestion),
    reject: () => {/* do nothing */}
  });
}

function confirmReject(suggestion) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir refuser la suggestion "${suggestion.titre}"?`,
    header: 'Confirmation de refus',
    icon: 'pi pi-times',
    acceptClass: 'p-button-danger',
    accept: () => rejectSuggestion(suggestion),
    reject: () => {/* do nothing */}
  });
}

async function acceptSuggestion(suggestion) {
  try {
    await ApiService.post(`/chef_de_departement/sujet-suggestions/${suggestion.id}/accept`);
    
    // Update local state
    const index = suggestions.value.findIndex(s => s.id === suggestion.id);
    if (index !== -1) {
      suggestions.value[index].status = 'accepter';
    }
    
    toast.add({
      severity: 'success',
      summary: 'Suggestion acceptée',
      detail: 'La suggestion a été acceptée et le sujet attribué au binôme',
      life: 3000
    });
    
    // Close modal if open
    if (showViewModal.value) {
      showViewModal.value = false;
    }
  } catch (error) {
    handleApiError(error, 'Erreur lors de l\'acceptation de la suggestion');
  }
}

async function rejectSuggestion(suggestion) {
  try {
    await ApiService.post(`/chef_de_departement/sujet-suggestions/${suggestion.id}/reject`);
    
    // Update local state
    const index = suggestions.value.findIndex(s => s.id === suggestion.id);
    if (index !== -1) {
      suggestions.value[index].status = 'refuser';
    }
    
    toast.add({
      severity: 'info',
      summary: 'Suggestion refusée',
      detail: 'La suggestion a été refusée',
      life: 3000
    });
    
    // Close modal if open
    if (showViewModal.value) {
      showViewModal.value = false;
    }
  } catch (error) {
    handleApiError(error, 'Erreur lors du refus de la suggestion');
  }
}

// UI helper methods
function viewSuggestion(suggestion) {
  viewingSuggestion.value = suggestion;
  showViewModal.value = true;
}

function goBackToSujets() {
  router.push('/management/sujets');
}

function getStatusLabel(status) {
  switch (status) {
    case 'en_attente': return 'En attente';
    case 'accepter': return 'Accepté';
    case 'refuser': return 'Refusé';
    default: return status;
  }
}

function getStatusClass(status) {
  switch (status) {
    case 'en_attente': return 'status-waiting';
    case 'accepter': return 'status-accepted';
    case 'refuser': return 'status-rejected';
    default: return '';
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
.suggestions-management {
  /* max-width: 1200px; */
  margin: 0 auto;
  /* padding: 2rem; */
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

.header-section {
  margin-bottom: 2rem;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.page-title {
  color: var(--primary-color);
  font-size: 2rem;
  margin: 0;
}



.filter-row {
  display: flex;
  margin-top: 1rem;
}

.filiere-dropdown {
  min-width: 250px;
}

.action-bar {
  display: flex;
  justify-content: flex-end; /* Right-align the button */
  margin-bottom: 1.5rem;
}


.table-card {
  margin-bottom: 2rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

/* Status styles */
.status-waiting {
  color: #FF9900;
  font-weight: 600;
}

.status-accepted {
  color: #22C55E;
  font-weight: 600;
}

.status-rejected {
  color: #F43F5E;
  font-weight: 600;
}

/* Detail dialog styles */
.suggestion-details {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.detail-section {
  border: 1px solid var(--surface-border);
  padding: 1rem;
  border-radius: 8px;
  background-color: var(--surface-ground);
}

.detail-section h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: var(--primary-color);
  font-size: 1.2rem;
}

.detail-item {
  display: flex;
  margin-bottom: 0.75rem;
}

.description-item {
  flex-direction: column;
}

.detail-label {
  font-weight: 600;
  min-width: 100px;
}

.detail-value {
  flex: 1;
}

.description-text {
  margin-top: 0.5rem;
  white-space: pre-wrap;
}

.action-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .suggestions-management {
    padding: 1rem;
  }
  
  .title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  

  
  .filter-row {
    width: 100%;
  }
  
  .filiere-dropdown {
    width: 100%;
  }
  
  .detail-item {
    flex-direction: column;
  }
  
  .detail-label {
    margin-bottom: 0.25rem;
  }
  
  .action-section {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .action-section .p-button {
    width: 100%;
    margin-right: 0 !important;
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