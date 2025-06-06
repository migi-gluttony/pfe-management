// Updated frontend/src/views/chefDeDepartement/ArchiveView.vue

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

        <!-- Loading Indicator -->
        <ProgressSpinner v-if="initialLoading" class="loading-spinner" />

        <!-- Main Content with Sidebar -->
        <div class="archive-content" v-if="selectedYear && !initialLoading">
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

                <!-- Account Type Filter -->
                <div class="account-type-filter" v-if="selectedSection === 'comptes'">
                    <h3>Type de compte</h3>
                    <Dropdown
                        v-model="selectedAccountType"
                        :options="[
                            { value: 'all', label: 'Tous les comptes' },
                            { value: 'ETUDIANT', label: 'Étudiants' },
                            { value: 'ENCADRANT', label: 'Encadrants' },
                            { value: 'JURY', label: 'Jurys' }
                        ]"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Choisir un type de compte"
                        class="w-full"
                        @change="loadSectionData"
                    />
                </div>
                
                <!-- Filière Filter -->
                <div class="filiere-filter" v-if="(selectedSection && selectedSection !== 'comptes') || (selectedSection === 'comptes' && selectedAccountType === 'ETUDIANT')">
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
                                <template #body="slotProps">
                                    <component 
                                        v-if="column.template"
                                        :is="column.template" 
                                        :data="slotProps.data" 
                                    />
                                    <template v-else-if="column.body">
                                        {{ column.body(slotProps.data) }}
                                    </template>
                                    <template v-else>
                                        {{ getNestedValue(slotProps.data, column.field) }}
                                    </template>
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
            :columns="getPrintColumns()"
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
import ProgressSpinner from 'primevue/progressspinner';

// Component state
const academicYears = ref([]);
const filieres = ref([]);
const selectedYear = ref(null);
const selectedSection = ref(null);
const selectedFiliere = ref('all');
const searchQuery = ref('');
const loading = ref(false);
const initialLoading = ref(true);
const sectionData = ref({});
const isPrinting = ref(false);

// Default section to show
const DEFAULT_SECTION = 'comptes';

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

// Account type filter
const selectedAccountType = ref('all');

const filteredData = computed(() => {
    const query = searchQuery.value.toLowerCase();
    let data = [];
    
    switch (selectedSection.value) {
        case 'comptes':
            // Get all accounts except CHEF_DE_DEPARTEMENT
            data = (sectionData.value.comptes || []).filter(item => item.role !== 'CHEF_DE_DEPARTEMENT');
            
            // Apply account type filter
            if (selectedAccountType.value !== 'all') {
                data = data.filter(item => item.role === selectedAccountType.value);
            }
            
            // Apply filière filter only for ETUDIANT accounts if a filière is selected
            if (selectedAccountType.value === 'ETUDIANT' && selectedFiliere.value !== 'all') {
                data = data.filter(item => item.filiereName && 
                    (typeof item.filiereName === 'string' ? 
                        item.filiereName === getSelectedFiliere() : 
                        item.filiereName.id === selectedFiliere.value));
            }
            break;
        case 'binomes':
            data = sectionData.value.binomes || [];
            
            // Apply filière filter if selected
            if (selectedFiliere.value !== 'all') {
                data = data.filter(item => {
                    const filiereName = typeof item.filiereName === 'string' ? 
                        item.filiereName : (item.filiereName?.nom || '');
                    return filiereName === getSelectedFiliere();
                });
            }
            break;
        case 'sujets':
            data = sectionData.value.sujets || [];
            
            // Apply filière filter if selected
            if (selectedFiliere.value !== 'all') {
                data = data.filter(item => {
                    const filiereName = typeof item.filiereName === 'string' ? 
                        item.filiereName : (item.filiereName?.nom || '');
                    return filiereName === getSelectedFiliere();
                });
            }
            break;
        case 'soutenances':
            data = sectionData.value.soutenances || [];
            
            // Apply filière filter if selected
            if (selectedFiliere.value !== 'all') {
                data = data.filter(item => {
                    const binome = typeof item.binome === 'string' ? 
                        tryParseJSON(item.binome) : item.binome;
                    const filiereName = binome?.filiereName || '';
                    return filiereName === getSelectedFiliere();
                });
            }
            break;
        case 'notes':
            data = sectionData.value.notes || [];
            
            // Apply filière filter if selected
            if (selectedFiliere.value !== 'all') {
                data = data.filter(item => {
                    const filiereName = typeof item.filiereName === 'string' ? 
                        item.filiereName : (item.filiereName?.nom || '');
                    return filiereName === getSelectedFiliere();
                });
            }
            break;
    }
    
    if (query && data.length > 0) {
        // Generic search across all string fields
        data = data.filter(item => {
            return JSON.stringify(item).toLowerCase().includes(query);
        });
    }
    
    return parseData(data);
});

// Parse any JSON string values into objects if needed
function parseData(data) {
    if (!data || !Array.isArray(data)) return [];
    
    return data.map(item => {
        const parsedItem = { ...item };
        
        // Parse fields that might contain JSON strings
        for (const key in parsedItem) {
            if (typeof parsedItem[key] === 'string' && 
                (parsedItem[key].startsWith('{') || parsedItem[key].startsWith('['))) {
                try {
                    parsedItem[key] = JSON.parse(parsedItem[key]);
                } catch (e) {
                    // Not valid JSON, keep as is
                }
            }
        }
        
        return parsedItem;
    });
}

// Get nested value from object using dot notation
function getNestedValue(obj, path) {
    if (!obj || !path) return '';
    
    const value = path.split('.').reduce((o, key) => o && o[key] !== undefined ? o[key] : null, obj);
    return value !== null && value !== undefined ? value : '';
}

function getColumns() {
    switch (selectedSection.value) {
        case 'comptes':
            return [
                { field: 'nom', header: 'Nom', sortable: true },
                { field: 'prenom', header: 'Prénom', sortable: true },
                { field: 'email', header: 'Email', sortable: true },
                { field: 'role', header: 'Rôle', sortable: true,
                  body: (data) => getRoleLabel(data.role) },
                { field: 'cni', header: 'CNI', 
                  body: (data) => data.role !== 'ETUDIANT' ? data.cni : '-' },
                { field: 'cne', header: 'CNE', 
                  body: (data) => data.role === 'ETUDIANT' ? data.cne : '-' },
                { field: 'filiereName', header: 'Filière', 
                  body: (data) => data.role === 'ETUDIANT' ? 
                    (typeof data.filiereName === 'string' ? data.filiereName : 
                     (data.filiereName?.nom || '-')) : '-' }
            ];
        
        case 'binomes':
            return [
                { field: 'etudiant1', header: 'Étudiant 1', 
                  body: (data) => {
                    const etudiant = typeof data.etudiant1 === 'string' ? 
                      tryParseJSON(data.etudiant1) : data.etudiant1;
                    if (etudiant) {
                        return `${etudiant.nom || ''} ${etudiant.prenom || ''}`.trim() || '-';
                    }
                    return '-';
                  }
                },
                { field: 'etudiant2', header: 'Étudiant 2', 
                  body: (data) => {
                    const etudiant = typeof data.etudiant2 === 'string' ? 
                      tryParseJSON(data.etudiant2) : data.etudiant2;
                    if (etudiant) {
                        return `${etudiant.nom || ''} ${etudiant.prenom || ''}`.trim() || '-';
                    }
                    return '-';
                  }
                },
                { field: 'encadrant', header: 'Encadrant', 
                  body: (data) => {
                    const encadrant = typeof data.encadrant === 'string' ? 
                      tryParseJSON(data.encadrant) : data.encadrant;
                    if (encadrant) {
                        return `${encadrant.nom || ''} ${encadrant.prenom || ''}`.trim() || '-';
                    }
                    return '-';
                  }
                },
                { field: 'sujet', header: 'Sujet', 
                  body: (data) => {
                    const sujet = typeof data.sujet === 'string' ? 
                      tryParseJSON(data.sujet) : data.sujet;
                    return sujet?.titre || '-';
                  }
                },
                { field: 'filiereName', header: 'Filière',
                  body: (data) => typeof data.filiereName === 'string' ? 
                    data.filiereName : (data.filiereName?.nom || '-') }
            ];
        
        case 'sujets':
            return [
                { field: 'titre', header: 'Titre', sortable: true },
                { field: 'theme', header: 'Thème', sortable: true },
                { field: 'description', header: 'Description', style: { maxWidth: '300px' } },
                { field: 'filiereName', header: 'Filière', sortable: true,
                  body: (data) => typeof data.filiereName === 'string' ? 
                    data.filiereName : (data.filiereName?.nom || '-') }
            ];
        
        case 'soutenances':
            return [
                { field: 'date', header: 'Date', sortable: true, 
                  body: (data) => data.date ? new Date(data.date).toLocaleDateString('fr-FR') : '-' 
                },
                { field: 'heure', header: 'Heure', sortable: true },
                { field: 'salle', header: 'Salle', 
                  body: (data) => {
                    const salle = typeof data.salle === 'string' ? 
                      tryParseJSON(data.salle) : data.salle;
                    return salle?.nom || '-';
                  }
                },
                { field: 'binome', header: 'Binôme', 
                  body: (data) => {
                    const binome = typeof data.binome === 'string' ? 
                      tryParseJSON(data.binome) : data.binome;
                    if (!binome) return '-';
                    
                    let result = '';
                    const etudiant1 = binome.etudiant1;
                    const etudiant2 = binome.etudiant2;
                    
                    if (etudiant1) {
                        result += `${etudiant1.nom || ''} ${etudiant1.prenom || ''}`.trim();
                    }
                    if (etudiant2) {
                        result += result ? ' / ' : '';
                        result += `${etudiant2.nom || ''} ${etudiant2.prenom || ''}`.trim();
                    }
                    return result || '-';
                  }
                },
                { field: 'jury', header: 'Jury', 
                  body: (data) => {
                    let result = '';
                    const jury1 = typeof data.jury1 === 'string' ? 
                      tryParseJSON(data.jury1) : data.jury1;
                    const jury2 = typeof data.jury2 === 'string' ? 
                      tryParseJSON(data.jury2) : data.jury2;
                    
                    if (jury1) {
                        result += `${jury1.nom || ''} ${jury1.prenom || ''}`.trim();
                    }
                    if (jury2) {
                        result += result ? ' / ' : '';
                        result += `${jury2.nom || ''} ${jury2.prenom || ''}`.trim();
                    }
                    return result || '-';
                  }
                },
                { field: 'filiere', header: 'Filière', 
                  body: (data) => {
                    const binome = typeof data.binome === 'string' ? 
                      tryParseJSON(data.binome) : data.binome;
                    return binome?.filiereName || '-';
                  }
                }
            ];
        
        case 'notes':
            return [
                { field: 'etudiant.nom', header: 'Nom', sortable: true, 
                  body: (data) => {
                    const etudiant = typeof data.etudiant === 'string' ? 
                      tryParseJSON(data.etudiant) : data.etudiant;
                    return etudiant?.nom || '-';
                  }
                },
                { field: 'etudiant.prenom', header: 'Prénom', sortable: true,
                  body: (data) => {
                    const etudiant = typeof data.etudiant === 'string' ? 
                      tryParseJSON(data.etudiant) : data.etudiant;
                    return etudiant?.prenom || '-';
                  }
                },
                { field: 'etudiant.cne', header: 'CNE',
                  body: (data) => {
                    const etudiant = typeof data.etudiant === 'string' ? 
                      tryParseJSON(data.etudiant) : data.etudiant;
                    return etudiant?.cne || '-';
                  }
                },
                { field: 'noteRapport', header: 'Note Rapport', sortable: true, 
                  body: (data) => formatGrade(data.noteRapport) 
                },
                { field: 'noteSoutenance', header: 'Note Soutenance', sortable: true, 
                  body: (data) => formatGrade(data.noteSoutenance) 
                },
                { field: 'noteEncadrant', header: 'Note Encadrant', sortable: true, 
                  body: (data) => formatGrade(data.noteEncadrant) 
                },
                { field: 'noteFinale', header: 'Note Finale', sortable: true, 
                  body: (data) => formatGrade(calculateFinalGrade(data)) 
                },
                { field: 'filiereName', header: 'Filière', sortable: true,
                  body: (data) => typeof data.filiereName === 'string' ? 
                    data.filiereName : (data.filiereName?.nom || '-') }
            ];
        
        default:
            return [];
    }
}

// Get simplified columns for printing - remove sortable and style props
function getPrintColumns() {
    const columns = getColumns();
    return columns.map(column => ({
        field: column.field,
        header: column.header,
        body: column.body
    }));
}

function getPaginatorTemplate() {
    const itemLabel = selectedSection.value === 'binomes' ? 'binômes' : selectedSection.value;
    return `Montrant {first} à {last} sur {totalRecords} ${itemLabel}`;
}

// Try to parse JSON, return null if parsing fails
function tryParseJSON(jsonString) {
    try {
        if (typeof jsonString === 'string') {
            return JSON.parse(jsonString);
        }
        return jsonString;
    } catch (e) {
        return null;
    }
}

// Find and return the latest academic year
function findLatestAcademicYear(years) {
    if (!years || years.length === 0) return null;
    
    // Sort years by ID assuming higher ID = newer year
    // Alternatively, sort by academic year string if in format "YYYY-YYYY"
    return years.sort((a, b) => {
        // Try to extract start year from format "YYYY-YYYY"
        const aYear = a.annee && a.annee.includes('-') ? 
            parseInt(a.annee.split('-')[0]) : 0;
        const bYear = b.annee && b.annee.includes('-') ? 
            parseInt(b.annee.split('-')[0]) : 0;
            
        // If years can be compared, use that, otherwise fall back to ID comparison
        if (aYear && bYear) {
            return bYear - aYear; // Descending order
        }
        
        // Fall back to ID comparison (assuming higher ID = newer)
        return b.id - a.id;
    })[0];
}

// Fetch metadata on mount
onMounted(async () => {
    initialLoading.value = true;
    await fetchArchiveMetadata();
    initialLoading.value = false;
});

// Methods
async function fetchArchiveMetadata() {
    loading.value = true;
    try {
        const response = await ApiService.get('/chef_de_departement/archive/metadata');
        academicYears.value = response.academicYears;
        filieres.value = response.filieres;
        
        // Select the latest academic year by default
        if (academicYears.value.length > 0) {
            const latestYear = findLatestAcademicYear(academicYears.value);
            if (latestYear) {
                selectedYear.value = latestYear.id;
                
                // Set default section
                selectedSection.value = DEFAULT_SECTION;
                
                // Load data for the selected year and section
                await loadSectionData();
            }
        }
    } catch (error) {
        handleApiError(error, 'Erreur lors du chargement des métadonnées');
    } finally {
        loading.value = false;
    }
}

async function onYearChange() {
    // If no section is selected, use default
    if (!selectedSection.value) {
        selectedSection.value = DEFAULT_SECTION;
    }
    
    selectedFiliere.value = 'all';
    sectionData.value = {};
    
    // Load data for the selected year and section
    await loadSectionData();
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
                    `/chef_de_departement/archive/comptes/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { comptes: comptes.comptes || [] };
                break;
            
            case 'binomes':
                const binomes = await ApiService.get(
                    `/chef_de_departement/archive/binomes/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { binomes: binomes.binomes || [] };
                break;
            
            case 'sujets':
                const sujets = await ApiService.get(
                    `/chef_de_departement/archive/sujets/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { sujets: sujets.sujets || [] };
                break;
            
            case 'soutenances':
                const soutenances = await ApiService.get(
                    `/chef_de_departement/archive/soutenances/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { soutenances: soutenances || [] };
                break;
            
            case 'notes':
                const notes = await ApiService.get(
                    `/chef_de_departement/archive/notes/${filiereParam}/${selectedYear.value}`
                );
                sectionData.value = { 
                    notes: notes.notes || [],
                    pourcentages: notes.pourcentages || {
                        pourcentageRapport: 40,
                        pourcentageSoutenance: 40,
                        pourcentageEncadrant: 20
                    }
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
    return grade !== null && grade !== undefined ? parseFloat(grade).toFixed(2) : '-';
}

function calculateFinalGrade(note) {
    if (!sectionData.value.pourcentages || !canShowFinalGrade(note)) return null;
    
    const { pourcentageRapport, pourcentageSoutenance, pourcentageEncadrant } = sectionData.value.pourcentages;
    
    const noteRapport = parseFloat(note.noteRapport || 0);
    const noteSoutenance = parseFloat(note.noteSoutenance || 0);
    const noteEncadrant = parseFloat(note.noteEncadrant || 0);
    
    return (
        noteRapport * (pourcentageRapport / 100) +
        noteSoutenance * (pourcentageSoutenance / 100) +
        noteEncadrant * (pourcentageEncadrant / 100)
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

.loading-spinner {
    display: flex;
    justify-content: center;
    margin: 2rem auto;
}

.archive-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
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
    border-color: var(--primary-color);
}

.filiere-filter , .account-type-filter {
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