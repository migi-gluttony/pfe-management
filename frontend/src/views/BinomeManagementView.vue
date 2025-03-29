<template>
  <div class="binome-management">
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
        <h1 class="page-title">Gestion des Binômes de la Filiere : </h1>
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
        <Button 
          label="Ajouter un binôme" 
          icon="pi pi-plus" 
          class="p-button-primary action-btn"
          @click="openAddModal" 
        />
      </div>
    </div>

    <!-- Data Table with PrimeVue -->
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
          <Column field="etudiant1.nom" header="Étudiant 1" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ getStudentFullName(slotProps.data.etudiant1) }}
            </template>
          </Column>
          <Column field="etudiant2.nom" header="Étudiant 2" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ slotProps.data.etudiant2 ? getStudentFullName(slotProps.data.etudiant2) : '-' }}
            </template>
          </Column>
          <Column field="encadrant.nom" header="Encadrant" sortable style="min-width: 12rem">
            <template #body="slotProps">
              {{ getEncadrantFullName(slotProps.data.encadrant) }}
            </template>
          </Column>
          <Column field="sujet.titre" header="Sujet" sortable style="min-width: 15rem"></Column>
          <!-- Removed Filière column -->
          <Column header="Actions" :exportable="false" style="min-width: 10rem">
            <template #body="slotProps">
              <div class="action-buttons">
                <Button 
                  icon="pi pi-pencil" 
                  class="p-button-rounded p-button-outlined p-button-info mr-2" 
                  @click="openEditModal(slotProps.data)" 
                  tooltip="Changer l'encadrant"
                  tooltipOptions="top"
                />
                <Button 
                  icon="pi pi-trash" 
                  class="p-button-rounded p-button-outlined p-button-danger" 
                  @click="confirmDelete(slotProps.data)" 
                  tooltip="Supprimer"
                  tooltipOptions="top"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>
    
    <!-- Add Binome Dialog -->
    <Dialog 
      v-model:visible="showAddModal" 
      header="Ajouter un Nouveau Binôme" 
      :modal="true" 
      class="binome-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div class="field-container">
        <div class="p-field">
          <label for="etudiant1">Étudiant 1 <span class="required">*</span></label>
          <Dropdown 
            id="etudiant1" 
            v-model="newBinome.etudiant1Id" 
            :options="processedStudents" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un étudiant" 
            class="w-full"
            :filter="true"
            filterMatchMode="contains"
            :showClear="true"
            :virtualScrollerOptions="{ itemSize: 38 }"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="etudiant2">Étudiant 2</label>
          <Dropdown 
            id="etudiant2" 
            v-model="newBinome.etudiant2Id" 
            :options="filteredStudent2Options" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un étudiant (optionnel)" 
            class="w-full"
            :filter="true"
            filterMatchMode="contains"
            :showClear="true"
            :virtualScrollerOptions="{ itemSize: 38 }"
            :disabled="!newBinome.etudiant1Id"
          />
        </div>
        
        <div class="p-field">
          <label for="encadrant">Encadrant <span class="required">*</span></label>
          <Dropdown 
            id="encadrant" 
            v-model="newBinome.encadrantId" 
            :options="processedEncadrants" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un encadrant" 
            class="w-full"
            :filter="true"
            filterMatchMode="contains"
            :showClear="true"
            :virtualScrollerOptions="{ itemSize: 38 }"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="sujet">Sujet <span class="required">*</span></label>
          <Dropdown 
            id="sujet" 
            v-model="newBinome.sujetId" 
            :options="availableSujets" 
            optionLabel="titre" 
            optionValue="id"
            placeholder="Sélectionner un sujet" 
            class="w-full"
            :filter="true"
            filterMatchMode="contains"
            :virtualScrollerOptions="{ itemSize: 38 }"
            required
          />
        </div>
      </div>
      
      <template #footer>
        <Button 
          label="Annuler" 
          icon="pi pi-times" 
          class="p-button-text" 
          @click="closeModals" 
        />
        <Button 
          label="Ajouter" 
          icon="pi pi-check" 
          class="p-button-primary" 
          @click="addBinome" 
          :loading="submitting"
        />
      </template>
    </Dialog>
    
    <!-- Edit Binome Dialog (Change Encadrant) -->
    <Dialog 
      v-model:visible="showEditModal" 
      header="Changer l'Encadrant du Binôme" 
      :modal="true" 
      class="binome-dialog"
      :style="{ width: '500px' }"
      :closable="false"
    >
      <div v-if="editingBinome" class="field-container">
        <div class="current-info">
          <p><strong>Binôme:</strong> {{ getStudentFullName(editingBinome.etudiant1) }} 
             {{ editingBinome.etudiant2 ? '& ' + getStudentFullName(editingBinome.etudiant2) : '' }}</p>
          <p><strong>Encadrant actuel:</strong> {{ getEncadrantFullName(editingBinome.encadrant) }}</p>
        </div>
        
        <div class="p-field">
          <label for="new-encadrant">Nouvel Encadrant <span class="required">*</span></label>
          <Dropdown 
            id="new-encadrant" 
            v-model="editRequest.encadrantId" 
            :options="processedEncadrants" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un encadrant" 
            class="w-full"
            :filter="true"
            filterMatchMode="contains"
            :virtualScrollerOptions="{ itemSize: 38 }"
            required
          />
        </div>
      </div>
      
      <template #footer>
        <Button 
          label="Annuler" 
          icon="pi pi-times" 
          class="p-button-text" 
          @click="closeModals" 
        />
        <Button 
          label="Sauvegarder" 
          icon="pi pi-check" 
          class="p-button-primary" 
          @click="saveEditedBinome" 
          :loading="submitting"
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

// Component state
const binomes = ref([]);
const filieres = ref([]);
const selectedFiliere = ref(null);
const availableStudents = ref([]);
const encadrants = ref([]);
const availableSujets = ref([]);

const newBinome = ref({
  etudiant1Id: null,
  etudiant2Id: null,
  encadrantId: null,
  sujetId: null
});

const editingBinome = ref(null);
const editRequest = ref({
  encadrantId: null
});

const showAddModal = ref(false);
const showEditModal = ref(false);
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref('');

// Services
const toast = useToast();
const confirm = useConfirm();

// Process data for dropdowns - do this once when data is fetched
const processedStudents = ref([]);
const processedEncadrants = ref([]);

// Update processed data whenever the source data changes
watch(availableStudents, (newVal) => {
  processedStudents.value = newVal.map(student => ({
    ...student,
    fullName: `${student.nom} ${student.prenom}${student.cne ? ' (' + student.cne + ')' : ''}`
  }));
}, { immediate: true });

watch(encadrants, (newVal) => {
  processedEncadrants.value = newVal.map(encadrant => ({
    ...encadrant,
    fullName: `${encadrant.nom} ${encadrant.prenom}`
  }));
}, { immediate: true });

// Filtered options for student 2 - depends on student 1 selection
const filteredStudent2Options = computed(() => {
  if (!newBinome.value.etudiant1Id) return [];
  return processedStudents.value.filter(s => s.id !== newBinome.value.etudiant1Id);
});

// Computed filtered binomes
const filteredBinomes = computed(() => {
  const query = searchQuery.value.toLowerCase();
  if (!query) return binomes.value;
  
  return binomes.value.filter(binome => 
    (binome.etudiant1?.nom?.toLowerCase().includes(query) || binome.etudiant1?.prenom?.toLowerCase().includes(query)) ||
    (binome.etudiant2?.nom?.toLowerCase().includes(query) || binome.etudiant2?.prenom?.toLowerCase().includes(query)) ||
    (binome.encadrant?.nom?.toLowerCase().includes(query) || binome.encadrant?.prenom?.toLowerCase().includes(query)) ||
    (binome.sujet?.titre?.toLowerCase().includes(query))
    // Removed the filière filter
  );
});

// Fetch data on component mount
onMounted(async () => {
  await fetchData();
});

// Handler for search from header component
function handleHeaderSearch(query) {
  searchQuery.value = query;
}

// Helper methods
function getStudentFullName(student) {
  if (!student) return '-';
  return `${student.nom} ${student.prenom}`;
}

function getEncadrantFullName(encadrant) {
  if (!encadrant) return '-';
  return `${encadrant.nom} ${encadrant.prenom}`;
}

// Methods for fetching data
async function fetchData(filiereId = null) {
  loading.value = true;
  try {
    const endpoint = filiereId 
      ? `/chef_de_departement/binomes?filiereId=${filiereId}`
      : '/chef_de_departement/binomes';
      
    const response = await ApiService.get(endpoint);
    
    binomes.value = response.binomes || [];
    filieres.value = response.filieres || [];
    availableStudents.value = response.availableStudents || [];
    encadrants.value = response.encadrants || [];
    availableSujets.value = response.availableSujets || [];
    
    // Set first filière as default if none selected
    if (filieres.value.length > 0 && !selectedFiliere.value) {
      selectedFiliere.value = filieres.value[0].id;
      // Immediately fetch binomes for the selected filière
      fetchBinomesForFiliere(selectedFiliere.value);
    }
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des données');
  } finally {
    loading.value = false;
  }
}

// Function to get binomes for a specific filière
async function fetchBinomesForFiliere(filiereId) {
  if (!filiereId) return;
  
  loading.value = true;
  try {
    const response = await ApiService.get(`/chef_de_departement/binomes?filiereId=${filiereId}`);
    binomes.value = response.binomes || [];
    availableStudents.value = response.availableStudents || [];
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des binômes pour cette filière');
  } finally {
    loading.value = false;
  }
}

function handleFiliereChange() {
  fetchBinomesForFiliere(selectedFiliere.value);
}

// CRUD operations
async function addBinome() {
  if (!validateBinomeData(newBinome.value)) return;
  
  submitting.value = true;
  try {
    const response = await ApiService.post('/chef_de_departement/binomes', newBinome.value);
    binomes.value.push(response);
    
    // Reset form and close modal
    resetNewBinomeForm();
    showAddModal.value = false;
    
    // Show success message
    toast.add({
      severity: 'success',
      summary: 'Binôme ajouté',
      detail: 'Le binôme a été ajouté avec succès',
      life: 3000
    });
    
    // Refresh data to update available students and subjects
    await fetchBinomesForFiliere(selectedFiliere.value);
  } catch (error) {
    handleApiError(error, "Erreur lors de l'ajout du binôme");
  } finally {
    submitting.value = false;
  }
}

async function saveEditedBinome() {
  if (!editRequest.value.encadrantId) {
    showValidationError("Veuillez sélectionner un encadrant");
    return;
  }
  
  submitting.value = true;
  try {
    const response = await ApiService.put(`/chef_de_departement/binomes/${editingBinome.value.id}`, editRequest.value);
    
    // Update local data
    const index = binomes.value.findIndex(b => b.id === editingBinome.value.id);
    if (index !== -1) {
      binomes.value[index] = response;
    }
    
    showEditModal.value = false;
    
    toast.add({
      severity: 'success',
      summary: 'Binôme mis à jour',
      detail: "L'encadrant du binôme a été modifié avec succès",
      life: 3000
    });
  } catch (error) {
    handleApiError(error, "Erreur lors de la modification de l'encadrant");
  } finally {
    submitting.value = false;
  }
}

function confirmDelete(binome) {
  const studentNames = binome.etudiant2 
    ? `${getStudentFullName(binome.etudiant1)} et ${getStudentFullName(binome.etudiant2)}`
    : getStudentFullName(binome.etudiant1);
    
  confirm.require({
    message: `Êtes-vous sûr de vouloir supprimer le binôme de ${studentNames}?`,
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => deleteBinome(binome),
    reject: () => {/* do nothing */}
  });
}

async function deleteBinome(binome) {
  try {
    await ApiService.delete(`/chef_de_departement/binomes/${binome.id}`);
    
    // Update local data
    binomes.value = binomes.value.filter(b => b.id !== binome.id);
    
    toast.add({
      severity: 'success',
      summary: 'Binôme supprimé',
      detail: 'Le binôme a été supprimé avec succès',
      life: 3000
    });
    
    // Refresh data to update available students
    await fetchBinomesForFiliere(selectedFiliere.value);
  } catch (error) {
    handleApiError(error, 'Erreur lors de la suppression du binôme');
  }
}

// UI control methods
function openAddModal() {
  resetNewBinomeForm();
  showAddModal.value = true;
}

function openEditModal(binome) {
  editingBinome.value = { ...binome };
  editRequest.value.encadrantId = binome.encadrant.id;
  showEditModal.value = true;
}

function closeModals() {
  showAddModal.value = false;
  showEditModal.value = false;
  resetNewBinomeForm();
}

function resetNewBinomeForm() {
  newBinome.value = {
    etudiant1Id: null,
    etudiant2Id: null,
    encadrantId: null,
    sujetId: null
  };
}

// Validation
function validateBinomeData(binome) {
  if (!binome.etudiant1Id) {
    showValidationError("L'étudiant 1 est obligatoire");
    return false;
  }
  
  if (!binome.encadrantId) {
    showValidationError("L'encadrant est obligatoire");
    return false;
  }
  
  if (!binome.sujetId) {
    showValidationError("Le sujet est obligatoire");
    return false;
  }
  
  return true;
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

function showValidationError(message) {
  toast.add({
    severity: 'warn',
    summary: 'Validation',
    detail: message,
    life: 5000
  });
}
</script>

<style scoped>
.binome-management {
  margin: 0 auto;
  /* padding: 2rem; */
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

.filiere-dropdown {
  min-width: 250px;
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

.button-row {
  display: flex;
  justify-content: flex-end;
}

.add-btn {
  min-width: 150px;
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

.current-info {
  background-color: var(--surface-ground);
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.current-info p {
  margin: 0.5rem 0;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .binome-management {
    padding: 1rem;
  }
  
  .title-row, .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filiere-dropdown {
    width: 100%;
    margin-top: 0.5rem;
  }
  
  .button-row {
    justify-content: center;
  }
  
  .add-btn {
    width: 100%;
  }
}

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