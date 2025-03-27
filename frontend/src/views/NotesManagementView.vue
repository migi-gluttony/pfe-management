<template>
    <div class="notes-management">
      <Toast />
      <!-- Add the UserInfoHeader component -->
    <UserInfoHeader 
      searchPlaceholder="Rechercher un binôme..." 
      :initialSearchValue="searchQuery"
      @search="handleHeaderSearch" 
    />
    <div class="header-section no-print">
  <div class="title-filter-group">
    <h1 class="page-title">Gestion des Notes</h1>
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
      label="Imprimer" 
      icon="pi pi-print" 
      class="p-button-primary action-btn"
      @click="printTable" 
    />
  </div>
</div>
  
      <!-- Regular DataTable (hidden when printing) -->
      <Card class="table-card no-print">
        <template #content>
          <DataTable 
            :value="filteredNotes" 
            :loading="loading" 
            responsiveLayout="scroll"
            stripedRows 
            :paginator="filteredNotes.length > 10" 
            class="data-table"
            :rows="10" 
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
            :rowsPerPageOptions="[10, 20, 50]"
            currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} étudiants"
            emptyMessage="Aucune note trouvée"
          >
            <Column field="etudiant.nom" header="Nom" sortable style="min-width: 10rem">
              <template #body="slotProps">
                {{ slotProps.data.etudiant.nom }}
              </template>
            </Column>
            <Column field="etudiant.prenom" header="Prénom" sortable style="min-width: 10rem">
              <template #body="slotProps">
                {{ slotProps.data.etudiant.prenom }}
              </template>
            </Column>
            <Column field="etudiant.cne" header="CNE" style="min-width: 10rem">
              <template #body="slotProps">
                {{ slotProps.data.etudiant.cne || '-' }}
              </template>
            </Column>
            <Column field="noteRapport" header="Note Rapport" sortable style="min-width: 8rem">
              <template #body="slotProps">
                <span :class="getGradeClass(slotProps.data.noteRapport)">
                  {{ formatGrade(slotProps.data.noteRapport) }}
                </span>
              </template>
            </Column>
            <Column field="noteSoutenance" header="Note Soutenance" sortable style="min-width: 8rem">
              <template #body="slotProps">
                <span :class="getGradeClass(slotProps.data.noteSoutenance)">
                  {{ formatGrade(slotProps.data.noteSoutenance) }}
                </span>
              </template>
            </Column>
            <Column field="noteEncadrant" header="Note Encadrant" sortable style="min-width: 8rem">
              <template #body="slotProps">
                <span :class="getGradeClass(slotProps.data.noteEncadrant)">
                  {{ formatGrade(slotProps.data.noteEncadrant) }}
                </span>
              </template>
            </Column>
            <Column field="noteFinale" header="Note Finale" sortable style="min-width: 8rem">
              <template #body="slotProps">
                <span v-if="canShowFinalGrade(slotProps.data)" :class="getGradeClass(slotProps.data.noteFinale)">
                  <strong>{{ formatGrade(calculateFinalGrade(slotProps.data)) }}</strong>
                </span>
                <span v-else class="incomplete-grade">Incomplet</span>
              </template>
            </Column>
            <Column field="mention" header="Mention"  style="min-width: 8rem">
              <template #body="slotProps">
                <Tag v-if="canShowFinalGrade(slotProps.data)" 
                     :severity="getMentionSeverity(getMention(calculateFinalGrade(slotProps.data)))" 
                     :value="getMention(calculateFinalGrade(slotProps.data))"></Tag>
                <span v-else>-</span>
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>
      
      <!-- Hidden table that will be shown only when printing -->
      <div class="print-only-table">
        <h1 class="print-header">Gestion des Notes</h1>
        
        <table class="print-table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Prénom</th>
              <th>CNE</th>
              <th>Note Rapport</th>
              <th>Note Soutenance</th>
              <th>Note Encadrant</th>
              <th>Note Finale</th>
              <th>Mention</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="note in filteredNotes" :key="note.id">
              <td>{{ note.etudiant.nom }}</td>
              <td>{{ note.etudiant.prenom }}</td>
              <td>{{ note.etudiant.cne || '-' }}</td>
              <td>{{ formatGrade(note.noteRapport) }}</td>
              <td>{{ formatGrade(note.noteSoutenance) }}</td>
              <td>{{ formatGrade(note.noteEncadrant) }}</td>
              <td>
                <strong v-if="canShowFinalGrade(note)">{{ formatGrade(calculateFinalGrade(note)) }}</strong>
                <span v-else>Incomplet</span>
              </td>
              <td>
                {{ canShowFinalGrade(note) ? getMention(calculateFinalGrade(note)) : '-' }}
              </td>
            </tr>
          </tbody>
        </table>
        
        <div class="print-footer">
          <p>Date d'impression: {{ new Date().toLocaleDateString() }}</p>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed, watch } from 'vue';
  import { useToast } from 'primevue/usetoast';
  import ApiService from '@/services/ApiService';
  import UserInfoHeader from '@/components/UserInfoHeader.vue';

  // Import PrimeVue components
  import Toast from 'primevue/toast';
  import Dropdown from 'primevue/dropdown';
  import InputText from 'primevue/inputtext';
  import Card from 'primevue/card';
  import DataTable from 'primevue/datatable';
  import Column from 'primevue/column';
  import Tag from 'primevue/tag';
  import Button from 'primevue/button';
  
  // Component state
  const notes = ref([]);
  const filieres = ref([]);
  const selectedFiliere = ref(null);
  const loading = ref(false);
  const searchQuery = ref('');
  
  // Services
  const toast = useToast();
  
  // State for grade percentages
  const pourcentages = ref({
    pourcentageRapport: 40,
    pourcentageSoutenance: 40,
    pourcentageEncadrant: 20
  });
  
  // Computed properties
  const filteredNotes = computed(() => {
    const query = searchQuery.value.toLowerCase();
    let filtered = [...notes.value];
    
    if (selectedFiliere.value) {
      filtered = filtered.filter(note => 
        note.filiereId === selectedFiliere.value
      );
    }
    
    if (query) {
      filtered = filtered.filter(note => 
        note.etudiant.nom.toLowerCase().includes(query) || 
        note.etudiant.prenom.toLowerCase().includes(query) ||
        (note.etudiant.cne && note.etudiant.cne.toLowerCase().includes(query))
      );
    }
    
    // Calculate final grade and mention for each note
    return filtered.map(note => ({
      ...note,
      noteFinale: calculateFinalGrade(note),
      mention: getMention(calculateFinalGrade(note))
    }));
  });
  
  // Print function
  function printTable() {
    window.print();
  }
  
  // Fetch data on component mount
  onMounted(async () => {
    await Promise.all([
      fetchNotes(),
      fetchFilieres()
    ]);
  });
  
  // Watch for filieres changes to set default value
  watch(filieres, (newFilieres) => {
    if (newFilieres && newFilieres.length > 0 && !selectedFiliere.value) {
      selectedFiliere.value = newFilieres[0].id;
      console.log('Automatically selected first filière:', newFilieres[0].nom);
    }
  }, { immediate: true });
  
  // Methods for fetching data
  async function fetchNotes() {
    loading.value = true;
    try {
      const response = await ApiService.get('/chef_de_departement/notes');
      
      // Extract notes from response
      if (response && response.notes) {
        notes.value = response.notes;
      } else {
        notes.value = [];
        console.warn('No notes found in response', response);
      }
      
      // Extract filières if present
      if (response && response.filieres && response.filieres.length > 0) {
        filieres.value = response.filieres;
      }
      
      // Extract percentages if present
      if (response && response.pourcentages) {
        pourcentages.value = response.pourcentages;
      }
      
    } catch (error) {
      console.error('Error fetching notes:', error);
      handleApiError(error, 'Erreur lors du chargement des notes');
      
      // Generate sample data if no data is available
      if (notes.value.length === 0) {
        generateSampleData();
      }
    } finally {
      loading.value = false;
    }
  }
  
  async function fetchFilieres() {
    // If we already have filières from the notes endpoint, don't fetch again
    if (filieres.value.length > 0) {
      return;
    }
    
    try {
      const response = await ApiService.get('/chef_de_departement/filieres');
      if (Array.isArray(response)) {
        filieres.value = response;
      } else {
        console.warn('Unexpected filières response format:', response);
      }
    } catch (error) {
      console.error('Error fetching filières:', error);
      handleApiError(error, 'Erreur lors du chargement des filières');
    }
  }
  
  function handleFiliereChange() {
    // We don't need to fetch data again as we'll filter client-side
    console.log('Filière selected:', selectedFiliere.value);
  }
  
  // Generate sample data for development and testing
  function generateSampleData() {
    console.log('Generating sample note data');
    
    const sampleFilieres = [
      { id: 1, nom: 'Génie Informatique' },
      { id: 2, nom: 'Génie Civil' },
      { id: 3, nom: 'Génie Électrique' }
    ];
    
    const sampleNotes = [];
    
    // Generate 5 sample students for each filière
    sampleFilieres.forEach(filiere => {
      for (let i = 1; i <= 5; i++) {
        const noteRapport = Math.floor(Math.random() * 11) + 10; // 10-20
        const noteSoutenance = Math.floor(Math.random() * 11) + 10; // 10-20
        const noteEncadrant = Math.floor(Math.random() * 11) + 10; // 10-20
        
        sampleNotes.push({
          id: sampleNotes.length + 1,
          etudiant: {
            id: sampleNotes.length + 1,
            nom: `Nom${sampleNotes.length + 1}`,
            prenom: `Prenom${sampleNotes.length + 1}`,
            cne: `CNE${100000 + sampleNotes.length + 1}`
          },
          noteRapport: noteRapport,
          noteSoutenance: noteSoutenance,
          noteEncadrant: noteEncadrant,
          filiereId: filiere.id,
          filiereName: filiere.nom
        });
      }
    });
    
    filieres.value = sampleFilieres;
    notes.value = sampleNotes;
    
    console.log('Sample data generated:', { filieres: filieres.value, notes: notes.value });
  }
  
  // Helper methods
  function formatGrade(grade) {
    return grade !== null && grade !== undefined ? grade.toFixed(2) : '-';
  }
  
  function getGradeClass(grade) {
    if (grade === null || grade === undefined) return '';
    if (grade >= 16) return 'grade-excellent';
    if (grade >= 14) return 'grade-very-good';
    if (grade >= 12) return 'grade-good';
    if (grade >= 10) return 'grade-satisfactory';
    return 'grade-poor';
  }
  
  function calculateFinalGrade(note) {
    // Use the pourcentages to calculate the weighted final grade
    const rapportWeight = pourcentages.value.pourcentageRapport / 100;
    const soutenanceWeight = pourcentages.value.pourcentageSoutenance / 100;
    const encadrantWeight = pourcentages.value.pourcentageEncadrant / 100;
    
    // Handle null values by defaulting to 0
    const rapportNote = note.noteRapport || 0;
    const soutenanceNote = note.noteSoutenance || 0;
    const encadrantNote = note.noteEncadrant || 0;
    
    return (
      rapportNote * rapportWeight +
      soutenanceNote * soutenanceWeight +
      encadrantNote * encadrantWeight
    );
  }
  
  function getMention(grade) {
    if (grade >= 16) return 'Excellent';
    if (grade >= 14) return 'Très Bien';
    if (grade >= 12) return 'Bien';
    if (grade >= 10) return 'Passable';
    return 'Insuffisant';
  }
  
  function getMentionSeverity(mention) {
    switch (mention) {
      case 'Excellent': return 'success';
      case 'Très Bien': return 'info';
      case 'Bien': return 'info';
      case 'Passable': return 'warning';
      case 'Insuffisant': return 'danger';
      default: return 'secondary';
    }
  }
  
  // Check if all required grades are present to show final grade
  function canShowFinalGrade(note) {
    return note.noteRapport != null && 
           note.noteSoutenance != null && 
           note.noteEncadrant != null;
  }
  
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
  
  <style>
  .notes-management {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
  }
  
  .header-section {
    margin-bottom: 2rem;
    display:flex;
    flex-direction: row;
    gap:1rem;
  }
  
  .title-row {
    margin-bottom: 1rem;
  }
  
  .page-title {
    color: var(--primary-color);
    font-size: 2rem;
    margin: 0;
  }
  
  .filter-row {
    display: flex;
    gap: 1rem;
    align-items: center;
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
    flex: 1;
    flex-direction: row;
    margin-bottom: 0.75rem;
    align-items: center;
    justify-content: center;
  }
  
  .search-container {
    width: 100%;
    display: block;
    display:flex;
    flex-direction: row;
    gap:1rem;
  }
  
  .search-input {
    width: 100%;
  }
  
  .button-row {
    margin-left: 1rem;
  }
  
  .print-btn {
    min-width: 120px;
  }
  
  .table-card {
    margin-bottom: 2rem;
  }
  
  /* Hidden table for printing */
  .print-only-table {
    display: none;
  }
  
  .print-header {
    text-align: center;
    margin-bottom: 20px;
  }
  
  .print-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
  }
  
  .print-table th, .print-table td {
    border: 1px solid black;
    padding: 8px;
    text-align: left;
  }
  
  .print-table th {
    font-weight: bold;
    background-color: #f2f2f2;
  }
  
  .print-footer {
    margin-top: 20px;
    text-align: right;
    font-size: 0.9em;
    color: #666;
  }
  
  /* Grade color classes */
  .incomplete-grade {
    font-style: italic;
    color: #888;
  }
  
  .grade-excellent {
    color: #4caf50;
  }
  
  .grade-very-good {
    color: #2196f3;
  }
  
  .grade-good {
    color: #00bcd4;
  }
  
  .grade-satisfactory {
    color: #ff9800;
  }
  
  .grade-poor {
    color: #f44336;
  }
  
  /* Print styles */
  @media print {
    body {
      margin: 0;
      padding: 0;
      font-family: serif;
      background-color: white;
    }
    
    .notes-management {
      max-width: 100%;
      padding: 0;
      margin: 0;
    }
    
    /* Hide the regular table and show the print table */
    .no-print {
      display: none !important;
    }
    
    .print-only-table {
      display: block;
      width: 100%;
      padding: 10px;
    }
    
    .print-header {
      color: black;
      font-size: 18pt;
      font-weight: bold;
      margin-bottom: 20px;
    }
    
    .print-table {
      width: 100%;
      border-collapse: collapse;
      border: 1px solid black;
    }
    
    .print-table th, .print-table td {
      border: 1px solid black;
      padding: 6px;
      text-align: left;
      color: black;
      font-size: 10pt;
    }
    
    .print-table th {
      font-weight: bold;
      background-color: white !important;
    }
    
    .print-footer {
      margin-top: 20px;
      text-align: right;
      color: black;
      font-size: 9pt;
    }
  }
  
  /* Responsive adjustments */
  @media screen and (max-width: 768px) {
    .notes-management {
      padding: 1rem;
    }
    
    .filter-row {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
    
    .filiere-dropdown {
      width: 100%;
    }
    
    .action-bar {
      flex-direction: column;
    }
    
    .button-row {
      margin-left: 0;
      margin-top: 0.5rem;
      width: 100%;
    }
    
    .print-btn {
      width: 100%;
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