<template>
    <div class="suggestions-management">
      <Toast />
      <ConfirmDialog />
      
      <div class="header-section">
        <div class="title-row">
          <h1 class="page-title">Suggestions de Sujets PFE</h1>
          <Button 
            label="Retour aux sujets" 
            icon="pi pi-arrow-left" 
            class="p-button-secondary back-btn"
            @click="goBackToSujets" 
          />
        </div>
      </div>
      
      <div class="action-bar">
        <div class="search-row">
          <span class="p-input-icon-left search-container">
            <i class="pi pi-search" />
            <InputText 
              v-model="searchQuery" 
              placeholder="Rechercher une suggestion..." 
              class="search-input"
            />
          </span>
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
            <Column field="binome.filiereName" header="Filière" sortable style="min-width: 8rem"></Column>
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
  
  // Import PrimeVue components
  import Toast from 'primevue/toast';
  import ConfirmDialog from 'primevue/confirmdialog';
  import InputText from 'primevue/inputtext';
  import Button from 'primevue/button';
  import Card from 'primevue/card';
  import DataTable from 'primevue/datatable';
  import Column from 'primevue/column';
  import Dialog from 'primevue/dialog';
  
  // Component state
  const suggestions = ref([]);
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
    if (!query) return suggestions.value;
    
    return suggestions.value.filter(suggestion => 
      suggestion.titre.toLowerCase().includes(query) || 
      suggestion.theme.toLowerCase().includes(query) ||
      suggestion.description.toLowerCase().includes(query) ||
      (suggestion.binome.etudiant1Name && suggestion.binome.etudiant1Name.toLowerCase().includes(query)) ||
      (suggestion.binome.etudiant2Name && suggestion.binome.etudiant2Name.toLowerCase().includes(query)) ||
      (suggestion.binome.filiereName && suggestion.binome.filiereName.toLowerCase().includes(query))
    );
  });
  
  // Fetch data on component mount
  onMounted(async () => {
    await fetchSuggestions();
  });
  
  // Methods for fetching data
  async function fetchSuggestions() {
    loading.value = true;
    try {
      const response = await ApiService.get('/chef_de_departement/sujet-suggestions');
      suggestions.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des suggestions');
    } finally {
      loading.value = false;
    }
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
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
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
  
  .back-btn {
    margin-left: 1rem;
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
    
    .back-btn {
      margin-left: 0;
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
  </style>