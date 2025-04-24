<template>
    <Dialog
      :visible="visible"
      @update:visible="$emit('update:visible', $event)"
      header="Options d'impression"
      :modal="true"
      :style="{ width: '500px' }"
      :closable="true"
    >
      <div class="print-options">
        <div class="option-group">
          <h3>Type de document</h3>
          <div class="p-field-radiobutton">
            <RadioButton id="summary-table" v-model="printOptions.documentType" value="summary" />
            <label for="summary-table">Tableau récapitulatif des notes</label>
          </div>
          <div class="p-field-radiobutton">
            <RadioButton id="evaluation-sheet" v-model="printOptions.documentType" value="evaluationSheet" />
            <label for="evaluation-sheet">Fiche d'évaluation individuelle</label>
          </div>
        </div>
        
        <div v-if="printOptions.documentType === 'summary'" class="option-group">
          <h3>Contenu</h3>
          <div class="p-field-checkbox">
            <Checkbox id="show-details" v-model="printOptions.showDetails" :binary="true" />
            <label for="show-details">Afficher les détails des notes</label>
          </div>
        </div>
        
        <div v-if="printOptions.documentType === 'summary'" class="option-group">
          <h3>Filières</h3>
          <div class="p-field-radiobutton">
            <RadioButton id="current-filiere" v-model="printOptions.printOption" value="current" />
            <label for="current-filiere">Imprimer la filière actuelle</label>
          </div>
          <div class="p-field-radiobutton">
            <RadioButton id="all-filieres" v-model="printOptions.printOption" value="all" />
            <label for="all-filieres">Imprimer toutes les filières</label>
          </div>
        </div>
        
        <div v-if="printOptions.documentType === 'evaluationSheet'" class="option-group">
          <h3>Étudiant</h3>
          <div class="student-selector">
            <Dropdown
              v-model="printOptions.selectedStudent"
              :options="studentOptions"
              optionLabel="name"
              optionValue="id"
              placeholder="Sélectionner un étudiant"
              class="w-full"
            />
          </div>
        </div>
        
        <div v-if="documentTypeValid" class="print-preview-notice">
          <i class="pi pi-info-circle"></i>
          <p>Après l'impression, vous serez redirigé vers la fenêtre d'impression de votre navigateur où vous pourrez prévisualiser et configurer d'autres options.</p>
        </div>
        
        <div v-else class="print-error-notice">
          <i class="pi pi-exclamation-triangle"></i>
          <p>Veuillez compléter toutes les informations requises pour continuer.</p>
        </div>
      </div>
      
      <template #footer>
        <Button
          label="Annuler"
          icon="pi pi-times"
          class="p-button-text"
          @click="$emit('update:visible', false)"
        />
        <Button
          label="Imprimer"
          icon="pi pi-print"
          class="p-button-primary"
          @click="handlePrint"
          :disabled="!documentTypeValid"
        />
      </template>
    </Dialog>
  </template>
  
  <script setup>
  import { ref, reactive, watch, computed } from 'vue';
  import Dialog from 'primevue/dialog';
  import Button from 'primevue/button';
  import Checkbox from 'primevue/checkbox';
  import RadioButton from 'primevue/radiobutton';
  import Dropdown from 'primevue/dropdown';
  
  const props = defineProps({
    visible: {
      type: Boolean,
      required: true
    },
    notes: {
      type: Array,
      default: () => []
    },
    selectedFiliere: {
      type: [Number, String, null],
      default: null
    },
    filieres: {
      type: Array,
      default: () => []
    }
  });
  
  const emit = defineEmits(['update:visible', 'print', 'print-evaluation-sheet']);
  
  // Print options state
  const printOptions = reactive({
    documentType: 'summary', // 'summary' or 'evaluationSheet'
    showDetails: true,
    printOption: 'current', // 'current' or 'all'
    selectedStudent: null
  });
  
  // Compute student options for dropdown
  const studentOptions = computed(() => {
    if (!props.notes || props.notes.length === 0) {
      return [];
    }
    
    // Filter notes by selected filiere if applicable
    let filteredNotes = props.notes;
    if (printOptions.printOption === 'current' && props.selectedFiliere) {
      filteredNotes = props.notes.filter(note => note.filiereId === props.selectedFiliere);
    }
    
    // Map to options format
    return filteredNotes.map(note => ({
      id: note.id,
      name: `${note.etudiant.nom} ${note.etudiant.prenom}`,
      etudiantId: note.etudiant.id
    }));
  });
  
  // Check if current document type selection is valid
  const documentTypeValid = computed(() => {
    if (printOptions.documentType === 'summary') {
      return true; // Summary is always valid
    } else if (printOptions.documentType === 'evaluationSheet') {
      return !!printOptions.selectedStudent; // Evaluation sheet needs a selected student
    }
    return false;
  });
  
  // Get filiere name by ID
  function getFiliereName(filiereId) {
    const filiere = props.filieres.find(f => f.id === filiereId);
    return filiere ? filiere.nom : '';
  }
  
  // Handle print button click
  function handlePrint() {
    if (printOptions.documentType === 'summary') {
      emit('print', {
        showDetails: printOptions.showDetails,
        showAllFilieres: printOptions.printOption === 'all'
      });
    } else if (printOptions.documentType === 'evaluationSheet' && printOptions.selectedStudent) {
      // Find the student note
      const selectedNote = props.notes.find(note => note.id === printOptions.selectedStudent);
      if (selectedNote) {
        emit('print-evaluation-sheet', {
          student: selectedNote.etudiant,
          note: selectedNote,
          filiereName: getFiliereName(selectedNote.filiereId)
        });
      }
    }
    
    // Close dialog
    emit('update:visible', false);
  }
  
  // Reset options when dialog opens
  watch(() => props.visible, (newValue) => {
    if (newValue) {
      printOptions.documentType = 'summary';
      printOptions.showDetails = true;
      printOptions.printOption = 'current';
      printOptions.selectedStudent = null;
    }
  });
  </script>
  
  <style scoped>
  .print-options {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .option-group {
    border: 1px solid var(--surface-border);
    border-radius: 8px;
    padding: 1rem;
  }
  
  .option-group h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    font-size: 1rem;
    color: var(--primary-color);
  }
  
  .p-field-checkbox, .p-field-radiobutton {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
  }
  
  .p-field-checkbox:last-child, .p-field-radiobutton:last-child {
    margin-bottom: 0;
  }
  
  .print-preview-notice {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    background-color: var(--surface-section);
    padding: 1rem;
    border-radius: 8px;
    font-size: 0.9rem;
  }
  
  .print-preview-notice i {
    color: var(--primary-color);
    margin-top: 2px;
  }
  
  .print-preview-notice p {
    margin: 0;
    line-height: 1.4;
  }
  
  .print-error-notice {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    background-color: var(--red-50);
    padding: 1rem;
    border-radius: 8px;
    font-size: 0.9rem;
  }
  
  .dark-mode .print-error-notice {
    background-color: rgba(255, 0, 0, 0.1);
  }
  
  .print-error-notice i {
    color: var(--red-500);
    margin-top: 2px;
  }
  
  .print-error-notice p {
    margin: 0;
    line-height: 1.4;
    color: var(--red-700);
  }
  
  .student-selector {
    width: 100%;
  }
  
  :deep(.student-selector .p-dropdown) {
    width: 100%;
  }
  </style>