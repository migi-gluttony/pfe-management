<template>
    <div class="printable-notes print-only">
      <!-- Header with logos and title -->
      <div class="print-header">
        <div class="header-logo left-logo">
          <img src="/src/assets/LogoESTFBS.png" alt="Logo EST" />
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
          <img src="/src/assets/LogoUSMS.png" alt="Logo USMS" />
        </div>
      </div>
      
      <hr class="header-separator" />
      
      <!-- Document Title -->
      <div class="document-title">
        <h1>Procès-verbal des Notes Finales</h1>
        <h2 v-if="currentFiliere">Filière: {{ currentFiliere.nom }}</h2>
        <h2 v-else>Toutes les Filières</h2>
        <h3>Année Universitaire: {{ currentYear }}</h3>
      </div>
      
      <!-- Notes Table -->
      <table class="notes-table">
        <thead>
          <tr>
            <th>Nom</th>
            <th>Prénom</th>
            <th>CNE</th>
            <th v-if="showAllFilieres">Filière</th>
            <th v-if="showDetails">Note Rapport ({{ pourcentages.pourcentageRapport }}%)</th>
            <th v-if="showDetails">Note Soutenance ({{ pourcentages.pourcentageSoutenance }}%)</th>
            <th v-if="showDetails">Note Encadrant ({{ pourcentages.pourcentageEncadrant }}%)</th>
            <th>Note Finale</th>
            <th>Mention</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="note in filteredNotes" :key="note.id">
            <td>{{ note.etudiant.nom }}</td>
            <td>{{ note.etudiant.prenom }}</td>
            <td>{{ note.etudiant.cne || '-' }}</td>
            <td v-if="showAllFilieres">{{ getFiliereName(note.filiereId) }}</td>
            <td v-if="showDetails">{{ formatGrade(note.noteRapport) }}</td>
            <td v-if="showDetails">{{ formatGrade(note.noteSoutenance) }}</td>
            <td v-if="showDetails">{{ formatGrade(note.noteEncadrant) }}</td>
            <td>{{ formatGrade(calculateFinalGrade(note)) }}</td>
            <td>{{ getMention(calculateFinalGrade(note)) }}</td>
          </tr>
        </tbody>
      </table>
      
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
  </template>
  
  <script setup>
  import { computed, ref } from 'vue';
  
  const props = defineProps({
    notes: {
      type: Array,
      required: true
    },
    filieres: {
      type: Array,
      required: true
    },
    selectedFiliere: {
      type: [Number, String, null],
      default: null
    },
    showAllFilieres: {
      type: Boolean,
      default: false
    },
    showDetails: {
      type: Boolean,
      default: true
    },
    pourcentages: {
      type: Object,
      default: () => ({
        pourcentageRapport: 40,
        pourcentageSoutenance: 40,
        pourcentageEncadrant: 20
      })
    }
  });
  
  // Get current academic year
  const currentYear = computed(() => {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    
    // If we're in the second half of the academic year (Jan-Aug)
    if (month < 8) {
      return `${year-1}/${year}`;
    } else {
      return `${year}/${year+1}`;
    }
  });
  
  // Get current filiere object
  const currentFiliere = computed(() => {
    if (!props.showAllFilieres && props.selectedFiliere) {
      return props.filieres.find(f => f.id === props.selectedFiliere) || null;
    }
    return null;
  });
  
  // Filter notes based on selectedFiliere or show all if showAllFilieres is true
  const filteredNotes = computed(() => {
    if (props.showAllFilieres) {
      return props.notes;
    } else if (props.selectedFiliere) {
      return props.notes.filter(note => note.filiereId === props.selectedFiliere);
    }
    return props.notes;
  });
  
  // Utility functions
  function formatGrade(grade) {
    if (grade === null || grade === undefined) return '-';
    return grade.toFixed(2);
  }
  
  function calculateFinalGrade(note) {
    if (!canShowFinalGrade(note)) return null;
    
    const rapportWeight = props.pourcentages.pourcentageRapport / 100;
    const soutenanceWeight = props.pourcentages.pourcentageSoutenance / 100;
    const encadrantWeight = props.pourcentages.pourcentageEncadrant / 100;
    
    return (
      note.noteRapport * rapportWeight +
      note.noteSoutenance * soutenanceWeight +
      note.noteEncadrant * encadrantWeight
    );
  }
  
  function getMention(grade) {
    if (grade === null || grade === undefined) return '-';
    if (grade >= 16) return 'Excellent';
    if (grade >= 14) return 'Très Bien';
    if (grade >= 12) return 'Bien';
    if (grade >= 10) return 'Passable';
    return 'Insuffisant';
  }
  
  function canShowFinalGrade(note) {
    return (
      note.noteRapport != null &&
      note.noteSoutenance != null &&
      note.noteEncadrant != null
    );
  }
  
  function getFiliereName(filiereId) {
    const filiere = props.filieres.find(f => f.id === filiereId);
    return filiere ? filiere.nom : '-';
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
      visibility: visible;
    }
    
    .printable-notes {
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      padding: 20px;
    }
    
    /* Reset all styles */
    .printable-notes * {
      color: black !important;
      background: white !important;
      font-family: Arial, sans-serif !important;
      box-shadow: none !important;
    }
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
  }
  
  .header-logo img {
    width: 100%;
    height: auto;
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
  }
  
  .document-title h2, .document-title h3 {
    font-size: 14px;
    font-weight: normal;
    margin: 5px 0;
  }
  
  /* Table styles */
  .notes-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 30px;
    page-break-inside: auto;
  }
  
  .notes-table th, .notes-table td {
    border: 1px solid black;
    padding: 8px;
    text-align: center;
    font-size: 12px;
  }
  
  .notes-table th {
    font-weight: bold;
    background-color: #f2f2f2;
  }
  
  .notes-table tr {
    page-break-inside: avoid;
    page-break-after: auto;
  }
  
  /* Signature section */
  .signature-section {
    margin-top: 40px;
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