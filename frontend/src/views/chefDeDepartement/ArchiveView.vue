<template>
    <div class="archive-management">
        <Toast />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher dans les archives..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Archive Header -->
        <div class="archive-header no-print">
            <h1 class="page-title">Archives</h1>
            <div class="header-actions">
                <Dropdown
                    v-model="selectedYear"
                    :options="academicYears"
                    optionLabel="annee"
                    optionValue="id"
                    placeholder="Choisir une année académique"
                    class="year-dropdown"
                    @change="onYearChange"
                />
                <Button
                    label="Exporter l'année"
                    icon="pi pi-download"
                    class="p-button-primary"
                    :disabled="!selectedYear"
                    @click="exportYearData"
                />
            </div>
        </div>

        <!-- Main Content with Sidebar -->
        <div class="archive-content" v-if="selectedYear">
            <!-- Sidebar for Section Selection -->
            <div class="sidebar no-print">
                <h3>Sections</h3>
                <div class="section-menu">
                    <Button
                        v-for="section in availableSections"
                        :key="section.id"
                        :label="section.label"
                        :icon="section.icon"
                        :class="['section-button', { 'active': selectedSection === section.id }]"
                        @click="selectSection(section.id)"
                    />
                </div>

                <div class="filiere-filter" v-if="selectedSection">
                    <h3>Filière</h3>
                    <Dropdown
                        v-model="selectedFiliere"
                        :options="[{ id: 'all', nom: 'Toutes les filières' }, ...filieres]"
                        optionLabel="nom"
                        optionValue="id"
                        placeholder="Choisir une filière"
                        class="w-full"
                        @change="onFiliereChange"
                    />
                </div>
            </div>

            <!-- Data Section -->
            <div class="data-display" v-if="selectedSection">
                <div class="data-header no-print">
                    <h2>{{ getSectionTitle() }}</h2>
                    <Button
                        label="Imprimer"
                        icon="pi pi-print"
                        class="p-button-primary"
                        @click="printSection"
                    />
                </div>

                <!-- Generic Data Table -->
                <Card class="table-card no-print">
                    <template #content>
                        <DataTable
                            :value="filteredData"
                            :loading="loading"
                            responsiveLayout="scroll"
                            stripedRows
                            :paginator="filteredData.length > 10"
                            :rows="10"
                            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                            :rowsPerPageOptions="[10, 20, 50]"
                            :currentPageReportTemplate="getPaginatorTemplate()"
                            emptyMessage="Aucune donnée trouvée"
                        >
                            <Column v-for="column in getColumns()" 
                                :key="column.field" 
                                :field="column.field" 
                                :header="column.header" 
                                :sortable="column.sortable || false"
                                :style="column.style || {}"
                            >
                                <template #body="slotProps" v-if="column.template">
                                    <component :is="column.template(slotProps.data)" />
                                </template>
                            </Column>
                        </DataTable>
                    </template>
                </Card>
            </div>
        </div>

        <!-- Generic Printable Component -->
        <ArchivePrintable
            v-if="isPrinting"
            :data="filteredData"
            :columns="getColumns()"
            :title="getSectionTitle()"
            :academicYear="getSelectedYear()"
            :selectedFiliere="getSelectedFiliere()"
        />
    </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, h } from 'vue';
import { useToast } from 'primevue/usetoast';
import ApiService from '@/services/ApiService';
import UserInfoHeader from '@/components/UserInfoHeader.vue';
import ArchivePrintable from '@/components/chefDeDepartement/archive/ArchivePrintable.vue';

// PrimeVue Components
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Tag from 'primevue/tag';

// Component state
const academicYears = ref([]);
const filieres = ref([]);
const selectedYear = ref(null);
const selectedSection = ref(null);
const selectedFiliere = ref('all');
const searchQuery = ref('');
const loading = ref(false);
const sectionData = ref({});
const isPrinting = ref(false);

// Services
const toast = useToast();

// Available sections
const availableSections = [
    { id: 'comptes', label: 'Comptes', icon: 'pi pi-users' },
    { id: 'binomes', label: 'Binômes', icon: 'pi pi-user-plus' },
    { id: 'sujets', label: 'Sujets', icon: 'pi pi-book' },
    { id: 'soutenances', label: 'Soutenances', icon: 'pi pi-calendar' },
    { id: 'notes', label: 'Notes', icon: 'pi pi-chart-bar' }
];

const filteredData = computed(() => {
    const query = searchQuery.value.toLowerCase();
    let data = [];
    
    switch (selectedSection.value) {
        case 'comptes':
            data = sectionData.value.comptes || [];
            break;
        case 'binomes':
            data = sectionData.value.binomes || [];
            break;
        case 'sujets':
            data = sectionData.value.sujets || [];
            break;
        case 'soutenances':
            data = sectionData.value.soutenances || [];
            break;
        case 'notes':
            data = sectionData.value.notes || [];
            break;
    }
    
    if (query && data.length > 0) {
        // Generic search across all string fields
        data = data.filter(item => {
            return Object.values(item).some(value => 
                String(value).toLowerCase().includes(query)
            );
        });
    }
    
    return data;
});
function getColumns() {
    switch (selectedSection.value) {
        case 'comptes':
            return [
                { field: 'nom', header: 'Nom', sortable: true },
                { field: 'prenom', header: 'Prénom', sortable: true },
                { field: 'email', header: 'Email', sortable: true },
                { 
                    field: 'role', 
                    header: 'Rôle', 
                    sortable: true,
                    body: (data) => getRoleLabel(data.role)
                },
                { field: 'cni', header: 'CNI', body: (data) => data.role !== 'ETUDIANT' ? data.cni : '-' },
                { field: 'cne', header: 'CNE', body: (data) => data.role === 'ETUDIANT' ? data.cne : '-' },
                { field: 'filiereName', header: 'Filière', body: (data) => data.role === 'ETUDIANT' ? data.filiereName : '-' }
            ];
        
        case 'binomes':
            return [
                { 
                    field: 'etudiant1', 
                    header: 'Étudiant 1', 
                    body: (data) => {
                        if (data.etudiant1) {
                            return `${data.etudiant1.nom || ''} ${data.etudiant1.prenom || ''}`.trim() || '-';
                        }
                        return '-';
                    }
                },
                { 
                    field: 'etudiant2', 
                    header: 'Étudiant 2', 
                    body: (data) => {
                        if (data.etudiant2) {
                            return `${data.etudiant2.nom || ''} ${data.etudiant2.prenom || ''}`.trim() || '-';
                        }
                        return '-';
                    }
                },
                { 
                    field: 'encadrant', 
                    header: 'Encadrant', 
                    body: (data) => {
                        if (data.encadrant) {
                            return `${data.encadrant.nom || ''} ${data.encadrant.prenom || ''}`.trim() || '-';
                        }
                        return '-';
                    }
                },
                { 
                    field: 'sujet', 
                    header: 'Sujet', 
                    body: (data) => data.sujet?.titre || '-'
                },
                { field: 'filiereName', header: 'Filière' }
            ];
        
        case 'sujets':
            return [
                { field: 'titre', header: 'Titre', sortable: true },
                { field: 'theme', header: 'Thème', sortable: true },
                { field: 'description', header: 'Description', style: { maxWidth: '300px' } },
                { field: 'filiereName', header: 'Filière', sortable: true }
            ];
        
        case 'soutenances':
            return [
                { 
                    field: 'date', 
                    header: 'Date', 
                    sortable: true, 
                    body: (data) => data.date ? new Date(data.date).toLocaleDateString('fr-FR') : '-' 
                },
                { field: 'heure', header: 'Heure', sortable: true },
                { 
                    field: 'salle', 
                    header: 'Salle', 
                    body: (data) => data.salle?.nom || '-'
                },
                { 
                    field: 'binome', 
                    header: 'Binôme', 
                    body: (data) => {
                        if (!data.binome) return '-';
                        let result = '';
                        if (data.binome.etudiant1) {
                            result += `${data.binome.etudiant1.nom || ''} ${data.binome.etudiant1.prenom || ''}`.trim();
                        }
                        if (data.binome.etudiant2) {
                            result += result ? ' / ' : '';
                            result += `${data.binome.etudiant2.nom || ''} ${data.binome.etudiant2.prenom || ''}`.trim();
                        }
                        return result || '-';
                    }
                },
                { 
                    field: 'jury', 
                    header: 'Jury', 
                    body: (data) => {
                        let result = '';
                        if (data.jury1) {
                            result += `${data.jury1.nom || ''} ${data.jury1.prenom || ''}`.trim();
                        }
                        if (data.jury2) {
                            result += result ? ' / ' : '';
                            result += `${data.jury2.nom || ''} ${data.jury2.prenom || ''}`.trim();
                        }
                        return result || '-';
                    }
                }
            ];
        
        case 'notes':
            return [
                { field: 'etudiant.nom', header: 'Nom', sortable: true, body: (data) => data.etudiant?.nom || '-' },
                { field: 'etudiant.prenom', header: 'Prénom', sortable: true, body: (data) => data.etudiant?.prenom || '-' },
                { field: 'etudiant.cne', header: 'CNE', body: (data) => data.etudiant?.cne || '-' },
                { field: 'noteRapport', header: 'Note Rapport', sortable: true, body: (data) => formatGrade(data.noteRapport) },
                { field: 'noteSoutenance', header: 'Note Soutenance', sortable: true, body: (data) => formatGrade(data.noteSoutenance) },
                { field: 'noteEncadrant', header: 'Note Encadrant', sortable: true, body: (data) => formatGrade(data.noteEncadrant) },
                { field: 'noteFinale', header: 'Note Finale', sortable: true, body: (data) => formatGrade(calculateFinalGrade(data)) },
                { field: 'filiereName', header: 'Filière', sortable: true }
            ];
        
        default:
            return [];
    }
}
function getPaginatorTemplate() {
    const itemLabel = selectedSection.value === 'binomes' ? 'binômes' : selectedSection.value;
    return `Montrant {first} à {last} sur {totalRecords} ${itemLabel}`;
}

// Fetch metadata on mount
onMounted(async () => {
    await fetchArchiveMetadata();
});

// Methods
async function fetchArchiveMetadata() {
    loading.value = true;
    try {
        const response = await ApiService.get('/chef_de_departement/archive/metadata');
        academicYears.value = response.academicYears;
        filieres.value = response.filieres;
    } catch (error) {
        handleApiError(error, 'Erreur lors du chargement des métadonnées');
    } finally {
        loading.value = false;
    }
}

async function onYearChange() {
    selectedSection.value = null;
    selectedFiliere.value = 'all';
    sectionData.value = {};
}

async function selectSection(sectionId) {
    selectedSection.value = sectionId;
    await loadSectionData();
}

async function onFiliereChange() {
    await loadSectionData();
}

async function loadSectionData() {
    if (!selectedYear.value || !selectedSection.value) return;

    loading.value = true;
    try {
        const filiereParam = selectedFiliere.value === 'all' ? 'all' : selectedFiliere.value;
        
        switch (selectedSection.value) {
            case 'comptes':
                const comptes = await ApiService.get(
                    `/chef_de_departement/archive/comptes/all/${selectedYear.value}`
                );
                sectionData.value = { comptes: comptes.comptes };
                break;
            
            case 'binomes':
                const binomes = await ApiService.get(
                    `/chef_de_departement/archive/binomes/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { binomes: binomes.binomes };
                break;
            
            case 'sujets':
                const sujets = await ApiService.get(
                    `/chef_de_departement/archive/sujets/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { sujets: sujets.sujets };
                break;
            
            case 'soutenances':
                const soutenances = await ApiService.get(
                    `/chef_de_departement/archive/soutenances/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { soutenances };
                break;
            
            case 'notes':
                const notes = await ApiService.get(
                    `/chef_de_departement/archive/notes/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { 
                    notes: notes.notes,
                    pourcentages: notes.pourcentages
                };
                break;
        }
    } catch (error) {
        handleApiError(error, 'Erreur lors du chargement des données');
    } finally {
        loading.value = false;
    }
}

async function exportYearData() {
    if (!selectedYear.value) return;
    
    loading.value = true;
    try {
        const data = await ApiService.get(`/chef_de_departement/archive/export/${selectedYear.value}`);
        
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `archive_${data.academicYear}.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        toast.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Export réussi',
            life: 3000
        });
    } catch (error) {
        handleApiError(error, 'Erreur lors de l\'export');
    } finally {
        loading.value = false;
    }
}

function printSection() {
    isPrinting.value = true;
    nextTick(() => {
        window.print();
        isPrinting.value = false;
    });
}

function getSectionTitle() {
    const section = availableSections.find(s => s.id === selectedSection.value);
    return section ? section.label : '';
}

function getSelectedYear() {
    const year = academicYears.value.find(y => y.id === selectedYear.value);
    return year ? year.annee : '';
}

function getSelectedFiliere() {
    if (selectedFiliere.value === 'all') return 'Toutes les filières';
    const filiere = filieres.value.find(f => f.id === selectedFiliere.value);
    return filiere ? filiere.nom : '';
}

function formatGrade(grade) {
    return grade !== null && grade !== undefined ? grade.toFixed(2) : '-';
}

function calculateFinalGrade(note) {
    if (!sectionData.value.pourcentages || !canShowFinalGrade(note)) return null;
    
    const { pourcentageRapport, pourcentageSoutenance, pourcentageEncadrant } = sectionData.value.pourcentages;
    
    return (
        note.noteRapport * (pourcentageRapport / 100) +
        note.noteSoutenance * (pourcentageSoutenance / 100) +
        note.noteEncadrant * (pourcentageEncadrant / 100)
    );
}

function canShowFinalGrade(note) {
    return note.noteRapport != null && note.noteSoutenance != null && note.noteEncadrant != null;
}

function getRoleLabel(role) {
    const labels = {
        'ETUDIANT': 'Étudiant',
        'ENCADRANT': 'Encadrant',
        'JURY': 'Jury',
        'CHEF_DE_DEPARTEMENT': 'Chef de Département'
    };
    return labels[role] || role;
}

function getRoleSeverity(role) {
    const severities = {
        'ETUDIANT': 'info',
        'ENCADRANT': 'success',
        'JURY': 'warning',
        'CHEF_DE_DEPARTEMENT': 'danger'
    };
    return severities[role] || 'secondary';
}

function handleHeaderSearch(query) {
    searchQuery.value = query;
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

<style scoped>
.archive-management {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

.archive-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;
}

.header-actions {
    display: flex;
    gap: 1rem;
    align-items: center;
}

.year-dropdown {
    min-width: 250px;
}

.page-title {
    margin: 0;
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
}

.archive-content {
    display: flex;
    gap: 2rem;
}

.sidebar {
    width: 250px;
    background-color: var(--surface-section);
    border-radius: 8px;
    padding: 1rem;
    height: fit-content;
    position: sticky;
    top: 1rem;
}

.sidebar h3 {
    margin-bottom: 1rem;
    font-size: 1.1rem;
    color: var(--text-color);
}

.section-menu {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.section-button {
    width: 100%;
    justify-content: flex-start;
    padding-left: 1rem;
}

.section-button.active {
    background-color: var(--primary-color);
    color: white;
}

.filiere-filter {
    margin-top: 2rem;
}

.data-display {
    flex: 1;
    min-width: 0;
}

.data-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.table-card {
    margin-bottom: 2rem;
}

@media (max-width: 768px) {
    .archive-content {
        flex-direction: column;
    }
    
    .sidebar {
        width: 100%;
        position: static;
    }
}

@media print {
    .no-print {
        display: none !important;
    }
}
</style>