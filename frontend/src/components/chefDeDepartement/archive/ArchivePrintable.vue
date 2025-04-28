<template>
    <div class="printable-component print-only">
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
            <h1>{{ title }}</h1>
            <h2>Année Universitaire: {{ academicYear }}</h2>
            <h3 v-if="selectedFiliere && selectedFiliere !== 'Toutes les filières'">
                Filière: {{ selectedFiliere }}
            </h3>
        </div>
        
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
                <tr v-for="row in data" :key="row.id">
                    <td v-for="column in columns" :key="column.field">
                        {{ getCellValue(row, column) }}
                    </td>
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
    const value = column.field.split('.').reduce((obj, key) => obj && obj[key], row);
    
    if (value === null || value === undefined) return '-';
    if (typeof value === 'number') return column.field.includes('note') ? value.toFixed(2) : value;
    
    return value;
}

function formatCurrentDate() {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date().toLocaleDateString('fr-FR', options);
}
</script>

<style scoped>
@media print {
    .printable-component {
        display: block;
        width: 100%;
        margin: 0;
        padding: 20px;
    }
    
    @page {
        size: A4;
        margin: 1cm;
    }
}

.print-only {
    display: none;
}

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

.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 30px;
    page-break-inside: avoid;
}

.data-table th, .data-table td {
    border: 1px solid black;
    padding: 8px;
    text-align: left;
    font-size: 12px;
}

.data-table th {
    font-weight: bold;
    background-color: #f2f2f2;
}

.data-table tr {
    page-break-inside: avoid;
}

.signature-section {
    margin-top: 50px;
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