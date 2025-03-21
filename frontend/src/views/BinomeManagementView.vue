<template>
  <div class="binome-management">
    <Toast />
    <ConfirmDialog />
    
    <div class="header-section">
      <h1 class="page-title">
        Gestion des Binômes
        <Dropdown 
          v-model="selectedClass" 
          :options="classes" 
          optionLabel="filiere" 
          optionValue="id"
          placeholder="Sélectionner une classe" 
          class="class-dropdown" 
        />
      </h1>
    </div>
    
    <div class="action-bar">
      <span class="p-input-icon-left search-container">
        <i class="pi pi-search" />
        <InputText 
          v-model="searchQuery" 
          placeholder="Rechercher un binôme..." 
          class="search-input"
        />
      </span>
      <Button 
        label="Ajouter un binôme" 
        icon="pi pi-plus" 
        class="p-button-primary add-btn"
        @click="openAddModal" 
      />
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
          <Column field="etudiant1" header="Étudiant 1">
            <template #body="slotProps">
              {{ getStudentName(slotProps.data.etudiant1_id) }}
            </template>
          </Column>
          <Column field="etudiant2" header="Étudiant 2">
            <template #body="slotProps">
              {{ getStudentName(slotProps.data.etudiant2_id) }}
            </template>
          </Column>
          <Column field="encadrant" header="Encadrant">
            <template #body="slotProps">
              {{ getSupervisorName(slotProps.data.encadrant_id) }}
            </template>
          </Column>
          <Column field="sujet" header="Sujet">
            <template #body="slotProps">
              {{ getSubjectTitle(slotProps.data.sujet_id) }}
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

    <!-- Add Binome Dialog -->
    <Dialog 
      v-model:visible="showAddModal" 
      header="Ajouter un Nouveau Binôme" 
      :modal="true" 
      class="binome-dialog"
      :style="{ width: '500px' }"
      :closable="false"
    >
      <div class="field-container">
        <div class="p-field">
          <label for="etudiant1">Étudiant 1</label>
          <Dropdown 
            id="etudiant1" 
            v-model="newBinome.etudiant1_id" 
            :options="etudiants" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner un étudiant" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un étudiant"
          >
            <template #option="slotProps">
              {{ slotProps.option.prenom }} {{ slotProps.option.nom }}
            </template>
            <template #selectedOption="slotProps">
              {{ getStudentName(slotProps.value) }}
            </template>
          </Dropdown>
        </div>
        
        <div class="p-field">
          <label for="etudiant2">Étudiant 2</label>
          <Dropdown 
            id="etudiant2" 
            v-model="newBinome.etudiant2_id" 
            :options="etudiants" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner un étudiant (optionnel)" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un étudiant"
          >
            <template #option="slotProps">
              {{ slotProps.option.prenom }} {{ slotProps.option.nom }}
            </template>
            <template #selectedOption="slotProps">
              {{ getStudentName(slotProps.value) }}
            </template>
          </Dropdown>
        </div>
        
        <div class="p-field">
          <label for="encadrant">Encadrant</label>
          <Dropdown 
            id="encadrant" 
            v-model="newBinome.encadrant_id" 
            :options="enseignants" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner un encadrant" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un encadrant"
          >
            <template #option="slotProps">
              {{ slotProps.option.prenom }} {{ slotProps.option.nom }}
            </template>
            <template #selectedOption="slotProps">
              {{ getSupervisorName(slotProps.value) }}
            </template>
          </Dropdown>
        </div>
        
        <div class="p-field">
          <label for="sujet">Sujet</label>
          <Dropdown 
            id="sujet" 
            v-model="newBinome.sujet_id" 
            :options="sujets" 
            optionLabel="titre" 
            optionValue="id"
            placeholder="Sélectionner un sujet" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un sujet"
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
    
    <!-- Edit Binome Dialog -->
    <Dialog 
      v-model:visible="showEditModal" 
      header="Modifier le Binôme" 
      :modal="true" 
      class="binome-dialog"
      :style="{ width: '500px' }"
      :closable="false"
    >
      <div v-if="editingBinome" class="field-container">
        <div class="p-field">
          <label for="edit-etudiant1">Étudiant 1</label>
          <Dropdown 
            id="edit-etudiant1" 
            v-model="editingBinome.etudiant1_id" 
            :options="etudiants" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner un étudiant" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un étudiant"
          >
            <template #option="slotProps">
              {{ slotProps.option.prenom }} {{ slotProps.option.nom }}
            </template>
            <template #selectedOption="slotProps">
              {{ getStudentName(slotProps.value) }}
            </template>
          </Dropdown>
        </div>
        
        <div class="p-field">
          <label for="edit-etudiant2">Étudiant 2</label>
          <Dropdown 
            id="edit-etudiant2" 
            v-model="editingBinome.etudiant2_id" 
            :options="etudiants" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner un étudiant (optionnel)" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un étudiant"
          >
            <template #option="slotProps">
              {{ slotProps.option.prenom }} {{ slotProps.option.nom }}
            </template>
            <template #selectedOption="slotProps">
              {{ getStudentName(slotProps.value) }}
            </template>
          </Dropdown>
        </div>
        
        <div class="p-field">
          <label for="edit-encadrant">Encadrant</label>
          <Dropdown 
            id="edit-encadrant" 
            v-model="editingBinome.encadrant_id" 
            :options="enseignants" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner un encadrant" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un encadrant"
          >
            <template #option="slotProps">
              {{ slotProps.option.prenom }} {{ slotProps.option.nom }}
            </template>
            <template #selectedOption="slotProps">
              {{ getSupervisorName(slotProps.value) }}
            </template>
          </Dropdown>
        </div>
        
        <div class="p-field">
          <label for="edit-sujet">Sujet</label>
          <Dropdown 
            id="edit-sujet" 
            v-model="editingBinome.sujet_id" 
            :options="sujets" 
            optionLabel="titre" 
            optionValue="id"
            placeholder="Sélectionner un sujet" 
            class="w-full" 
            :filter="true"
            filterPlaceholder="Rechercher un sujet"
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
import { ref, onMounted, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import ApiService from '@/services/ApiService';

// PrimeVue components are globally registered in main.js

// Component state
const binomes = ref([]);
const enseignants = ref([]);
const etudiants = ref([]);
const sujets = ref([]);
const classes = ref([]);
const selectedClass = ref(null);
const newBinome = ref({ 
  etudiant1_id: null, 
  etudiant2_id: null, 
  encadrant_id: null, 
  sujet_id: null,
  class_id: null
});
const editingBinome = ref(null);
const showEditModal = ref(false);
const showAddModal = ref(false);
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref('');

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed filtered binomes
const filteredBinomes = computed(() => {
  const query = searchQuery.value.toLowerCase();
  let filtered = binomes.value;

  // Filter by selected class
  if (selectedClass.value) {
    filtered = filtered.filter(binome => binome.class_id === selectedClass.value);
  }

  // Filter by search query
  if (query) {
    filtered = filtered.filter(binome => {
      const etudiant1 = getStudentName(binome.etudiant1_id).toLowerCase();
      const etudiant2 = getStudentName(binome.etudiant2_id).toLowerCase();
      const encadrant = getSupervisorName(binome.encadrant_id).toLowerCase();
      const sujet = getSubjectTitle(binome.sujet_id).toLowerCase();
      
      return (
        etudiant1.includes(query) || 
        etudiant2.includes(query) ||
        encadrant.includes(query) ||
        sujet.includes(query)
      );
    });
  }

  return filtered;
});

// Fetch data on component mount
onMounted(async () => {
  await Promise.all([
    fetchBinomes(),
    fetchEnseignants(),
    fetchEtudiants(),
    fetchSujets(),
    fetchClasses()
  ]);
  
  // Set default selected class
  if (classes.value.length > 0) {
    selectedClass.value = classes.value[0].id;
  }
});

// Methods for fetching data
async function fetchBinomes() {
  loading.value = true;
  try {
    const response = await ApiService.get('/binomes');
    binomes.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des binômes');
  } finally {
    loading.value = false;
  }
}

async function fetchEnseignants() {
  try {
    const response = await ApiService.get('/utilisateurs', { role: 'ENCADRANT' });
    enseignants.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des enseignants');
  }
}

async function fetchEtudiants() {
  try {
    const response = await ApiService.get('/utilisateurs', { role: 'ETUDIANT' });
    etudiants.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des étudiants');
  }
}

async function fetchSujets() {
  try {
    const response = await ApiService.get('/sujets');
    sujets.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des sujets');
  }
}

async function fetchClasses() {
  try {
    const response = await ApiService.get('/filieres');
    classes.value = response;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des classes');
  }
}

// Helper methods for data display
function getStudentName(id) {
  if (!id) return 'N/A';
  const student = etudiants.value.find(e => e.id === id);
  return student ? `${student.prenom} ${student.nom}` : 'N/A';
}

function getSupervisorName(id) {
  if (!id) return 'N/A';
  const supervisor = enseignants.value.find(e => e.id === id);
  return supervisor ? `${supervisor.prenom} ${supervisor.nom}` : 'N/A';
}

function getSubjectTitle(id) {
  if (!id) return 'N/A';
  const subject = sujets.value.find(s => s.id === id);
  return subject ? subject.titre : 'N/A';
}

// CRUD operations
async function addBinome() {
  if (!validateBinomeData(newBinome.value)) return;
  
  submitting.value = true;
  try {
    // Set class_id from selected class
    newBinome.value.class_id = selectedClass.value;
    
    const response = await ApiService.post('/binomes', newBinome.value);
    binomes.value.push(response);
    
    // Reset form and close modal
    newBinome.value = { 
      etudiant1_id: null, 
      etudiant2_id: null, 
      encadrant_id: null, 
      sujet_id: null,
      class_id: null 
    };
    showAddModal.value = false;
    
    // Show success message
    toast.add({
      severity: 'success',
      summary: 'Binôme ajouté',
      detail: 'Le binôme a été ajouté avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, "Erreur lors de l'ajout du binôme");
  } finally {
    submitting.value = false;
  }
}

async function saveEditedBinome() {
  if (!validateBinomeData(editingBinome.value)) return;
  
  submitting.value = true;
  try {
    const response = await ApiService.put(`/binomes/${editingBinome.value.id}`, editingBinome.value);
    
    // Update local data
    const index = binomes.value.findIndex(b => b.id === editingBinome.value.id);
    if (index !== -1) {
      binomes.value[index] = response;
    }
    
    showEditModal.value = false;
    
    toast.add({
      severity: 'success',
      summary: 'Binôme mis à jour',
      detail: 'Le binôme a été modifié avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de la modification du binôme');
  } finally {
    submitting.value = false;
  }
}

function confirmDelete(binome) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir supprimer le binôme de ${getStudentName(binome.etudiant1_id)} ${binome.etudiant2_id ? 'et ' + getStudentName(binome.etudiant2_id) : ''}?`,
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => deleteBinome(binome),
    reject: () => {/* do nothing */}
  });
}

async function deleteBinome(binome) {
  try {
    await ApiService.delete(`/binomes/${binome.id}`);
    
    // Update local data
    binomes.value = binomes.value.filter(b => b.id !== binome.id);
    
    toast.add({
      severity: 'success',
      summary: 'Binôme supprimé',
      detail: 'Le binôme a été supprimé avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de la suppression du binôme');
  }
}

// UI control methods
function openAddModal() {
  newBinome.value = { 
    etudiant1_id: null, 
    etudiant2_id: null, 
    encadrant_id: null, 
    sujet_id: null,
    class_id: selectedClass.value
  };
  showAddModal.value = true;
}

function openEditModal(binome) {
  editingBinome.value = { ...binome };
  showEditModal.value = true;
}

function closeModals() {
  showEditModal.value = false;
  showAddModal.value = false;
}

// Validation
function validateBinomeData(binome) {
  if (!binome.etudiant1_id) {
    showValidationError("L'étudiant 1 est obligatoire");
    return false;
  }
  
  if (!binome.encadrant_id) {
    showValidationError("L'encadrant est obligatoire");
    return false;
  }
  
  if (!binome.sujet_id) {
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  gap: 1rem;
}

.search-container {
  flex: 1;
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

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .binome-management {
    padding: 1rem;
  }
  
  .page-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .action-bar {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>