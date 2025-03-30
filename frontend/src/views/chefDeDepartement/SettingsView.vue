<template>
    <div class="settings-container">
      <Toast />
      <ConfirmDialog />
      
      <!-- User Info Header -->
      <UserInfoHeader title="Paramètres de l'application" />
      
      <div class="settings-layout">
        <!-- Percentages Card -->
        <Card class="settings-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-percentage mr-2"></i>
              <span>Pourcentages des notes</span>
            </div>
          </template>
          <template #content>
            <div v-if="loading" class="p-d-flex p-jc-center">
              <ProgressSpinner style="width: 50px; height: 50px;" />
            </div>
            <div v-else class="percentage-content">
              <div class="percentage-description mb-4">
                <p>
                  Configurez les pourcentages de calcul pour les notes finales.
                  <br />La somme des pourcentages doit être égale à 100%.
                </p>
              </div>
              
              <div class="percentage-form">
                <div class="p-field mb-3">
                  <label for="rapport">Pourcentage Rapport (%)</label>
                  <InputNumber 
                    id="rapport" 
                    v-model="pourcentages.pourcentageRapport" 
                    :min="0" 
                    :max="100" 
                    showButtons 
                    buttonLayout="horizontal"
                    decrementButtonClass="p-button-danger" 
                    incrementButtonClass="p-button-success"
                    incrementButtonIcon="pi pi-plus" 
                    decrementButtonIcon="pi pi-minus"
                    class="w-full"
                  />
                </div>
                
                <div class="p-field mb-3">
                  <label for="soutenance">Pourcentage Soutenance (%)</label>
                  <InputNumber 
                    id="soutenance" 
                    v-model="pourcentages.pourcentageSoutenance" 
                    :min="0" 
                    :max="100" 
                    showButtons 
                    buttonLayout="horizontal"
                    decrementButtonClass="p-button-danger" 
                    incrementButtonClass="p-button-success"
                    incrementButtonIcon="pi pi-plus" 
                    decrementButtonIcon="pi pi-minus"
                    class="w-full"
                  />
                </div>
                
                <div class="p-field mb-3">
                  <label for="encadrant">Pourcentage Encadrant (%)</label>
                  <InputNumber 
                    id="encadrant" 
                    v-model="pourcentages.pourcentageEncadrant" 
                    :min="0" 
                    :max="100" 
                    showButtons 
                    buttonLayout="horizontal"
                    decrementButtonClass="p-button-danger" 
                    incrementButtonClass="p-button-success"
                    incrementButtonIcon="pi pi-plus" 
                    decrementButtonIcon="pi pi-minus"
                    class="w-full"
                  />
                </div>
                
                <div class="percentages-total mt-4">
                  <Tag severity="info" class="p-mr-2">
                    <b>Total: {{ calculateTotal() }}%</b>
                    <i 
                      class="pi ml-2" 
                      :class="calculateTotal() === 100 ? 'pi-check text-green-500' : 'pi-times text-red-500'"
                    ></i>
                  </Tag>
                </div>
                
                <Button 
                  label="Sauvegarder les pourcentages" 
                  icon="pi pi-save" 
                  class="p-button-primary mt-4 w-full"
                  :disabled="calculateTotal() !== 100 || savingPercentages"
                  :loading="savingPercentages"
                  @click="savePercentages"
                />
              </div>
            </div>
          </template>
        </Card>
        
        <!-- Academic Year Card -->
        <Card class="settings-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-calendar mr-2"></i>
              <span>Gestion des années scolaires</span>
            </div>
          </template>
          <template #content>
            <div v-if="loading" class="p-d-flex p-jc-center">
              <ProgressSpinner style="width: 50px; height: 50px;" />
            </div>
            <div v-else class="year-content">
              <div class="current-year-info mb-4">
                <h3>Année scolaire courante</h3>
                <div class="year-detail">
                  <Tag severity="success" class="current-year-tag">
                    {{ currentYear.annee }}
                  </Tag>
                </div>
              </div>
              
              <Divider />
              
              <div class="archive-section mt-4 mb-4">
                <h3>Archiver l'année courante</h3>
                <p class="mb-3">
                  Archiver l'année courante va rendre toutes les données en lecture seule.
                  Une nouvelle année scolaire ({{ getNextAcademicYear(currentYear.annee) }}) sera automatiquement créée.
                </p>
                <Button 
                  label="Archiver l'année courante" 
                  icon="pi pi-box" 
                  class="p-button-warning"
                  @click="confirmArchive"
                  :loading="archiving"
                />
              </div>
              
              <Divider />
              
              <div class="years-history mt-4">
                <h3>Historique des années scolaires</h3>
                <DataTable 
                  :value="academicYears" 
                  class="mt-3"
                  :paginator="academicYears.length > 5"
                  :rows="5"
                  stripedRows
                  responsiveLayout="scroll"
                >
                  <Column field="annee" header="Année scolaire"></Column>
                  <Column header="Statut">
                    <template #body="slotProps">
                      <Tag :severity="slotProps.data.courante ? 'success' : 'info'">
                        {{ slotProps.data.courante ? 'Courante' : 'Archivée' }}
                      </Tag>
                    </template>
                  </Column>
                </DataTable>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useToast } from 'primevue/usetoast';
  import { useConfirm } from 'primevue/useconfirm';
  import ApiService from '@/services/ApiService';
  
  // Import components
  import UserInfoHeader from '@/components/UserInfoHeader.vue';
  import ProgressSpinner from 'primevue/progressspinner';
  import Card from 'primevue/card';
  import InputNumber from 'primevue/inputnumber';
  import Button from 'primevue/button';
  import Divider from 'primevue/divider';
  import Tag from 'primevue/tag';
  import DataTable from 'primevue/datatable';
  import Column from 'primevue/column';
  import Toast from 'primevue/toast';
  import ConfirmDialog from 'primevue/confirmdialog';
  
  // Services
  const toast = useToast();
  const confirm = useConfirm();
  
  // Component state
  const loading = ref(true);
  const savingPercentages = ref(false);
  const archiving = ref(false);
  
  // Data
  const pourcentages = ref({
    pourcentageRapport: 40,
    pourcentageSoutenance: 40,
    pourcentageEncadrant: 20
  });
  
  const currentYear = ref({
    id: null,
    annee: '',
    courante: true
  });
  
  const academicYears = ref([]);
  
  // Calculate total percentage
  const calculateTotal = () => {
    return (pourcentages.value.pourcentageRapport || 0) +
           (pourcentages.value.pourcentageSoutenance || 0) +
           (pourcentages.value.pourcentageEncadrant || 0);
  };
  
  // Load data on component mount
  onMounted(async () => {
    await fetchSettings();
    await fetchAcademicYears();
  });
  
  // Methods
  async function fetchSettings() {
    loading.value = true;
    try {
      const response = await ApiService.get('/chef_de_departement/settings');
      
      // Set percentages
      if (response.pourcentages) {
        pourcentages.value = {
          pourcentageRapport: response.pourcentages.pourcentageRapport,
          pourcentageSoutenance: response.pourcentages.pourcentageSoutenance,
          pourcentageEncadrant: response.pourcentages.pourcentageEncadrant
        };
      }
      
      // Set current year
      if (response.currentYear) {
        currentYear.value = response.currentYear;
      }
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des paramètres');
    } finally {
      loading.value = false;
    }
  }
  
  async function fetchAcademicYears() {
    try {
      const response = await ApiService.get('/chef_de_departement/settings/academic-years');
      if (response && response.years) {
        academicYears.value = response.years;
      }
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des années scolaires');
    }
  }
  
  // Calculate next academic year based on current year format (YYYY-YYYY)
  function getNextAcademicYear(currentYearStr) {
    if (!currentYearStr || !currentYearStr.includes('-')) return '';
    
    const yearParts = currentYearStr.split('-');
    if (yearParts.length !== 2) return '';
    
    try {
      const startYear = parseInt(yearParts[0]);
      const endYear = parseInt(yearParts[1]);
      return `${startYear + 1}-${endYear + 1}`;
    } catch (e) {
      return '';
    }
  }
  
  async function savePercentages() {
    // Validate total is 100%
    if (calculateTotal() !== 100) {
      toast.add({
        severity: 'warn',
        summary: 'Validation',
        detail: 'La somme des pourcentages doit être égale à 100%',
        life: 5000
      });
      return;
    }
    
    savingPercentages.value = true;
    try {
      const response = await ApiService.put('/chef_de_departement/settings/percentages', pourcentages.value);
      
      toast.add({
        severity: 'success',
        summary: 'Pourcentages mis à jour',
        detail: 'Les pourcentages ont été sauvegardés avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, 'Erreur lors de la sauvegarde des pourcentages');
    } finally {
      savingPercentages.value = false;
    }
  }
  
  function confirmArchive() {
    const nextYear = getNextAcademicYear(currentYear.value.annee);
    
    confirm.require({
      message: `Êtes-vous sûr de vouloir archiver l'année scolaire courante ${currentYear.value.annee}? Une nouvelle année ${nextYear} sera automatiquement créée. Cette action est irréversible.`,
      header: 'Confirmation d\'archivage',
      icon: 'pi pi-exclamation-triangle',
      acceptClass: 'p-button-danger',
      accept: () => archiveCurrentYear(),
      reject: () => {}
    });
  }
  
  async function archiveCurrentYear() {
    archiving.value = true;
    try {
      const response = await ApiService.post('/chef_de_departement/settings/academic-years/archive');
      
      toast.add({
        severity: 'success',
        summary: 'Années mises à jour',
        detail: `L'année scolaire ${currentYear.value.annee} a été archivée et l'année ${response.annee} a été créée automatiquement`,
        life: 5000
      });
      
      // Refresh data
      await fetchSettings();
      await fetchAcademicYears();
    } catch (error) {
      handleApiError(error, 'Erreur lors de l\'archivage de l\'année scolaire');
    } finally {
      archiving.value = false;
    }
  }
  
  // Error handling
  function handleApiError(error, defaultMessage) {
    console.error(defaultMessage, error);
    
    let errorMessage = defaultMessage;
    if (error.message) {
      errorMessage = error.message;
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
  .settings-container {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
  }
  
  .settings-layout {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
    gap: 2rem;
    margin-top: 2rem;
  }
  
  .settings-card {
    margin-bottom: 2rem;
  }
  
  .card-title {
    display: flex;
    align-items: center;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--primary-color);
  }
  
  .current-year-tag {
    font-size: 1.25rem;
    padding: 0.5rem 1rem;
  }
  
  h3 {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 0.75rem;
    color: var(--text-color);
  }
  
  /* Responsive tweaks */
  @media (max-width: 768px) {
    .settings-layout {
      grid-template-columns: 1fr;
    }
  }
  </style>