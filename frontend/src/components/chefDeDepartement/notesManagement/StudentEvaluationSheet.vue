<template>
  <div class="evaluation-sheet print-only">
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
      <h1>Fiche d'évaluation du Projet de Fin d'Études</h1>
      <h2>{{ student.prenom }} {{ student.nom }}</h2>
      <h3>Filière: {{ filiereName }}</h3>
    </div>
    
    <!-- Student Information -->
    <div class="student-info">
      <div class="info-row">
        <div class="info-field">
          <span class="field-label">CNE:</span>
          <span class="field-value">{{ student.cne || 'Non spécifié' }}</span>
        </div>
      </div>
      
      <div class="info-row">
        <div class="info-field">
          <span class="field-label">Titre du rapport:</span>
          <span class="field-value">{{ noteDetail?.noteRapportDetail?.titre || 'Non spécifié' }}</span>
        </div>
      </div>
      
      <div class="info-row">
        <div class="info-field">
          <span class="field-label">Nom et prénom de l'encadrant:</span>
          <span class="field-value">{{ noteDetail?.noteEncadrantDetail?.encadrantPrenom || '' }} {{ noteDetail?.noteEncadrantDetail?.encadrantNom || '' }}</span>
        </div>
      </div>
      
      <div class="info-row">
        <div class="info-field">
          <span class="field-label">Date de la soutenance:</span>
          <span class="field-value">{{ soutenanceDate || '_____________________' }}</span>
        </div>
        
        <div class="info-field">
          <span class="field-label">à</span>
          <span class="field-value">{{ soutenanceTime || '_________' }}</span>
        </div>
      </div>
      
      <div class="info-row">
        <div class="info-field">
          <span class="field-label">Lieu de la soutenance:</span>
          <span class="field-value">{{ soutenanceLocation || '_____________________' }}</span>
        </div>
      </div>
    </div>
    
    <!-- Mode d'évaluation -->
    <div class="evaluation-mode">
      <h3>Mode d'évaluation:</h3>
      <table class="mode-table">
        <tr>
          <td>Rapport</td>
          <td>{{ pourcentages.pourcentageRapport }}%</td>
          <td>/20</td>
        </tr>
        <tr>
          <td>Présentation orale et discsssions</td>
          <td>{{ pourcentages.pourcentageSoutenance }}%</td>
          <td>/20</td>
        </tr>
        <tr>
          <td>Note des encadrants</td>
          <td>{{ pourcentages.pourcentageEncadrant }}%</td>
          <td>/20</td>
        </tr>
        <tr class="total-row">
          <td>Note finale</td>
          <td>100%</td>
          <td>/20</td>
        </tr>
      </table>
    </div>
    
    <!-- Evaluation Details -->
    <div class="evaluation-details">
      <!-- Encadrant Notes -->
      <div class="detail-section">
        <h3>1. Évaluation de l'Encadrant</h3>
        <table class="detail-table">
          <tr>
            <td>Technique</td>
            <td>{{ formatGrade(noteDetail?.noteEncadrantDetail?.technicalScore) }}</td>
          </tr>
          <tr>
            <td>Rapport</td>
            <td>{{ formatGrade(noteDetail?.noteEncadrantDetail?.reportScore) }}</td>
          </tr>
          <tr>
            <td>Progression</td>
            <td>{{ formatGrade(noteDetail?.noteEncadrantDetail?.progressScore) }}</td>
          </tr>
          <tr>
            <td>Professionnalisme</td>
            <td>{{ formatGrade(noteDetail?.noteEncadrantDetail?.professionalismScore) }}</td>
          </tr>
          <tr class="total-row">
            <td>Note Encadrant ({{ pourcentages.pourcentageEncadrant }}%)</td>
            <td>{{ formatGrade(note?.noteEncadrant) }}</td>
          </tr>
        </table>
        
        <div class="comment-section" v-if="noteDetail?.noteEncadrantDetail?.commentaire">
          <p class="comment-label">Commentaire:</p>
          <p class="comment-text">{{ noteDetail.noteEncadrantDetail.commentaire }}</p>
        </div>
      </div>
      
      <!-- Soutenance Notes -->
      <div class="detail-section">
        <h3>2. Évaluation de la Soutenance</h3>
        <table class="detail-table">
          <tr>
            <td>Présentation</td>
            <td>{{ formatGrade(noteDetail?.noteSoutenanceDetail?.presentationScore) }}</td>
          </tr>
          <tr>
            <td>Questions/Réponses</td>
            <td>{{ formatGrade(noteDetail?.noteSoutenanceDetail?.qaScore) }}</td>
          </tr>
          <tr>
            <td>Gestion du temps</td>
            <td>{{ formatGrade(noteDetail?.noteSoutenanceDetail?.timeManagementScore) }}</td>
          </tr>
          <tr class="total-row">
            <td>Note Soutenance ({{ pourcentages.pourcentageSoutenance }}%)</td>
            <td>{{ formatGrade(note?.noteSoutenance) }}</td>
          </tr>
        </table>
        
        <div class="comment-section" v-if="noteDetail?.noteSoutenanceDetail?.commentaire">
          <p class="comment-label">Commentaire:</p>
          <p class="comment-text">{{ noteDetail.noteSoutenanceDetail.commentaire }}</p>
        </div>
      </div>
      
      <!-- Rapport Notes -->
      <div class="detail-section">
        <h3>3. Évaluation du Rapport</h3>
        <table class="detail-table">
          <tr>
            <td>Contenu technique</td>
            <td>{{ formatGrade(noteDetail?.noteRapportDetail?.technicalScore) }}</td>
          </tr>
          <tr>
            <td>Structure</td>
            <td>{{ formatGrade(noteDetail?.noteRapportDetail?.structureScore) }}</td>
          </tr>
          <tr>
            <td>Originalité</td>
            <td>{{ formatGrade(noteDetail?.noteRapportDetail?.originalityScore) }}</td>
          </tr>
          <tr class="total-row">
            <td>Note Rapport ({{ pourcentages.pourcentageRapport }}%)</td>
            <td>{{ formatGrade(note?.noteRapport) }}</td>
          </tr>
        </table>
        
        <div class="comment-section" v-if="noteDetail?.noteRapportDetail?.commentaire">
          <p class="comment-label">Commentaire:</p>
          <p class="comment-text">{{ noteDetail.noteRapportDetail.commentaire }}</p>
        </div>
      </div>
      
      <!-- Final Note -->
      <div class="final-note-section">
        <table class="detail-table">
          <tr>
            <td>Note finale</td>
            <td>{{ formatGrade(calculateFinalGrade()) }}</td>
          </tr>
          <tr>
            <td>Mention</td>
            <td>{{ getMention(calculateFinalGrade()) }}</td>
          </tr>
        </table>
      </div>
    </div>
    
    <!-- Jury Members -->
    <div class="jury-section">
      <h3>Membre de jury:</h3>
      <table class="jury-table">
        <tr>
          <th>Membres</th>
          <th>Nom et Prénom</th>
          <th>Émargement</th>
        </tr>
        <tr v-for="(jury, index) in juryMembers" :key="index">
          <td></td>
          <td>{{ jury.juryNom }} {{ jury.juryPrenom }}</td>
          <td></td>
        </tr>
        <!-- Add empty rows if needed -->
        <tr v-for="i in Math.max(0, 3 - (juryMembers?.length || 0))" :key="`empty-${i}`">
          <td></td>
          <td></td>
          <td></td>
        </tr>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  student: {
    type: Object,
    required: true
  },
  note: {
    type: Object,
    required: true
  },
  noteDetail: {
    type: Object,
    default: () => ({})
  },
  filiereName: {
    type: String,
    default: ''
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

// Computed jury members from soutenance details
const juryMembers = computed(() => {
  if (props.noteDetail?.noteSoutenanceDetail?.juryEvaluations) {
    return props.noteDetail.noteSoutenanceDetail.juryEvaluations;
  }
  return [];
});

// Get soutenance details if available
const soutenanceDate = computed(() => {
  if (props.noteDetail?.noteSoutenanceDetail?.date) {
    return new Date(props.noteDetail.noteSoutenanceDetail.date)
      .toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' });
  }
  return null;
});

const soutenanceTime = computed(() => {
  if (props.noteDetail?.noteSoutenanceDetail?.heure) {
    return props.noteDetail.noteSoutenanceDetail.heure;
  }
  return null;
});

const soutenanceLocation = computed(() => {
  if (props.noteDetail?.noteSoutenanceDetail?.salle) {
    return props.noteDetail.noteSoutenanceDetail.salle;
  }
  return null;
});

// Utility functions
function formatGrade(grade) {
  if (grade === null || grade === undefined) return '-';
  return typeof grade === 'number' ? grade.toFixed(2) : grade;
}

function calculateFinalGrade() {
  if (!canShowFinalGrade()) return null;
  
  const rapportWeight = props.pourcentages.pourcentageRapport / 100;
  const soutenanceWeight = props.pourcentages.pourcentageSoutenance / 100;
  const encadrantWeight = props.pourcentages.pourcentageEncadrant / 100;
  
  return (
    props.note.noteRapport * rapportWeight +
    props.note.noteSoutenance * soutenanceWeight +
    props.note.noteEncadrant * encadrantWeight
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

function canShowFinalGrade() {
  return (
    props.note.noteRapport != null &&
    props.note.noteSoutenance != null &&
    props.note.noteEncadrant != null
  );
}
</script>

<style>
/* These styles will only apply when printing */
@media print {
  .evaluation-sheet {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    padding: 20px;
    font-size: 12px;
  }
  
  @page {
    size: A4;
    margin: 1cm;
    title: attr(data-page-title);
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
  margin-bottom: 20px;
}

.document-title h1 {
  font-size: 18px;
  margin-bottom: 5px;
}

.document-title h2, .document-title h3 {
  font-size: 14px;
  margin: 5px 0;
}

/* Student info styles */
.student-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.info-field {
  display: flex;
  align-items: baseline;
  margin-right: 20px;
}

.field-label {
  font-weight: bold;
  margin-right: 5px;
}

.field-value {
  border-bottom: 1px solid black;
  padding: 0 5px;
  min-width: 150px;
}

/* Evaluation mode table */
.evaluation-mode {
  margin-bottom: 20px;
}

.mode-table {
  width: 80%;
  border-collapse: collapse;
  margin: 0 auto;
}

.mode-table td {
  border: 1px solid black;
  padding: 8px;
}

.total-row {
  font-weight: bold;
}

/* Evaluation details */
.evaluation-details {
  margin-bottom: 30px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  font-size: 14px;
  margin-bottom: 10px;
}

.detail-table {
  width: 80%;
  border-collapse: collapse;
  margin-bottom: 10px;
}

.detail-table td {
  border: 1px solid black;
  padding: 8px;
}

.comment-section {
  margin-top: 10px;
  padding-left: 20px;
}

.comment-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.comment-text {
  font-style: italic;
  padding-left: 10px;
}

/* Jury section */
.jury-section {
  margin-top: 30px;
}

.jury-table {
  width: 100%;
  border-collapse: collapse;
}

.jury-table th, .jury-table td {
  border: 1px solid black;
  padding: 8px;
  text-align: left;
}

/* Final note section */
.final-note-section {
  margin-top: 30px;
}
</style>