<template>
  <div class="binome-management">
    <Toast />
    <ConfirmDialog />
    
    <div class="header-section">
      <div class="title-row">
        <h1 class="page-title">Gestion des Binômes</h1>
      </div>
      <div class="filter-row">
        <Dropdown 
          v-model="selectedFiliere" 
          :options="filieres" 
          optionLabel="nom" 
          optionValue="id"
          placeholder="Filtrer par filière" 
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
      <div class="button-row">
        <Button 
          label="Ajouter un binôme" 
          icon="pi pi-plus" 
          class="p-button-primary add-btn"
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
          <Column field="filiereName" header="Filière" sortable style="min-width: 8rem"></Column>
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
            :options="availableStudents" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un étudiant" 
            class="w-full"
            :filter="true"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="etudiant2">Étudiant 2</label>
          <Dropdown 
            id="etudiant2" 
            v-model="newBinome.etudiant2Id" 
            :options="availableStudents.filter(s => s.id !== newBinome.etudiant1Id)" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un étudiant (optionnel)" 
            class="w-full"
            :filter="true"
            :disabled="!newBinome.etudiant1Id"
          />
        </div>
        
        <div class="p-field">
          <label for="encadrant">Encadrant <span class="required">*</span></label>
          <Dropdown 
            id="encadrant" 
            v-model="newBinome.encadrantId" 
            :options="encadrants" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un encadrant" 
            class="w-full"
            :filter="true"
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
            :options="encadrants" 
            optionLabel="fullName" 
            optionValue="id"
            placeholder="Sélectionner un encadrant" 
            class="w-full"
            :filter="true"
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

// Process data for dropdowns
const processedAvailableStudents = computed(() => {
  return availableStudents.value.map(student => ({
    ...student,
    fullName: `${student.nom} ${student.prenom} (${student.cne || 'N/A'})`
  }));
});

const processedEncadrants = computed(() => {
  return encadrants.value.map(encadrant => ({
    ...encadrant,
    fullName: `${encadrant.nom} ${encadrant.prenom}`
  }));
});

// Computed filtered binomes
const filteredBinomes = computed(() => {
  const query = searchQuery.value.toLowerCase();
  if (!query) return binomes.value;
  
  return binomes.value.filter(binome => 
    (binome.etudiant1?.nom.toLowerCase().includes(query) || binome.etudiant1?.prenom.toLowerCase().includes(query)) ||
    (binome.etudiant2?.nom?.toLowerCase().includes(query) || binome.etudiant2?.prenom?.toLowerCase().includes(query)) ||
    (binome.encadrant?.nom.toLowerCase().includes(query) || binome.encadrant?.prenom.toLowerCase().includes(query)) ||
    (binome.sujet?.titre.toLowerCase().includes(query)) ||
    (binome.filiereName?.toLowerCase().includes(query))
  );
});

// Fetch data on component mount
onMounted(async () => {
  await fetchData();
});

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
    
    binomes.value = response.binomes;
    filieres.value = response.filieres;
    availableStudents.value = response.availableStudents;
    encadrants.value = response.encadrants;
    availableSujets.value = response.availableSujets;
    
    // Set default selected filiere if none selected
    if (filieres.value.length > 0 && !selectedFiliere.value) {
      selectedFiliere.value = filieres.value[0].id;
    }
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des données');
  } finally {
    loading.value = false;
  }
}

function handleFiliereChange() {
  fetchData(selectedFiliere.value);
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
    await fetchData(selectedFiliere.value);
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
    await fetchData(selectedFiliere.value);
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
</style>