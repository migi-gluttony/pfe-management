<template>
    <div class="sujet-management">
      <Toast />
      <ConfirmDialog />
      
      <div class="header-section">
        <h1 class="page-title">
          Gestion des Sujets PFE
          <Dropdown 
            v-model="selectedClass" 
            :options="classes" 
            optionLabel="nom" 
            optionValue="id"
            placeholder="Sélectionner une filière" 
            class="class-dropdown" 
          />
        </h1>
      </div>
      
      <div class="action-bar">
        <span class="p-input-icon-left search-container">
          <i class="pi pi-search" />
          <InputText 
            v-model="searchQuery" 
            placeholder="Rechercher un sujet..." 
            class="search-input"
          />
        </span>
        <Button 
          label="Ajouter un sujet" 
          icon="pi pi-plus" 
          class="p-button-primary add-btn"
          @click="openAddModal" 
        />
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
          
          <div class="p-field">
            <label for="filiere">Filière</label>
            <Dropdown 
              id="filiere" 
              v-model="newSujet.filiere.id" 
              :options="classes" 
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
          
          <div class="p-field">
            <label for="edit-filiere">Filière</label>
            <Dropdown 
              id="edit-filiere" 
              v-model="editingSujet.filiere.id" 
              :options="classes" 
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
            <p>{{ getClassLabel(viewingSujet.filiere?.id) }}</p>
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
  import ApiService from '@/services/ApiService';
  
  // Component state
  const sujets = ref([]);
  const classes = ref([]);
  const selectedClass = ref(null);
  const newSujet = ref({ 
    titre: '', 
    theme: '', 
    description: '', 
    filiere: { id: null }
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
      filtered = filtered.filter(sujet => sujet.filiere && sujet.filiere.id === selectedClass.value);
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
    await Promise.all([
      fetchSujets(),
      fetchClasses()
    ]);
    
    // Set default selected class
    if (classes.value.length > 0) {
      selectedClass.value = classes.value[0].id;
    }
  });
  
  // Methods for fetching data
  async function fetchSujets() {
    loading.value = true;
    try {
      const response = await ApiService.get('/sujet');
      sujets.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des sujets');
    } finally {
      loading.value = false;
    }
  }
  
  async function fetchClasses() {
    try {
      const response = await ApiService.get('/filiere');
      classes.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des filières');
    }
  }
  
  // Helper methods for data display
  function getClassLabel(id) {
    if (!id) return 'N/A';
    const cls = classes.value.find(c => c.id === id);
    return cls ? cls.nom : 'N/A';
  }
  
  // CRUD operations
  async function addSujet() {
    if (!validateSujetData(newSujet.value)) return;
    
    submitting.value = true;
    try {
      // Prepare filiere object if just the ID is selected
      if (typeof newSujet.value.filiere === 'number') {
        newSujet.value.filiere = { id: newSujet.value.filiere };
      }
      
      const response = await ApiService.post('/sujet', newSujet.value);
      sujets.value.push(response);
      
      // Reset form and close modal
      newSujet.value = { 
        titre: '', 
        theme: '', 
        description: '', 
        filiere: { id: null }
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
      // Prepare filiere object if just the ID is selected
      if (typeof editingSujet.value.filiere === 'number') {
        editingSujet.value.filiere = { id: editingSujet.value.filiere };
      }
      
      const response = await ApiService.put(`/sujet/${editingSujet.value.id}`, editingSujet.value);
      
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
      await ApiService.delete(`/sujet/${sujet.id}`);
      
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
      filiere: { id: selectedClass.value }
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
    
    if (!sujet.filiere || !sujet.filiere.id) {
      showValidationError('La filière est obligatoire');
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
    
    .action-bar {
      flex-direction: column;
    }
    
    .action-buttons {
      flex-direction: column;
    }
  }
  </style>