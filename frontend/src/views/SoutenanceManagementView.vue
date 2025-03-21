<template>
    <div class="soutenance-management">
      <Toast />
      <ConfirmDialog />
      
      <div class="header-section">
        <h1 class="page-title">
          Gestion des Soutenances
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
            placeholder="Rechercher une soutenance..." 
            class="search-input"
          />
        </span>
        <Button 
          label="Ajouter une soutenance" 
          icon="pi pi-plus" 
          class="p-button-primary add-btn"
          @click="openAddModal" 
        />
      </div>
  
      <!-- Data Table with PrimeVue -->
      <Card class="table-card">
        <template #content>
          <DataTable 
            :value="filteredSoutenances" 
            :loading="loading" 
            responsiveLayout="scroll"
            stripedRows 
            :paginator="filteredSoutenances.length > 10" 
            :rows="10" 
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
            :rowsPerPageOptions="[10, 20, 50]"
            currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} soutenances"
            emptyMessage="Aucune soutenance trouvée"
          >
            <Column field="date" header="Date" sortable style="min-width: 10rem">
              <template #body="slotProps">
                {{ formatDate(slotProps.data.date) }}
              </template>
            </Column>
            <Column field="heure" header="Heure" sortable style="min-width: 8rem"></Column>
            <Column field="salle.nom" header="Salle" sortable style="min-width: 8rem"></Column>
            <Column header="Binôme" style="min-width: 12rem">
              <template #body="slotProps">
                {{ getBinomeLabels(slotProps.data.binome) }}
              </template>
            </Column>
            <Column field="jury1.nom" header="Jury 1" sortable style="min-width: 10rem">
              <template #body="slotProps">
                {{ getJuryName(slotProps.data.jury1) }}
              </template>
            </Column>
            <Column field="jury2.nom" header="Jury 2" sortable style="min-width: 10rem">
              <template #body="slotProps">
                {{ getJuryName(slotProps.data.jury2) }}
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
  
      <!-- Add Soutenance Dialog -->
      <Dialog 
        v-model:visible="showAddModal" 
        header="Ajouter une Nouvelle Soutenance" 
        :modal="true" 
        class="soutenance-dialog"
        :style="{ width: '600px' }"
        :closable="false"
      >
        <div class="field-container">
          <div class="p-field">
            <label for="binome">Binôme</label>
            <Dropdown 
              id="binome" 
              v-model="newSoutenance.binome.id" 
              :options="binomes" 
              optionLabel="label" 
              optionValue="id"
              placeholder="Sélectionner un binôme" 
              class="w-full"
              required
              :filter="true"
              filterPlaceholder="Rechercher un binôme"
            />
          </div>
          
          <div class="p-field">
            <label for="date">Date</label>
            <Calendar 
              id="date" 
              v-model="newSoutenance.date" 
              dateFormat="dd/mm/yy"
              class="w-full" 
              placeholder="Date de soutenance"
              required
              :minDate="today"
            />
          </div>
          
          <div class="p-field">
            <label for="heure">Heure</label>
            <Calendar 
              id="heure" 
              v-model="newSoutenance.heure" 
              timeOnly 
              hourFormat="24" 
              class="w-full" 
              placeholder="Heure de soutenance"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="salle">Salle</label>
            <Dropdown 
              id="salle" 
              v-model="newSoutenance.salle.id" 
              :options="salles" 
              optionLabel="nom" 
              optionValue="id"
              placeholder="Sélectionner une salle" 
              class="w-full"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="jury1">Jury 1</label>
            <Dropdown 
              id="jury1" 
              v-model="newSoutenance.jury1.id" 
              :options="jurys" 
              optionLabel="fullName" 
              optionValue="id"
              placeholder="Sélectionner le premier membre du jury" 
              class="w-full"
              required
              :filter="true"
              filterPlaceholder="Rechercher un membre du jury"
            />
          </div>
          
          <div class="p-field">
            <label for="jury2">Jury 2</label>
            <Dropdown 
              id="jury2" 
              v-model="newSoutenance.jury2.id" 
              :options="jurys" 
              optionLabel="fullName" 
              optionValue="id"
              placeholder="Sélectionner le second membre du jury" 
              class="w-full"
              required
              :filter="true"
              filterPlaceholder="Rechercher un membre du jury"
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
            @click="addSoutenance" 
            :loading="submitting"
          />
        </template>
      </Dialog>
      
      <!-- Edit Soutenance Dialog -->
      <Dialog 
        v-model:visible="showEditModal" 
        header="Modifier la Soutenance" 
        :modal="true" 
        class="soutenance-dialog"
        :style="{ width: '600px' }"
        :closable="false"
      >
        <div v-if="editingSoutenance" class="field-container">
          <div class="p-field">
            <label for="edit-binome">Binôme</label>
            <Dropdown 
              id="edit-binome" 
              v-model="editingSoutenance.binome.id" 
              :options="binomes" 
              optionLabel="label" 
              optionValue="id"
              placeholder="Sélectionner un binôme" 
              class="w-full"
              required
              :filter="true"
              filterPlaceholder="Rechercher un binôme"
            />
          </div>
          
          <div class="p-field">
            <label for="edit-date">Date</label>
            <Calendar 
              id="edit-date" 
              v-model="editingSoutenance.date" 
              dateFormat="dd/mm/yy"
              class="w-full" 
              placeholder="Date de soutenance"
              required
              :minDate="today"
            />
          </div>
          
          <div class="p-field">
            <label for="edit-heure">Heure</label>
            <Calendar 
              id="edit-heure" 
              v-model="editingSoutenance.heure" 
              timeOnly 
              hourFormat="24" 
              class="w-full" 
              placeholder="Heure de soutenance"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="edit-salle">Salle</label>
            <Dropdown 
              id="edit-salle" 
              v-model="editingSoutenance.salle.id" 
              :options="salles" 
              optionLabel="nom" 
              optionValue="id"
              placeholder="Sélectionner une salle" 
              class="w-full"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="edit-jury1">Jury 1</label>
            <Dropdown 
              id="edit-jury1" 
              v-model="editingSoutenance.jury1.id" 
              :options="jurys" 
              optionLabel="fullName" 
              optionValue="id"
              placeholder="Sélectionner le premier membre du jury" 
              class="w-full"
              required
              :filter="true"
              filterPlaceholder="Rechercher un membre du jury"
            />
          </div>
          
          <div class="p-field">
            <label for="edit-jury2">Jury 2</label>
            <Dropdown 
              id="edit-jury2" 
              v-model="editingSoutenance.jury2.id" 
              :options="jurys" 
              optionLabel="fullName" 
              optionValue="id"
              placeholder="Sélectionner le second membre du jury" 
              class="w-full"
              required
              :filter="true"
              filterPlaceholder="Rechercher un membre du jury"
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
            @click="saveEditedSoutenance" 
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
  
  // Component state
  const soutenances = ref([]);
  const binomes = ref([]);
  const jurys = ref([]);
  const salles = ref([]);
  const classes = ref([]);
  const selectedClass = ref(null);
  const today = ref(new Date());
  
  const newSoutenance = ref({
    binome: { id: null },
    date: null,
    heure: null,
    salle: { id: null },
    jury1: { id: null },
    jury2: { id: null }
  });
  
  const editingSoutenance = ref(null);
  const showEditModal = ref(false);
  const showAddModal = ref(false);
  const loading = ref(false);
  const submitting = ref(false);
  const searchQuery = ref('');
  
  // Services
  const toast = useToast();
  const confirm = useConfirm();
  
  // Computed filtered soutenances
  const filteredSoutenances = computed(() => {
    const query = searchQuery.value.toLowerCase();
    let filtered = soutenances.value;
  
    // Filter by selected class
    if (selectedClass.value) {
      filtered = filtered.filter(soutenance => {
        const binomeMatch = binomes.value.find(b => b.id === soutenance.binome?.id);
        return binomeMatch && binomeMatch.classId === selectedClass.value;
      });
    }
  
    // Filter by search query
    if (query) {
      filtered = filtered.filter(soutenance => {
        const binomeLabel = getBinomeLabels(soutenance.binome).toLowerCase();
        const jury1Name = getJuryName(soutenance.jury1).toLowerCase();
        const jury2Name = getJuryName(soutenance.jury2).toLowerCase();
        const salleNom = soutenance.salle?.nom.toLowerCase() || '';
        const dateStr = formatDate(soutenance.date).toLowerCase();
        
        return (
          binomeLabel.includes(query) || 
          jury1Name.includes(query) ||
          jury2Name.includes(query) ||
          salleNom.includes(query) ||
          dateStr.includes(query) ||
          soutenance.heure.toLowerCase().includes(query)
        );
      });
    }
  
    return filtered;
  });
  
  // Fetch data on component mount
  onMounted(async () => {
    await Promise.all([
      fetchSoutenances(),
      fetchBinomes(),
      fetchJurys(),
      fetchSalles(),
      fetchClasses()
    ]);
    
    // Set default selected class
    if (classes.value.length > 0) {
      selectedClass.value = classes.value[0].id;
    }
  });
  
  // Methods for fetching data
  async function fetchSoutenances() {
    loading.value = true;
    try {
      const response = await ApiService.get('/api/soutenance');
      soutenances.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des soutenances');
    } finally {
      loading.value = false;
    }
  }
  
  async function fetchBinomes() {
    try {
      const response = await ApiService.get('/api/binome');
      
      // Process binomes to add labels and filiere info
      binomes.value = await Promise.all(response.map(async (binome) => {
        const etudiant1 = await getUtilisateur(binome.etudiant1?.id);
        const etudiant2 = await getUtilisateur(binome.etudiant2?.id);
        
        let label = etudiant1 ? `${etudiant1.prenom} ${etudiant1.nom}` : 'N/A';
        if (etudiant2) {
          label += ` & ${etudiant2.prenom} ${etudiant2.nom}`;
        }
        
        // Find class ID from etudiant entry
        let classId = null;
        if (etudiant1) {
          const etudiantInfo = await getEtudiant(etudiant1.id);
          if (etudiantInfo && etudiantInfo.filiere) {
            classId = etudiantInfo.filiere.id;
          }
        }
        
        return {
          ...binome,
          label,
          classId
        };
      }));
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des binômes');
    }
  }
  
  async function fetchJurys() {
    try {
      const response = await ApiService.get('/api/utilisateurs', { role: 'JURY' });
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
      const response = await ApiService.get('/api/salle');
      salles.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des salles');
    }
  }
  
  async function fetchClasses() {
    try {
      const response = await ApiService.get('/api/filiere');
      classes.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des filières');
    }
  }
  
  async function getUtilisateur(id) {
    if (!id) return null;
    try {
      return await ApiService.get(`/api/utilisateurs/${id}`);
    } catch (error) {
      console.error(`Erreur lors du chargement de l'utilisateur ${id}:`, error);
      return null;
    }
  }
  
  async function getEtudiant(id) {
    if (!id) return null;
    try {
      return await ApiService.get(`/api/etudiant/${id}`);
    } catch (error) {
      console.error(`Erreur lors du chargement de l'étudiant ${id}:`, error);
      return null;
    }
  }
  
  // Helper methods for data display
  function getBinomeLabels(binome) {
    if (!binome) return 'N/A';
    
    const binomeInfo = binomes.value.find(b => b.id === binome.id);
    return binomeInfo ? binomeInfo.label : 'N/A';
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
  
  // CRUD operations
  async function addSoutenance() {
    if (!validateSoutenanceData(newSoutenance.value)) return;
    
    submitting.value = true;
    try {
      // Prepare date and time in proper format
      const formattedDate = formatDateForApi(newSoutenance.value.date);
      const formattedTime = formatTimeForApi(newSoutenance.value.heure);
      
      // Create request body
      const soutenanceData = {
        date: formattedDate,
        heure: formattedTime,
        salle: { id: newSoutenance.value.salle.id },
        binome: { id: newSoutenance.value.binome.id },
        jury1: { id: newSoutenance.value.jury1.id },
        jury2: { id: newSoutenance.value.jury2.id }
      };
      
      const response = await ApiService.post('/api/soutenance', soutenanceData);
      soutenances.value.push(response);
      
      // Reset form and close modal
      resetNewSoutenance();
      showAddModal.value = false;
      
      // Show success message
      toast.add({
        severity: 'success',
        summary: 'Soutenance ajoutée',
        detail: 'La soutenance a été ajoutée avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, "Erreur lors de l'ajout de la soutenance");
    } finally {
      submitting.value = false;
    }
  }
  
  async function saveEditedSoutenance() {
    if (!validateSoutenanceData(editingSoutenance.value)) return;
    
    submitting.value = true;
    try {
      // Prepare date and time in proper format
      const formattedDate = formatDateForApi(editingSoutenance.value.date);
      const formattedTime = formatTimeForApi(editingSoutenance.value.heure);
      
      // Create request body
      const soutenanceData = {
        id: editingSoutenance.value.id,
        date: formattedDate,
        heure: formattedTime,
        salle: { id: editingSoutenance.value.salle.id },
        binome: { id: editingSoutenance.value.binome.id },
        jury1: { id: editingSoutenance.value.jury1.id },
        jury2: { id: editingSoutenance.value.jury2.id }
      };
      
      const response = await ApiService.put(`/api/soutenance/${editingSoutenance.value.id}`, soutenanceData);
      
      // Update local data
      const index = soutenances.value.findIndex(s => s.id === editingSoutenance.value.id);
      if (index !== -1) {
        soutenances.value[index] = response;
      }
      
      showEditModal.value = false;
      
      toast.add({
        severity: 'success',
        summary: 'Soutenance mise à jour',
        detail: 'La soutenance a été modifiée avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, 'Erreur lors de la modification de la soutenance');
    } finally {
      submitting.value = false;
    }
  }
  
  function confirmDelete(soutenance) {
    confirm.require({
      message: `Êtes-vous sûr de vouloir supprimer la soutenance du binôme ${getBinomeLabels(soutenance.binome)}?`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptClass: 'p-button-danger',
      accept: () => deleteSoutenance(soutenance),
      reject: () => {/* do nothing */}
    });
  }
  
  async function deleteSoutenance(soutenance) {
    try {
      await ApiService.delete(`/api/soutenance/${soutenance.id}`);
      
      // Update local data
      soutenances.value = soutenances.value.filter(s => s.id !== soutenance.id);
      
      toast.add({
        severity: 'success',
        summary: 'Soutenance supprimée',
        detail: 'La soutenance a été supprimée avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, 'Erreur lors de la suppression de la soutenance');
    }
  }
  
  // UI control methods
  function openAddModal() {
    resetNewSoutenance();
    showAddModal.value = true;
  }
  
  function openEditModal(soutenance) {
    // Create a deep copy to avoid modifying the original object
    editingSoutenance.value = JSON.parse(JSON.stringify(soutenance));
    
    // Convert date string to Date object if needed
    if (typeof editingSoutenance.value.date === 'string') {
      editingSoutenance.value.date = new Date(editingSoutenance.value.date);
    }
    
    // Convert time string to Date object if needed
    if (typeof editingSoutenance.value.heure === 'string') {
      const [hours, minutes] = editingSoutenance.value.heure.split(':');
      const timeDate = new Date();
      timeDate.setHours(parseInt(hours, 10), parseInt(minutes, 10), 0);
      editingSoutenance.value.heure = timeDate;
    }
    
    showEditModal.value = true;
  }
  
  function closeModals() {
    showEditModal.value = false;
    showAddModal.value = false;
  }
  
  function resetNewSoutenance() {
    newSoutenance.value = {
      binome: { id: null },
      date: null,
      heure: null,
      salle: { id: null },
      jury1: { id: null },
      jury2: { id: null }
    };
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
  
  // Validation
  function validateSoutenanceData(soutenance) {
    if (!soutenance.binome || !soutenance.binome.id) {
      showValidationError('Le binôme est obligatoire');
      return false;
    }
    
    if (!soutenance.date) {
      showValidationError('La date est obligatoire');
      return false;
    }
    
    if (!soutenance.heure) {
      showValidationError('L\'heure est obligatoire');
      return false;
    }
    
    if (!soutenance.salle || !soutenance.salle.id) {
      showValidationError('La salle est obligatoire');
      return false;
    }
    
    if (!soutenance.jury1 || !soutenance.jury1.id) {
      showValidationError('Le premier membre du jury est obligatoire');
      return false;
    }
    
    if (!soutenance.jury2 || !soutenance.jury2.id) {
      showValidationError('Le second membre du jury est obligatoire');
      return false;
    }
    
    if (soutenance.jury1.id === soutenance.jury2.id) {
      showValidationError('Les deux membres du jury doivent être différents');
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
  
<style>
/* Styles for SoutenanceManagementView.vue */

.soutenance-management {
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

.soutenance-dialog .p-calendar,
.soutenance-dialog .p-dropdown {
  width: 100%;
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
  
  .action-bar {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>