<template>
  <div class="sujet-management">
    <Toast />
    <ConfirmDialog />
    
    <!-- User Info Header -->
    <UserInfoHeader 
      searchPlaceholder="Rechercher un sujet..." 
      :initialSearchValue="searchQuery"
      @search="handleHeaderSearch" 
    />

    <!-- Sujet Header -->
    <SujetHeader
      v-model:selectedClass="selectedClass"
      :classes="classes"
      @add="openAddModal"
    />
    
    <!-- Sujet List -->
    <SujetList
      :sujets="sujets"
      :selectedClass="selectedClass"
      :searchQuery="searchQuery"
      :loading="loading"
      :classes="classes"
      @view="openViewModal"
      @edit="openEditModal"
      @delete="confirmDelete"
    />

    <!-- Add Sujet Modal -->
    <SujetAddModal
      v-model:visible="showAddModal"
      :selectedClass="selectedClass"
      :submitting="submitting"
      @submit="addSujet"
      @cancel="closeModals"
    />
    
    <!-- Edit Sujet Modal -->
    <SujetEditModal
      v-model:visible="showEditModal"
      :sujet="editingSujet"
      :submitting="submitting"
      @submit="saveEditedSujet"
      @cancel="closeModals"
    />
    
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
import { ref, onMounted, watch } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import ApiService from '@/services/ApiService';
import UserInfoHeader from '@/components/UserInfoHeader.vue';
import SujetHeader from '@/components/chefDeDepartement/sujetManagement/SujetHeader.vue';
import SujetList from '@/components/chefDeDepartement/sujetManagement/SujetList.vue';
import SujetAddModal from '@/components/chefDeDepartement/sujetManagement/SujetAddModal.vue';
import SujetEditModal from '@/components/chefDeDepartement/sujetManagement/SujetEditModal.vue';

// Import PrimeVue components
import Toast from 'primevue/toast';
import ConfirmDialog from 'primevue/confirmdialog';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';

// Component state
const sujets = ref([]);
const classes = ref([]);
const selectedClass = ref(null);
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

// Fetch data on component mount
onMounted(async () => {
  await fetchData();
});

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
  searchQuery.value = query;
}

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
async function addSujet(newSujet) {
  if (!validateSujetData(newSujet)) return;
  
  submitting.value = true;
  try {
    // Make sure filiereId is set to the selected filiere
    newSujet.filiereId = selectedClass.value;
    
    const response = await ApiService.post('/chef_de_departement/sujets', newSujet);
    sujets.value.push(response);
    
    // Reset form and close modal
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

async function saveEditedSujet(editedSujet) {
  if (!validateSujetData(editedSujet)) return;
  
  submitting.value = true;
  try {
    // Only send title, theme, and description for editing
    const editRequest = {
      titre: editedSujet.titre,
      theme: editedSujet.theme,
      description: editedSujet.description
    };
    
    const response = await ApiService.put(`/chef_de_departement/sujets/${editedSujet.id}`, editRequest);
    
    // Update local data
    const index = sujets.value.findIndex(s => s.id === editedSujet.id);
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
  margin: 0 auto;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
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
</style>