<template>
  <Dialog 
    :visible="visible" 
    @update:visible="$emit('update:visible', $event)"
    :header="`Import des ${getRoleLabel(role)}s`" 
    :modal="true" 
    class="import-dialog"
    :style="{ width: '700px' }"
    :closable="false"
  >
    <div class="import-container">
      <div class="import-tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'file' }"
          @click="activeTab = 'file'"
        >
          <i class="pi pi-file-import"></i>
          Importer un fichier
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'paste' }"
          @click="activeTab = 'paste'"
        >
          <i class="pi pi-code"></i>
          Coller des données
        </div>
      </div>
      
      <div class="tab-content">
        <!-- File Upload Tab -->
        <div v-if="activeTab === 'file'" class="file-upload-tab">
          <div class="format-selector">
            <div class="p-field-radiobutton">
              <RadioButton v-model="fileFormat" value="json" inputId="format-json" />
              <label for="format-json">JSON</label>
            </div>
            <div class="p-field-radiobutton">
              <RadioButton v-model="fileFormat" value="csv" inputId="format-csv" />
              <label for="format-csv">CSV</label>
            </div>
          </div>
          
          <FileUpload
            mode="basic"
            :accept="fileFormat === 'json' ? '.json' : '.csv'"
            chooseLabel="Sélectionner un fichier"
            class="upload-button"
            :auto="true"
            @select="onFileSelect"
            :disabled="processing"
          />
          
          <div v-if="selectedFile" class="selected-file">
            <i :class="fileFormat === 'json' ? 'pi pi-file-pdf' : 'pi pi-file-excel'"></i>
            <span>{{ selectedFile.name }}</span>
            <Button 
              icon="pi pi-times" 
              class="p-button-rounded p-button-text p-button-sm" 
              @click="selectedFile = null"
              :disabled="processing"
            />
          </div>
        </div>
        
        <!-- Paste Data Tab -->
        <div v-if="activeTab === 'paste'" class="paste-tab">
          <div class="format-selector">
            <div class="p-field-radiobutton">
              <RadioButton v-model="pasteFormat" value="json" inputId="paste-format-json" />
              <label for="paste-format-json">JSON</label>
            </div>
            <div class="p-field-radiobutton">
              <RadioButton v-model="pasteFormat" value="csv" inputId="paste-format-csv" />
              <label for="paste-format-csv">CSV</label>
            </div>
          </div>
          
          <Textarea 
            v-model="pastedData" 
            rows="10" 
            class="paste-area"
            :placeholder="pasteFormat === 'json' ? 'Collez votre JSON ici...' : 'Collez votre CSV ici...'"
            :disabled="processing"
          />
        </div>
      </div>
      
      <div v-if="previewData.length > 0" class="preview-section">
        <div class="preview-header">
          <h3>Aperçu ({{ previewData.length }} utilisateurs)</h3>
          <div class="preview-controls">
            <Button 
              icon="pi pi-refresh" 
              class="p-button-text p-button-sm" 
              @click="refreshPreview"
              tooltip="Actualiser l'aperçu"
              :disabled="processing"
            />
          </div>
        </div>
        
        <DataTable 
          :value="previewData.slice(0, 5)" 
          responsiveLayout="scroll"
          stripedRows
          class="preview-table"
        >
          <Column field="nom" header="Nom"></Column>
          <Column field="prenom" header="Prénom"></Column>
          <Column v-if="role === 'ETUDIANT'" field="cne" header="CNE"></Column>
          <Column v-if="role !== 'ETUDIANT'" field="cni" header="CNI"></Column>
          <Column field="dateNaissance" header="Date de naissance">
            <template #body="slotProps">
              {{ formatDate(slotProps.data.dateNaissance) }}
            </template>
          </Column>
          <Column v-if="role === 'ETUDIANT'" field="filiereId" header="Filière ID"></Column>
        </DataTable>
        
        <div v-if="previewData.length > 5" class="preview-more">
          ... et {{ previewData.length - 5 }} autres utilisateurs
        </div>
      </div>
      
      <div v-if="errors.length > 0" class="errors-section">
        <h3>Erreurs de validation</h3>
        <ul class="error-list">
          <li v-for="(error, index) in errors" :key="index" class="error-item">
            <i class="pi pi-exclamation-triangle"></i>
            <span>{{ error }}</span>
          </li>
        </ul>
      </div>
      
      <div class="import-info">
        <h3>Format attendu pour les {{ getRoleLabel(role) }}s:</h3>
        <div class="format-example">
          <div v-if="role === 'ETUDIANT'">
            <p><strong>JSON:</strong></p>
            <pre>[
  {
    "nom": "Nom",
    "prenom": "Prénom",
    "cne": "CNE12345",
    "dateNaissance": "1998-01-15",
    "filiereId": 1
  },
  ...
]</pre>
            <p><strong>CSV:</strong></p>
            <pre>nom,prenom,cne,dateNaissance,filiereId
Nom,Prénom,CNE12345,1998-01-15,1
...</pre>

            <!-- Filières reference table -->
            <div v-if="filieres.length > 0" class="filieres-reference">
              <h4>Filières disponibles:</h4>
              <div class="filieres-table">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Nom de la filière</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="filiere in filieres" :key="filiere.id">
                      <td>{{ filiere.id }}</td>
                      <td>{{ filiere.nom }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <p class="filieres-note">Utilisez l'ID correspondant à la filière dans votre fichier d'import.</p>
            </div>
          </div>
          <div v-else>
            <p><strong>JSON:</strong></p>
            <pre>[
  {
    "nom": "Nom",
    "prenom": "Prénom",
    "cni": "ABC123456",
    "dateNaissance": "1980-05-20"
  },
  ...
]</pre>
            <p><strong>CSV:</strong></p>
            <pre>nom,prenom,cni,dateNaissance
Nom,Prénom,ABC123456,1980-05-20
...</pre>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <Button 
        label="Annuler" 
        icon="pi pi-times" 
        class="p-button-text" 
        @click="cancel" 
        :disabled="processing"
      />
      <Button 
        :label="processing ? 'Importation...' : 'Importer'" 
        icon="pi pi-check" 
        class="p-button-primary" 
        @click="importUsers" 
        :loading="processing"
        :disabled="!canImport"
      />
    </template>
  </Dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import FileUpload from 'primevue/fileupload';
import RadioButton from 'primevue/radiobutton';
import Textarea from 'primevue/textarea';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  role: {
    type: String,
    required: true
  },
  filieres: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:visible', 'import-users']);

// Component state
const activeTab = ref('file');
const fileFormat = ref('json');
const pasteFormat = ref('json');
const selectedFile = ref(null);
const pastedData = ref('');
const previewData = ref([]);
const errors = ref([]);
const processing = ref(false);

// Reset component state when visibility changes
watch(() => props.visible, (newValue) => {
  if (newValue) {
    // Modal opened
    activeTab.value = 'file';
    fileFormat.value = 'json';
    pasteFormat.value = 'json';
    selectedFile.value = null;
    pastedData.value = '';
    previewData.value = [];
    errors.value = [];
  }
});
watch(() => pastedData.value, (newValue) => {
  if (activeTab.value === 'paste' && newValue && newValue.trim() !== '') {
    parseData(newValue, pasteFormat.value);
  } else if (activeTab.value === 'paste') {
    previewData.value = [];
  }
});

// Computed property to determine if import button should be enabled
const canImport = computed(() => {
  return (
    (activeTab.value === 'file' && selectedFile.value !== null) ||
    (activeTab.value === 'paste' && pastedData.value.trim() !== '')
  ) && previewData.value.length > 0 && errors.value.length === 0;
});

// Event handlers
function cancel() {
  emit('update:visible', false);
}

function onFileSelect(event) {
  selectedFile.value = event.files[0];
  readFile();
}

function readFile() {
  if (!selectedFile.value) return;
  
  const reader = new FileReader();
  
  reader.onload = (e) => {
    try {
      const fileContent = e.target.result;
      parseData(fileContent, fileFormat.value);
    } catch (error) {
      errors.value = [`Erreur de lecture du fichier: ${error.message}`];
    }
  };
  
  reader.onerror = () => {
    errors.value = ['Erreur de lecture du fichier'];
  };
  
  reader.readAsText(selectedFile.value);
}

function refreshPreview() {
  if (activeTab.value === 'file' && selectedFile.value) {
    readFile();
  } else if (activeTab.value === 'paste' && pastedData.value) {
    parseData(pastedData.value, pasteFormat.value);
  }
}

function parseData(data, format) {
  errors.value = [];
  previewData.value = [];
  
  try {
    let parsedData = [];
    
    if (format === 'json') {
      parsedData = parseJson(data);
    } else {
      parsedData = parseCsv(data);
    }
    
    // Validate and transform data
    const { validatedData, validationErrors } = validateData(parsedData);
    
    previewData.value = validatedData;
    errors.value = validationErrors;
    
  } catch (error) {
    errors.value = [`Erreur de parsing: ${error.message}`];
  }
}

function parseJson(jsonData) {
  const data = JSON.parse(jsonData);
  
  if (!Array.isArray(data)) {
    throw new Error('Le JSON doit être un tableau d\'objets');
  }
  
  return data;
}

function parseCsv(csvData) {
  const lines = csvData.split('\n').filter(line => line.trim() !== '');
  if (lines.length < 2) {
    throw new Error('CSV invalide: il doit avoir une ligne d\'en-tête et au moins une ligne de données');
  }
  
  const headers = lines[0].split(',').map(h => h.trim());
  const result = [];
  
  for (let i = 1; i < lines.length; i++) {
    const values = lines[i].split(',').map(v => v.trim());
    
    if (values.length === headers.length) {
      const obj = {};
      headers.forEach((header, index) => {
        obj[header] = values[index];
      });
      result.push(obj);
    }
  }
  
  return result;
}

function validateData(data) {
  const validatedData = [];
  const validationErrors = [];
  
  data.forEach((item, index) => {
    const errors = [];
    const validatedItem = {
      nom: '',
      prenom: '',
      dateNaissance: null,
      role: props.role
    };
    
    // Required fields for all roles
    if (!item.nom) errors.push(`Ligne ${index + 1}: Nom manquant`);
    else validatedItem.nom = item.nom;
    
    if (!item.prenom) errors.push(`Ligne ${index + 1}: Prénom manquant`);
    else validatedItem.prenom = item.prenom;
    
    // Validate date format
    if (!item.dateNaissance) {
      errors.push(`Ligne ${index + 1}: Date de naissance manquante`);
    } else {
      // Check if date is valid
      const date = new Date(item.dateNaissance);
      if (isNaN(date.getTime())) {
        errors.push(`Ligne ${index + 1}: Format de date invalide. Utilisez YYYY-MM-DD`);
      } else {
        validatedItem.dateNaissance = item.dateNaissance;
      }
    }
    
    // Role-specific validations
    if (props.role === 'ETUDIANT') {
      if (!item.cne) errors.push(`Ligne ${index + 1}: CNE manquant`);
      else validatedItem.cne = item.cne;
      
      if (!item.filiereId) errors.push(`Ligne ${index + 1}: ID de filière manquant`);
      else {
        const filiereId = parseInt(item.filiereId);
        if (isNaN(filiereId)) {
          errors.push(`Ligne ${index + 1}: ID de filière doit être un nombre`);
        } else {
          // Check if filiere exists
          const filiereExists = props.filieres.some(f => f.id === filiereId);
          if (!filiereExists) {
            errors.push(`Ligne ${index + 1}: Filière avec ID ${filiereId} n'existe pas`);
          } else {
            validatedItem.filiereId = filiereId;
          }
        }
      }
    } else {
      if (!item.cni) errors.push(`Ligne ${index + 1}: CNI manquant`);
      else validatedItem.cni = item.cni;
    }
    
    if (errors.length === 0) {
      validatedData.push(validatedItem);
    } else {
      validationErrors.push(...errors);
    }
  });
  
  return { validatedData, validationErrors };
}

async function importUsers() {
  if (!canImport.value) return;
  
  processing.value = true;
  
  try {
    // Emit event with user data to parent component
    emit('import-users', previewData.value);
    
    // Close modal on success
    setTimeout(() => {
      processing.value = false;
      emit('update:visible', false);
    }, 500);
  } catch (error) {
    errors.value = [`Erreur d'importation: ${error.message}`];
    processing.value = false;
  }
}

// Utility functions
function getRoleLabel(role) {
  switch (role) {
    case 'ETUDIANT': return 'étudiant';
    case 'ENCADRANT': return 'encadrant';
    case 'JURY': return 'jury';
    default: return role.toLowerCase();
  }
}

function formatDate(dateString) {
  if (!dateString) return '-';
  
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
  } catch (e) {
    return dateString;
  }
}
</script>

<style scoped>
.import-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.import-tabs {
  display: flex;
  border-bottom: 1px solid var(--surface-border);
}

.tab-item {
  padding: 0.75rem 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover {
  background-color: var(--surface-hover);
}

.tab-item.active {
  border-bottom-color: var(--primary-color);
  color: var(--primary-color);
  font-weight: 500;
}

.tab-content {
  padding: 1rem 0;
}

.format-selector {
  display: flex;
  gap: 2rem;
  margin-bottom: 1rem;
}

.p-field-radiobutton {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.file-upload-tab, .paste-tab {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.paste-area {
  width: 100%;
  font-family: monospace;
  resize: vertical;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background-color: var(--surface-ground);
  border-radius: 4px;
}

.preview-section {
  background-color: var(--surface-section);
  border-radius: 6px;
  padding: 1rem;
  margin-top: 1rem;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.preview-header h3 {
  margin: 0;
  font-size: 1rem;
}

.preview-more {
  text-align: center;
  padding: 0.5rem;
  color: var(--text-color-secondary);
  font-style: italic;
}

.errors-section {
  background-color: var(--red-50);
  border-radius: 6px;
  padding: 1rem;
}

.dark-mode .errors-section {
  background-color: rgba(255, 0, 0, 0.1);
}

.errors-section h3 {
  margin: 0 0 0.5rem 0;
  color: var(--red-700);
  font-size: 1rem;
}

.error-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.error-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0;
  color: var(--red-600);
}

.error-item i {
  color: var(--red-500);
}

.import-info {
  background-color: var(--surface-ground);
  border-radius: 6px;
  padding: 1rem;
}

.import-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
}

.format-example {
  font-size: 0.9rem;
}

.format-example pre {
  background-color: var(--surface-card);
  padding: 0.75rem;
  border-radius: 4px;
  margin: 0.5rem 0;
  overflow-x: auto;
}

.dark-mode .format-example pre {
  background-color: rgba(0, 0, 0, 0.2);
}

/* Filières reference section */
.filieres-reference {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px dashed var(--surface-border);
}

.filieres-reference h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.95rem;
  color: var(--primary-color);
}

.filieres-table {
  max-height: 150px;
  overflow-y: auto;
  margin: 0.5rem 0;
  border-radius: 4px;
  background-color: var(--surface-card);
}

.filieres-table table {
  width: 100%;
  border-collapse: collapse;
}

.filieres-table th,
.filieres-table td {
  padding: 0.5rem;
  font-size: 0.85rem;
  text-align: left;
}

.filieres-table th {
  position: sticky;
  top: 0;
  background-color: var(--surface-card);
  color: var(--primary-color);
  font-weight: 600;
  border-bottom: 1px solid var(--surface-border);
}

.filieres-table tr:nth-child(even) {
  background-color: var(--surface-hover);
}

.filieres-note {
  font-size: 0.8rem;
  color: var(--text-color-secondary);
  font-style: italic;
  margin: 0.25rem 0 0 0;
}

.dark-mode .filieres-table {
  background-color: rgba(0, 0, 0, 0.2);
}

.dark-mode .filieres-table th {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>