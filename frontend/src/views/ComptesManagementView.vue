<template>
  <div class="comptes-management">
    <Toast />
    <ConfirmDialog />
    
    <div class="header-section">
      <div class="title-row">
        <h1 class="page-title">Gestion des Comptes</h1>
      </div>
      <div class="filter-row">
        <Dropdown 
          v-model="selectedRole" 
          :options="roleOptions" 
          optionLabel="label"
          optionValue="value" 
          placeholder="Filtrer par rôle" 
          class="role-dropdown"
          @change="handleRoleChange" 
        />
      </div>
    </div>
    
    <div class="action-bar">
      <div class="search-row">
        <span class="p-input-icon-left search-container">
          <i class="pi pi-search" />
          <InputText 
            v-model="searchQuery" 
            placeholder="Rechercher un compte..." 
            class="search-input"
          />
        </span>
      </div>
      <div class="button-row">
        <Button 
          label="Ajouter un compte" 
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
          :value="filteredComptes" 
          :loading="loading" 
          responsiveLayout="scroll"
          stripedRows 
          :paginator="filteredComptes.length > 10" 
          :rows="10" 
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
          :rowsPerPageOptions="[10, 20, 50]"
          currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} comptes"
          emptyMessage="Aucun compte trouvé"
        >
          <Column field="nom" header="Nom" sortable style="min-width: 10rem"></Column>
          <Column field="prenom" header="Prénom" sortable style="min-width: 10rem"></Column>
          <Column field="email" header="Email" sortable style="min-width: 15rem"></Column>
          <Column field="role" header="Rôle" sortable style="min-width: 8rem">
            <template #body="slotProps">
              {{ getRoleLabel(slotProps.data.role) }}
            </template>
          </Column>
          <Column field="cne" header="CNE" sortable style="min-width: 10rem">
            <template #body="slotProps">
              {{ slotProps.data.cne || '-' }}
            </template>
          </Column>
          <Column field="cni" header="CNI" sortable style="min-width: 10rem">
            <template #body="slotProps">
              {{ slotProps.data.cni || '-' }}
            </template>
          </Column>
          <Column field="filiereName" header="Filière" sortable style="min-width: 8rem">
            <template #body="slotProps">
              {{ slotProps.data.filiereName || '-' }}
            </template>
          </Column>
          <Column header="Actions" :exportable="false" style="min-width: 10rem">
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
    
    <!-- Add Compte Dialog -->
    <Dialog 
      v-model:visible="showAddModal" 
      header="Ajouter un Nouveau Compte" 
      :modal="true" 
      class="compte-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div class="field-container">
        <div class="p-field">
          <label for="role">Rôle <span class="required">*</span></label>
          <Dropdown 
            id="role" 
            v-model="newCompte.role" 
            :options="roleOptions" 
            optionLabel="label" 
            optionValue="value"
            placeholder="Sélectionner un rôle" 
            class="w-full"
            @change="handleNewCompteRoleChange"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="nom">Nom <span class="required">*</span></label>
          <InputText 
            id="nom" 
            v-model="newCompte.nom" 
            class="w-full" 
            placeholder="Nom de famille"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="prenom">Prénom <span class="required">*</span></label>
          <InputText 
            id="prenom" 
            v-model="newCompte.prenom" 
            class="w-full" 
            placeholder="Prénom"
            required
          />
        </div>
        
        <div v-if="newCompte.role === 'ETUDIANT'" class="p-field">
          <label for="cne">CNE <span class="required">*</span></label>
          <InputText 
            id="cne" 
            v-model="newCompte.cne" 
            class="w-full" 
            placeholder="CNE"
            required
          />
        </div>
        
        <div v-if="newCompte.role !== 'ETUDIANT'" class="p-field">
          <label for="cni">CNI <span class="required">*</span></label>
          <InputText 
            id="cni" 
            v-model="newCompte.cni" 
            class="w-full" 
            placeholder="CNI"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="dateNaissance">Date de naissance <span class="required">*</span></label>
          <Calendar 
            id="dateNaissance" 
            v-model="newCompte.dateNaissance" 
            :showIcon="true"
            dateFormat="dd/mm/yy"
            class="w-full" 
            placeholder="JJ/MM/AAAA"
            required
          />
        </div>
        
        <div v-if="newCompte.role === 'ETUDIANT'" class="p-field">
          <label for="filiereId">Filière <span class="required">*</span></label>
          <Dropdown 
            id="filiereId" 
            v-model="newCompte.filiereId" 
            :options="filieres" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner une filière" 
            class="w-full"
            required
          />
        </div>
        
        <div class="info-box">
          <i class="pi pi-info-circle"></i>
          <p>L'email sera généré automatiquement au format: prenomnom.efb@usms.ac.ma</p>
          <p>Le mot de passe sera généré aléatoirement et affiché dans la console.</p>
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
          @click="addCompte" 
          :loading="submitting"
        />
      </template>
    </Dialog>
    
    <!-- Edit Compte Dialog -->
    <Dialog 
      v-model:visible="showEditModal" 
      header="Modifier le Compte" 
      :modal="true" 
      class="compte-dialog"
      :style="{ width: '600px' }"
      :closable="false"
    >
      <div v-if="editingCompte" class="field-container">
        <div class="current-info">
          <p><strong>Rôle:</strong> {{ getRoleLabel(editingCompte.role) }}</p>
          <p><strong>Email:</strong> {{ editingCompte.email }}</p>
        </div>
        
        <div class="p-field">
          <label for="edit-nom">Nom <span class="required">*</span></label>
          <InputText 
            id="edit-nom" 
            v-model="editRequest.nom" 
            class="w-full" 
            placeholder="Nom de famille"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="edit-prenom">Prénom <span class="required">*</span></label>
          <InputText 
            id="edit-prenom" 
            v-model="editRequest.prenom" 
            class="w-full" 
            placeholder="Prénom"
            required
          />
        </div>
        
        <div v-if="editingCompte.role === 'ETUDIANT'" class="p-field">
          <label for="edit-cne">CNE <span class="required">*</span></label>
          <InputText 
            id="edit-cne" 
            v-model="editRequest.cne" 
            class="w-full" 
            placeholder="CNE"
            required
          />
        </div>
        
        <div v-if="editingCompte.role !== 'ETUDIANT'" class="p-field">
          <label for="edit-cni">CNI <span class="required">*</span></label>
          <InputText 
            id="edit-cni" 
            v-model="editRequest.cni" 
            class="w-full" 
            placeholder="CNI"
            required
          />
        </div>
        
        <div class="p-field">
          <label for="edit-dateNaissance">Date de naissance <span class="required">*</span></label>
          <Calendar 
            id="edit-dateNaissance" 
            v-model="editRequest.dateNaissance" 
            :showIcon="true"
            dateFormat="dd/mm/yy"
            class="w-full" 
            placeholder="JJ/MM/AAAA"
            required
          />
        </div>
        
        <div v-if="editingCompte.role === 'ETUDIANT'" class="p-field">
          <label for="edit-filiereId">Filière <span class="required">*</span></label>
          <Dropdown 
            id="edit-filiereId" 
            v-model="editRequest.filiereId" 
            :options="filieres" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner une filière" 
            class="w-full"
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
          @click="saveEditedCompte" 
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

// Component state
const comptes = ref([]);
const filieres = ref([]);
const selectedRole = ref(null);
const roleOptions = [
  { label: 'Tous', value: null },
  { label: 'Étudiants', value: 'ETUDIANT' },
  { label: 'Encadrants', value: 'ENCADRANT' },
  { label: 'Jurys', value: 'JURY' }
];

// Default date for today
const today = new Date();

const newCompte = ref({
  nom: '',
  prenom: '',
  cni: '',
  cne: '',
  dateNaissance: today,
  role: 'ETUDIANT',
  filiereId: null
});

const editingCompte = ref(null);
const editRequest = ref({
  nom: '',
  prenom: '',
  cni: '',
  cne: '',
  dateNaissance: null,
  filiereId: null
});

const showAddModal = ref(false);
const showEditModal = ref(false);
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref('');

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed filtered comptes
const filteredComptes = computed(() => {
  const query = searchQuery.value.toLowerCase();
  if (!query) return comptes.value;
  
  return comptes.value.filter(compte => 
    compte.nom.toLowerCase().includes(query) || 
    compte.prenom.toLowerCase().includes(query) ||
    compte.email.toLowerCase().includes(query) ||
    (compte.cne && compte.cne.toLowerCase().includes(query)) ||
    (compte.cni && compte.cni.toLowerCase().includes(query)) ||
    (compte.filiereName && compte.filiereName.toLowerCase().includes(query))
  );
});

// Fetch data on component mount
onMounted(async () => {
  await fetchData();
});

// Helper methods
function getRoleLabel(role) {
  switch (role) {
    case 'ETUDIANT': return 'Étudiant';
    case 'ENCADRANT': return 'Encadrant';
    case 'JURY': return 'Jury';
    case 'ADMIN': return 'Admin';
    case 'CHEF_DE_DEPARTEMENT': return 'Chef de Département';
    default: return role;
  }
}

// Methods for fetching data
async function fetchData(role = null) {
  loading.value = true;
  try {
    const endpoint = role 
      ? `/chef_de_departement/comptes?role=${role}`
      : '/chef_de_departement/comptes';
      
    const response = await ApiService.get(endpoint);
    
    comptes.value = response.comptes;
    filieres.value = response.filieres;
  } catch (error) {
    handleApiError(error, 'Erreur lors du chargement des données');
  } finally {
    loading.value = false;
  }
}

function handleRoleChange() {
  fetchData(selectedRole.value);
}

function handleNewCompteRoleChange() {
  // Reset fields specific to role
  if (newCompte.value.role === 'ETUDIANT') {
    newCompte.value.cni = '';
  } else {
    newCompte.value.cne = '';
    newCompte.value.filiereId = null;
  }
}

// CRUD operations
async function addCompte() {
  if (!validateCompteData(newCompte.value)) return;
  
  submitting.value = true;
  try {
    const response = await ApiService.post('/chef_de_departement/comptes', newCompte.value);
    comptes.value.push(response);
    
    // Reset form and close modal
    resetNewCompteForm();
    showAddModal.value = false;
    
    // Show success message
    toast.add({
      severity: 'success',
      summary: 'Compte ajouté',
      detail: 'Le compte a été ajouté avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, "Erreur lors de l'ajout du compte");
  } finally {
    submitting.value = false;
  }
}

async function saveEditedCompte() {
  if (!validateEditCompteData(editRequest.value, editingCompte.value.role)) return;
  
  submitting.value = true;
  try {
    const response = await ApiService.put(`/chef_de_departement/comptes/${editingCompte.value.id}`, editRequest.value);
    
    // Update local data
    const index = comptes.value.findIndex(c => c.id === editingCompte.value.id);
    if (index !== -1) {
      comptes.value[index] = response;
    }
    
    showEditModal.value = false;
    
    toast.add({
      severity: 'success',
      summary: 'Compte mis à jour',
      detail: 'Le compte a été modifié avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de la modification du compte');
  } finally {
    submitting.value = false;
  }
}

function confirmDelete(compte) {
  confirm.require({
    message: `Êtes-vous sûr de vouloir supprimer le compte de ${compte.prenom} ${compte.nom}?`,
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => deleteCompte(compte),
    reject: () => {/* do nothing */}
  });
}

async function deleteCompte(compte) {
  try {
    await ApiService.delete(`/chef_de_departement/comptes/${compte.id}`);
    
    // Update local data
    comptes.value = comptes.value.filter(c => c.id !== compte.id);
    
    toast.add({
      severity: 'success',
      summary: 'Compte supprimé',
      detail: 'Le compte a été supprimé avec succès',
      life: 3000
    });
  } catch (error) {
    handleApiError(error, 'Erreur lors de la suppression du compte');
  }
}

// UI control methods
function openAddModal() {
  resetNewCompteForm();
  showAddModal.value = true;
}

function openEditModal(compte) {
  editingCompte.value = { ...compte };
  
  // Format the dateNaissance as a Date object
  const dateNaissance = compte.dateNaissance ? new Date(compte.dateNaissance) : null;
  
  // Set edit request values
  editRequest.value = {
    nom: compte.nom,
    prenom: compte.prenom,
    cni: compte.cni || '',
    cne: compte.cne || '',
    dateNaissance: dateNaissance,
    filiereId: null
  };
  
  // For students, find the filiere ID based on the filiere name
  if (compte.role === 'ETUDIANT' && compte.filiereName) {
    const filiere = filieres.value.find(f => f.nom === compte.filiereName);
    if (filiere) {
      editRequest.value.filiereId = filiere.id;
    }
  }
  
  showEditModal.value = true;
}

function closeModals() {
  showAddModal.value = false;
  showEditModal.value = false;
}

function resetNewCompteForm() {
  newCompte.value = {
    nom: '',
    prenom: '',
    cni: '',
    cne: '',
    dateNaissance: today,
    role: 'ETUDIANT',
    filiereId: null
  };
}

// Validation
function validateCompteData(compte) {
  if (!compte.nom || compte.nom.trim() === '') {
    showValidationError("Le nom est obligatoire");
    return false;
  }
  
  if (!compte.prenom || compte.prenom.trim() === '') {
    showValidationError("Le prénom est obligatoire");
    return false;
  }
  
  if (!compte.dateNaissance) {
    showValidationError("La date de naissance est obligatoire");
    return false;
  }
  
  if (!compte.role) {
    showValidationError("Le rôle est obligatoire");
    return false;
  }
  
  if (compte.role === 'ETUDIANT') {
    if (!compte.cne || compte.cne.trim() === '') {
      showValidationError("Le CNE est obligatoire pour un étudiant");
      return false;
    }
    
    if (!compte.filiereId) {
      showValidationError("La filière est obligatoire pour un étudiant");
      return false;
    }
  } else {
    if (!compte.cni || compte.cni.trim() === '') {
      showValidationError("Le CNI est obligatoire pour un encadrant ou un jury");
      return false;
    }
  }
  
  return true;
}

function validateEditCompteData(compte, role) {
  if (!compte.nom || compte.nom.trim() === '') {
    showValidationError("Le nom est obligatoire");
    return false;
  }
  
  if (!compte.prenom || compte.prenom.trim() === '') {
    showValidationError("Le prénom est obligatoire");
    return false;
  }
  
  if (!compte.dateNaissance) {
    showValidationError("La date de naissance est obligatoire");
    return false;
  }
  
  if (role === 'ETUDIANT') {
    if (!compte.cne || compte.cne.trim() === '') {
      showValidationError("Le CNE est obligatoire pour un étudiant");
      return false;
    }
    
    if (!compte.filiereId) {
      showValidationError("La filière est obligatoire pour un étudiant");
      return false;
    }
  } else {
    if (!compte.cni || compte.cni.trim() === '') {
      showValidationError("Le CNI est obligatoire pour un encadrant ou un jury");
      return false;
    }
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
.comptes-management {
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

.role-dropdown {
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

.info-box {
  background-color: var(--surface-hover);
  border-radius: 6px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  margin-top: 1rem;
}

.info-box i {
  color: var(--primary-color);
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.info-box p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .comptes-management {
    padding: 1rem;
  }
  
  .title-row, .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .role-dropdown {
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