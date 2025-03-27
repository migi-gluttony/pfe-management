<template>
  <div class="sujet-management">
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
    <h1 class="page-title">Gestion des Sujets PFE</h1>
    <Dropdown 
      v-model="selectedClass" 
      :options="classes" 
      optionLabel="nom" 
      optionValue="id"
      placeholder="Sélectionner une filière" 
      class="filter-dropdown" 
    />
  </div>
  <div class="action-buttons">
    <Button 
      label="Ajouter un sujet" 
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
          :value="filteredSujets" 
          :loading="loading" 
          responsiveLayout="scroll"
          stripedRows 
          :paginator="filteredSujets.length > 10" 
          :rows="10" 
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
          :rowsPerPageOptions="[10, 20, 50]"
          currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} sujets"
          emptyMessage="Aucun sujet trouvé"
        >
          <Column field="titre" header="Titre" sortable style="min-width: 12rem"></Column>
          <Column field="theme" header="Thème" sortable style="min-width: 8rem"></Column>
          <Column field="description" header="Description" style="min-width: 20rem">
            <template #body="slotProps">
              <div class="description-text">
                {{ slotProps.data.description }}
              </div>
            </template>
          </Column>
          <Column header="Actions" :exportable="false" style="min-width: 8rem">
            <template #body="slotProps">
              <div class="action-buttons">
                <Button 
                  icon="pi pi-pencil" 
                  class="p-button-rounded p-button-outlined p-button-info mr-2" 
                  @click="openEditModal(slotProps.data)" 
                  tooltip="Modifier"
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

    <!-- Add Sujet Dialog -->
    <Dialog 
      v-model:visible="showAddModal" 
      header="Ajouter un Nouveau Sujet" 
      :modal="true" 
      class="sujet-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div class="field-container">
        <div class="p-field">
          <label for="titre">Titre</label>
          <InputText 
            id="titre" 
            v-model="newSujet.titre" 
            class="w-full" 
            placeholder="Titre du sujet"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="theme">Thème</label>
          <InputText 
            id="theme" 
            v-model="newSujet.theme" 
            class="w-full" 
            placeholder="Thème du sujet"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="description">Description</label>
          <Textarea 
            id="description" 
            v-model="newSujet.description" 
            rows="5" 
            class="w-full" 
            placeholder="Description détaillée du sujet"
            required
            autoResize
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
          @click="addSujet" 
          :loading="submitting"
        />
      </template>
    </Dialog>
    
    <!-- Edit Sujet Dialog -->
    <Dialog 
      v-model:visible="showEditModal" 
      header="Modifier le Sujet" 
      :modal="true" 
      class="sujet-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div v-if="editingSujet" class="field-container">
        <div class="p-field">
          <label for="edit-titre">Titre</label>
          <InputText 
            id="edit-titre" 
            v-model="editingSujet.titre" 
            class="w-full" 
            placeholder="Titre du sujet"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="edit-theme">Thème</label>
          <InputText 
            id="edit-theme" 
            v-model="editingSujet.theme" 
            class="w-full" 
            placeholder="Thème du sujet"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="edit-description">Description</label>
          <Textarea 
            id="edit-description" 
            v-model="editingSujet.description" 
            rows="5" 
            class="w-full" 
            placeholder="Description détaillée du sujet"
            required
            autoResize
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
          @click="saveEditedSujet" 
          :loading="submitting"
        />
      </template>
    </Dialog>
    
    <!-- View Details Dialog -->
    <Dialog
      v-model:visible="showViewModal"
      :header="viewingSujet?.titre"
      :modal="true"
      class="sujet-dialog"
      :style="{ width: '600px' }"
    >
      <div v-if="viewingSujet" class="sujet-details">
        <div class="detail-item">
          <h3>Thème</h3>
          <p>{{ viewingSujet.theme }}</p>
        </div>
        
        <div class="detail-item">
          <h3>Description</h3>
          <p>{{ viewingSujet.description }}</p>
        </div>
        
        <div class="detail-item">
          <h3>Filière</h3>
          <p>{{ viewingSujet.filiereName }}</p>
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
import Textarea from 'primevue/textarea';

// Component state
const sujets = ref([]);
const classes = ref([]);
const selectedClass = ref(null);
const newSujet = ref({ 
  titre: '', 
  theme: '', 
  description: ''
});
const editingSujet = ref(null);
const viewingSujet = ref(null);
const showEditModal = ref(false);
const showAddModal = ref(false);
const showViewModal = ref(false);
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref('');

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed filtered sujets
const filteredSujets = computed(() => {
  const query = searchQuery.value.toLowerCase();
  let filtered = sujets.value;

  // Filter by selected class
  if (selectedClass.value) {
    const selectedFiliereNom = classes.value.find(c => c.id === selectedClass.value)?.nom;
    filtered = filtered.filter(sujet => sujet.filiereName === selectedFiliereNom);
  }

  // Filter by search query
  if (query) {
    filtered = filtered.filter(sujet => 
      sujet.titre.toLowerCase().includes(query) || 
      sujet.theme.toLowerCase().includes(query) ||
      sujet.description.toLowerCase().includes(query)
    );
  }

  return filtered;
});

// Fetch data on component mount
onMounted(async () => {
  await fetchData();
});

// Watch for changes in selectedClass to update newSujet.filiereId
watch(selectedClass, (newValue) => {
  if (newValue) {
    newSujet.value.filiereId = newValue;
  }
});

// Methods for fetching data
async function fetchData() {
  loading.value = true;
  try {
    const response = await ApiService.get('/chef_de_departement/sujets');
    sujets.value = response.sujets;
    classes.value = response.filieres;
    
    // Set default selected class
    if (classes.value.length > 0 && !selectedClass.value) {
      selectedClass.value = classes.value[0].id;
    }
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des données');
  } finally {
    loading.value = false;
  }
}

// CRUD operations
async function addSujet() {
  if (!validateSujetData(newSujet.value)) return;
  
  submitting.value = true;
  try {
    // Make sure filiereId is set to the selected filiere
    newSujet.value.filiereId = selectedClass.value;
    
    const response = await ApiService.post('/chef_de_departement/sujets', newSujet.value);
    sujets.value.push(response);
    
    // Reset form and close modal
    newSujet.value = { 
      titre: '', 
      theme: '', 
      description: ''
    };
    showAddModal.value = false;
    
    // Show success message
    toast.add({
      severity: 'success',
      summary: 'Sujet ajouté',
      detail: 'Le sujet a été ajouté avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, "Erreur lors de l'ajout du sujet");
  } finally {
    submitting.value = false;
  }
}

async function saveEditedSujet() {
  if (!validateSujetData(editingSujet.value)) return;
  
  submitting.value = true;
  try {
    // Only send title, theme, and description for editing
    const editRequest = {
      titre: editingSujet.value.titre,
      theme: editingSujet.value.theme,
      description: editingSujet.value.description
    };
    
    const response = await ApiService.put(`/chef_de_departement/sujets/${editingSujet.value.id}`, editRequest);
    
    // Update local data
    const index = sujets.value.findIndex(s => s.id === editingSujet.value.id);
    if (index !== -1) {
      sujets.value[index] = response;
    }
    
    showEditModal.value = false;
    
    toast.add({
      severity: 'success',
      summary: 'Sujet mis à jour',
      detail: 'Le sujet a été modifié avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de la modification du sujet');
  } finally {
    submitting.value = false;
  }
}

function confirmDelete(sujet) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir supprimer le sujet "${sujet.titre}"?`,
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => deleteSujet(sujet),
    reject: () => {/* do nothing */}
  });
}

async function deleteSujet(sujet) {
  try {
    await ApiService.delete(`/chef_de_departement/sujets/${sujet.id}`);
    
    // Update local data
    sujets.value = sujets.value.filter(s => s.id !== sujet.id);
    
    toast.add({
      severity: 'success',
      summary: 'Sujet supprimé',
      detail: 'Le sujet a été supprimé avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de la suppression du sujet');
  }
}

// UI control methods
function openAddModal() {
  newSujet.value = { 
    titre: '', 
    theme: '', 
    description: '',
    filiereId: selectedClass.value
  };
  showAddModal.value = true;
}

function openEditModal(sujet) {
  // Create a deep copy to avoid modifying the original object
  editingSujet.value = JSON.parse(JSON.stringify(sujet));
  showEditModal.value = true;
}

function openViewModal(sujet) {
  viewingSujet.value = sujet;
  showViewModal.value = true;
}

function closeModals() {
  showEditModal.value = false;
  showAddModal.value = false;
}

// Validation
function validateSujetData(sujet) {
  if (!sujet.titre || sujet.titre.trim() === '') {
    showValidationError('Le titre du sujet est obligatoire');
    return false;
  }
  
  if (!sujet.theme || sujet.theme.trim() === '') {
    showValidationError('Le thème du sujet est obligatoire');
    return false;
  }
  
  if (!sujet.description || sujet.description.trim() === '') {
    showValidationError('La description du sujet est obligatoire');
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
.sujet-management {
  /* max-width: 1200px; */
  margin: 0 auto;
  /* padding: 2rem; */
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

.class-dropdown {
  display: inline-flex;
  min-width: 200px;
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

.description-text {
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
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

.sujet-details .detail-item {
  margin-bottom: 1.5rem;
}

.sujet-details h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--primary-color);
}

.sujet-details p {
  margin: 0;
  line-height: 1.5;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .sujet-management {
    padding: 1rem;
  }
  
  .page-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .button-row {
    justify-content: center;
  }
  
  .add-btn {
    width: 100%;
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