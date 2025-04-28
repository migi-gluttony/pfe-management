<template>
  <div class="printable-component print-only">
    <!-- Single Filiere Mode -->
    <div class="filiere-document">
      <!-- Header with logos and title -->
      <div class="print-header">
        <div class="header-logo left-logo">
          <img src="@/assets/LogoESTFBS.png" alt="Logo EST" />
        </div>
        <div class="header-text">
          <div class="header-title">
            <p>Royaume du Maroc</p>
            <p>Ministère de l'Education Nationale, de la Formation Professionnelle,</p>
            <p>de l'Enseignement Supérieur et de la Recherche Scientifique</p>
            <p>Université Sultan Moulay Slimane</p>
            <p>L'École Supérieure de Technologie – Fkih Ben Salah</p>
          </div>
        </div>
        <div class="header-logo right-logo">
          <img src="@/assets/LogoUSMS.png" alt="Logo USMS" />
        </div>
      </div>
      
      <hr class="header-separator" />
      
      <!-- Document Title -->
      <div class="document-title">
        <h1>{{ title }}</h1>
        <h2>Année Universitaire: {{ academicYear }}</h2>
        <h3 v-if="selectedFiliere && selectedFiliere !== 'Toutes les filières'">
            Filière: {{ selectedFiliere }}
        </h3>
      </div>
      
      <!-- Content Area -->
      <div class="content-area">
        <!-- Data Table -->
        <table class="data-table">
          <thead>
            <tr>
              <th v-for="column in columns" :key="column.field">
                {{ column.header }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in data" :key="index">
              <td v-for="column in columns" :key="column.field">
                {{ getCellValue(row, column) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Signature section -->
      <div class="signature-section">
        <div class="date-section">
          <p>Fait à Fkih Ben Salah, le {{ formatCurrentDate() }}</p>
        </div>
        <div class="signature">
          <p>Signature du Chef de Département</p>
          <div class="signature-line"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
    data: {
        type: Array,
        default: () => []
    },
    columns: {
        type: Array,
        default: () => []
    },
    title: {
        type: String,
        default: ''
    },
    academicYear: {
        type: String,
        default: ''
    },
    selectedFiliere: {
        type: String,
        default: ''
    }
});

function getCellValue(row, column) {
    if (column.body) {
        return column.body(row);
    }
    
    // Handle nested properties (e.g., 'etudiant.nom')
    const value = getNestedValue(row, column.field);
    
    if (value === null || value === undefined) return '-';
    if (typeof value === 'number') return column.field.includes('note') ? value.toFixed(2) : value;
    
    return value;
}

// Get nested value from object using dot notation
function getNestedValue(obj, path) {
    if (!obj || !path) return '';
    
    // Handle JSON strings that might not have been parsed
    if (typeof obj === 'string' && (obj.startsWith('{') || obj.startsWith('['))) {
        try {
            obj = JSON.parse(obj);
        } catch (e) {
            // Not valid JSON, keep as is
        }
    }
    
    const value = path.split('.').reduce((o, key) => {
        if (o === null || o === undefined) return null;
        
        // Handle case where o[key] is a JSON string
        if (typeof o[key] === 'string' && (o[key].startsWith('{') || o[key].startsWith('['))) {
            try {
                return JSON.parse(o[key]);
            } catch (e) {
                return o[key];
            }
        }
        
        return o[key];
    }, obj);
    
    return value !== null && value !== undefined ? value : '';
}

function formatCurrentDate() {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date().toLocaleDateString('fr-FR', options);
}
</script>

<style>
/* These styles will only apply when printing */
@media print {
  body * {
    visibility: hidden;
  }
  
  .print-only, .print-only * {
    visibility: visible !important;
  }
  
  .header-logo, .header-logo img {
    visibility: visible !important;
    display: block !important;
  }
  
  .printable-component {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
  }
  
  /* Reset all styles */
  .printable-component * {
    color: black !important;
    background: white !important;
    font-family: Arial, sans-serif !important;
    box-shadow: none !important;
  }
  
  @page {
    size: A4;
    margin: 1cm;
  }
  
  .page-break {
    page-break-after: always;
    break-after: page;
  }
}

/* Filiere document structure */
.filiere-document {
  position: relative;
  min-height: 100vh;
  padding: 20px;
  padding-bottom: 150px; /* Make room for the signature at bottom */
  box-sizing: border-box;
  page-break-after: always;
}

/* Header styles */
.print-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.header-logo {
  width: 80px;
  height: 80px;
  display: block;
}

.header-logo img {
  width: 100%;
  height: auto;
  max-width: 80px;
  max-height: 80px;
  display: block;
}

.header-text {
  text-align: center;
}

.header-title p {
  margin: 2px 0;
  font-size: 12px;
}

.header-separator {
  border: none;
  border-top: 1px solid black;
  margin: 10px 0 20px 0;
}

/* Document title */
.document-title {
  text-align: center;
  margin-bottom: 30px;
}

.document-title h1 {
  font-size: 18px;
  margin-bottom: 5px;
  font-weight: bold;
}

.document-title h2, .document-title h3 {
  font-size: 14px;
  font-weight: normal;
  margin: 5px 0;
}

/* Content area */
.content-area {
  width: 100%;
}

/* Table styles */
.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
  page-break-inside: avoid;
}

.data-table th, .data-table td {
  border: 1px solid black;
  padding: 8px;
  text-align: center;
  font-size: 12px;
}

.data-table th {
  font-weight: bold;
  background-color: #f2f2f2;
}

.data-table tr {
  page-break-inside: avoid;
  page-break-after: auto;
}

/* Signature section at bottom of page */
.signature-section {
  position: absolute;
  bottom: 40px;
  left: 20px;
  right: 20px;
  display: flex;
  justify-content: space-between;
}

.date-section, .signature {
  width: 45%;
}

.signature {
  text-align: center;
}

.signature-line {
  border-top: 1px solid black;
  height: 40px;
  margin-top: 20px;
}
</style>