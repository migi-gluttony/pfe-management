<template>
    <div class="settings-container">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader title="Paramètres" />

        <div class="settings-layout">
            <!-- Settings Sidebar -->
            <div class="settings-sidebar">
                <div class="sidebar-title">Paramètres</div>
                
                <div class="nav-items">
                    <div 
                        class="nav-item" 
                        :class="{ active: activeSection === 'pourcentages' }"
                        @click="activeSection = 'pourcentages'"
                    >
                        <i class="pi pi-percentage"></i>
                        <span>Gestion des Pourcentages</span>
                    </div>
                    
                    <div 
                        class="nav-item" 
                        :class="{ active: activeSection === 'archivage' }"
                        @click="activeSection = 'archivage'"
                    >
                        <i class="pi pi-calendar"></i>
                        <span>Gestion des Années</span>
                    </div>
                </div>
            </div>

            <!-- Settings Content -->
            <div class="settings-content">
                <!-- Pourcentages Section -->
                <div v-show="activeSection === 'pourcentages'">
                    <h1>Gestion des Pourcentages</h1>

                    <!-- Global Percentages Card -->
                    <Card class="settings-card">
                        <template #title>Pourcentages Principaux</template>
                        <template #content>
                            <div v-if="loading" class="loader-container">
                                <ProgressSpinner />
                            </div>
                            <div v-else>
                                <p class="card-description">
                                    Configurez les pourcentages pour le calcul des notes finales (total = 100%)
                                </p>

                                <div class="form-group">
                                    <label for="rapport">Rapport</label>
                                    <InputNumber 
                                        id="rapport" 
                                        v-model="pourcentages.pourcentageRapport" 
                                        :min="0" 
                                        :max="100"
                                        showButtons
                                        class="w-full"
                                    />
                                </div>

                                <div class="form-group">
                                    <label for="soutenance">Soutenance</label>
                                    <InputNumber 
                                        id="soutenance" 
                                        v-model="pourcentages.pourcentageSoutenance" 
                                        :min="0" 
                                        :max="100"
                                        showButtons
                                        class="w-full"
                                    />
                                </div>

                                <div class="form-group">
                                    <label for="encadrant">Encadrant</label>
                                    <InputNumber 
                                        id="encadrant" 
                                        v-model="pourcentages.pourcentageEncadrant" 
                                        :min="0" 
                                        :max="100"
                                        showButtons
                                        class="w-full"
                                    />
                                </div>

                                <div class="total-display" :class="{ 'total-valid': calculateTotal() === 100, 'total-invalid': calculateTotal() !== 100 }">
                                    Total: {{ calculateTotal() }}%
                                </div>

                                <Button 
                                    label="Enregistrer" 
                                    icon="pi pi-save" 
                                    class="save-button"
                                    :disabled="calculateTotal() !== 100 || savingPercentages"
                                    :loading="savingPercentages"
                                    @click="savePercentages"
                                />
                            </div>
                        </template>
                    </Card>

                    <!-- Detailed Percentages Card -->
                    <Card class="settings-card">
                        <template #title>Détail des Pourcentages</template>
                        <template #content>
                            <div v-if="loading" class="loader-container">
                                <ProgressSpinner />
                            </div>
                            <div v-else>
                                <!-- Tabbed view for detailed percentages -->
                                <TabView>
                                    <!-- Encadrant Tab -->
                                    <TabPanel header="Encadrant">
                                        <div class="form-group">
                                            <label>Compétences techniques</label>
                                            <InputNumber 
                                                v-model="pourcentagesEncadrant.pourcentageTechnical" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Qualité du rapport</label>
                                            <InputNumber 
                                                v-model="pourcentagesEncadrant.pourcentageReport" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Progression du travail</label>
                                            <InputNumber 
                                                v-model="pourcentagesEncadrant.pourcentageProgress" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Professionnalisme</label>
                                            <InputNumber 
                                                v-model="pourcentagesEncadrant.pourcentageProfessionalism" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="total-display" :class="{ 'total-valid': calculateTotalEncadrant() === 100, 'total-invalid': calculateTotalEncadrant() !== 100 }">
                                            Total: {{ calculateTotalEncadrant() }}%
                                        </div>

                                        <Button 
                                            label="Enregistrer" 
                                            icon="pi pi-save" 
                                            class="save-button"
                                            :disabled="calculateTotalEncadrant() !== 100 || savingEncadrantPercentages"
                                            :loading="savingEncadrantPercentages"
                                            @click="saveEncadrantPercentages"
                                        />
                                    </TabPanel>

                                    <!-- Rapport Tab -->
                                    <TabPanel header="Rapport">
                                        <div class="form-group">
                                            <label>Contenu technique</label>
                                            <InputNumber 
                                                v-model="pourcentagesRapport.pourcentageTechnical" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Structure et organisation</label>
                                            <InputNumber 
                                                v-model="pourcentagesRapport.pourcentageStructure" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Originalité</label>
                                            <InputNumber 
                                                v-model="pourcentagesRapport.pourcentageOriginality" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="total-display" :class="{ 'total-valid': calculateTotalRapport() === 100, 'total-invalid': calculateTotalRapport() !== 100 }">
                                            Total: {{ calculateTotalRapport() }}%
                                        </div>

                                        <Button 
                                            label="Enregistrer" 
                                            icon="pi pi-save" 
                                            class="save-button"
                                            :disabled="calculateTotalRapport() !== 100 || savingRapportPercentages"
                                            :loading="savingRapportPercentages"
                                            @click="saveRapportPercentages"
                                        />
                                    </TabPanel>

                                    <!-- Soutenance Tab -->
                                    <TabPanel header="Soutenance">
                                        <div class="form-group">
                                            <label>Qualité de présentation</label>
                                            <InputNumber 
                                                v-model="pourcentagesSoutenance.pourcentagePresentation" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Réponses aux questions</label>
                                            <InputNumber 
                                                v-model="pourcentagesSoutenance.pourcentageQa" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="form-group">
                                            <label>Gestion du temps</label>
                                            <InputNumber 
                                                v-model="pourcentagesSoutenance.pourcentageTimeManagement" 
                                                :min="0" 
                                                :max="100"
                                                showButtons
                                                class="w-full"
                                            />
                                        </div>

                                        <div class="total-display" :class="{ 'total-valid': calculateTotalSoutenance() === 100, 'total-invalid': calculateTotalSoutenance() !== 100 }">
                                            Total: {{ calculateTotalSoutenance() }}%
                                        </div>

                                        <Button 
                                            label="Enregistrer" 
                                            icon="pi pi-save" 
                                            class="save-button"
                                            :disabled="calculateTotalSoutenance() !== 100 || savingSoutenancePercentages"
                                            :loading="savingSoutenancePercentages"
                                            @click="saveSoutenancePercentages"
                                        />
                                    </TabPanel>
                                </TabView>
                            </div>
                        </template>
                    </Card>
                </div>

                <!-- Archivage Section -->
                <div v-show="activeSection === 'archivage'">
                    <h1>Gestion des Années Académiques</h1>
                    
                    <Card class="settings-card">
                        <template #title>Année Académique</template>
                        <template #content>
                            <div v-if="loading" class="loader-container">
                                <ProgressSpinner />
                            </div>
                            <div v-else>
                                <div class="current-year">
                                    <label>Année académique actuelle</label>
                                    <div class="year-badge">{{ currentYear.annee }}</div>
                                </div>

                                <Divider />

                                <div class="archive-section">
                                    <p>
                                        Archiver l'année actuelle pour créer une nouvelle année 
                                        ({{ getNextAcademicYear(currentYear.annee) }})
                                    </p>
                                    
                                    <Button 
                                        label="Archiver" 
                                        icon="pi pi-box" 
                                        class="archive-button p-button-warning"
                                        @click="confirmArchive"
                                        :loading="archiving"
                                    />
                                </div>

                                <Divider />

                                <h3>Historique des années</h3>
                                <DataTable 
                                    :value="academicYears" 
                                    class="years-table"
                                    :paginator="academicYears.length > 5"
                                    :rows="5"
                                    stripedRows
                                    responsiveLayout="scroll"
                                >
                                    <Column field="annee" header="Année"></Column>
                                    <Column header="Statut">
                                        <template #body="slotProps">
                                            <Tag 
                                                :severity="slotProps.data.courante ? 'success' : 'info'"
                                            >
                                                {{ slotProps.data.courante ? "Active" : "Archivée" }}
                                            </Tag>
                                        </template>
                                    </Column>
                                </DataTable>
                            </div>
                        </template>
                    </Card>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// Import components
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import ProgressSpinner from "primevue/progressspinner";
import Card from "primevue/card";
import InputNumber from "primevue/inputnumber";
import Button from "primevue/button";
import Divider from "primevue/divider";
import Tag from "primevue/tag";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';

// Services
const toast = useToast();
const confirm = useConfirm();

// Component state
const loading = ref(true);
const savingPercentages = ref(false);
const savingEncadrantPercentages = ref(false);
const savingRapportPercentages = ref(false);
const savingSoutenancePercentages = ref(false);
const archiving = ref(false);
const activeSection = ref('pourcentages'); // Default active section

// Overall percentages data
const pourcentages = ref({
    pourcentageRapport: 40,
    pourcentageSoutenance: 40,
    pourcentageEncadrant: 20,
});

// Detailed encadrant percentages
const pourcentagesEncadrant = ref({
    pourcentageTechnical: 40,
    pourcentageReport: 20,
    pourcentageProgress: 20,
    pourcentageProfessionalism: 20,
});

// Detailed rapport percentages
const pourcentagesRapport = ref({
    pourcentageTechnical: 50,
    pourcentageStructure: 30,
    pourcentageOriginality: 20,
});

// Detailed soutenance percentages
const pourcentagesSoutenance = ref({
    pourcentagePresentation: 50,
    pourcentageQa: 40,
    pourcentageTimeManagement: 10,
});

const currentYear = ref({
    id: null,
    annee: "",
    courante: true,
});

const academicYears = ref([]);

// Calculate totals for each percentage category
const calculateTotal = () => {
    return (
        (pourcentages.value.pourcentageRapport || 0) +
        (pourcentages.value.pourcentageSoutenance || 0) +
        (pourcentages.value.pourcentageEncadrant || 0)
    );
};

const calculateTotalEncadrant = () => {
    return (
        (pourcentagesEncadrant.value.pourcentageTechnical || 0) +
        (pourcentagesEncadrant.value.pourcentageReport || 0) +
        (pourcentagesEncadrant.value.pourcentageProgress || 0) +
        (pourcentagesEncadrant.value.pourcentageProfessionalism || 0)
    );
};

const calculateTotalRapport = () => {
    return (
        (pourcentagesRapport.value.pourcentageTechnical || 0) +
        (pourcentagesRapport.value.pourcentageStructure || 0) +
        (pourcentagesRapport.value.pourcentageOriginality || 0)
    );
};

const calculateTotalSoutenance = () => {
    return (
        (pourcentagesSoutenance.value.pourcentagePresentation || 0) +
        (pourcentagesSoutenance.value.pourcentageQa || 0) +
        (pourcentagesSoutenance.value.pourcentageTimeManagement || 0)
    );
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
        const response = await ApiService.get("/chef_de_departement/settings");

        // Set overall percentages
        if (response.pourcentages) {
            pourcentages.value = {
                pourcentageRapport: response.pourcentages.pourcentageRapport,
                pourcentageSoutenance:
                    response.pourcentages.pourcentageSoutenance,
                pourcentageEncadrant:
                    response.pourcentages.pourcentageEncadrant,
            };
        }

        // Set encadrant percentages
        if (response.pourcentagesEncadrant) {
            pourcentagesEncadrant.value = {
                pourcentageTechnical:
                    response.pourcentagesEncadrant.pourcentageTechnical,
                pourcentageReport:
                    response.pourcentagesEncadrant.pourcentageReport,
                pourcentageProgress:
                    response.pourcentagesEncadrant.pourcentageProgress,
                pourcentageProfessionalism:
                    response.pourcentagesEncadrant.pourcentageProfessionalism,
            };
        }

        // Set rapport percentages
        if (response.pourcentagesRapport) {
            pourcentagesRapport.value = {
                pourcentageTechnical:
                    response.pourcentagesRapport.pourcentageTechnical,
                pourcentageStructure:
                    response.pourcentagesRapport.pourcentageStructure,
                pourcentageOriginality:
                    response.pourcentagesRapport.pourcentageOriginality,
            };
        }

        // Set soutenance percentages
        if (response.pourcentagesSoutenance) {
            pourcentagesSoutenance.value = {
                pourcentagePresentation:
                    response.pourcentagesSoutenance.pourcentagePresentation,
                pourcentageQa: response.pourcentagesSoutenance.pourcentageQa,
                pourcentageTimeManagement:
                    response.pourcentagesSoutenance.pourcentageTimeManagement,
            };
        }

        // Set current year
        if (response.currentYear) {
            currentYear.value = response.currentYear;
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des paramètres");
    } finally {
        loading.value = false;
    }
}

async function fetchAcademicYears() {
    try {
        const response = await ApiService.get(
            "/chef_de_departement/settings/academic-years"
        );
        if (response && response.years) {
            academicYears.value = response.years;
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des années scolaires");
    }
}

// Calculate next academic year based on current year format (YYYY-YYYY)
function getNextAcademicYear(currentYearStr) {
    if (!currentYearStr || !currentYearStr.includes("-")) return "";

    const yearParts = currentYearStr.split("-");
    if (yearParts.length !== 2) return "";

    try {
        const startYear = parseInt(yearParts[0]);
        const endYear = parseInt(yearParts[1]);
        return `${startYear + 1}-${endYear + 1}`;
    } catch (e) {
        return "";
    }
}

async function savePercentages() {
    // Validate total is 100%
    if (calculateTotal() !== 100) {
        toast.add({
            severity: "warn",
            summary: "Validation",
            detail: "La somme des pourcentages doit être égale à 100%",
            life: 5000,
        });
        return;
    }

    savingPercentages.value = true;
    try {
        await ApiService.put(
            "/chef_de_departement/settings/percentages",
            pourcentages.value
        );

        toast.add({
            severity: "success",
            summary: "Succès",
            detail: "Pourcentages enregistrés",
            life: 3000,
        });
    } catch (error) {
        handleApiError(error, "Erreur lors de la sauvegarde des pourcentages");
    } finally {
        savingPercentages.value = false;
    }
}

async function saveEncadrantPercentages() {
    // Validate total is 100%
    if (calculateTotalEncadrant() !== 100) {
        toast.add({
            severity: "warn",
            summary: "Validation",
            detail: "La somme des pourcentages doit être égale à 100%",
            life: 5000,
        });
        return;
    }

    savingEncadrantPercentages.value = true;
    try {
        await ApiService.put(
            "/chef_de_departement/settings/percentages/encadrant",
            pourcentagesEncadrant.value
        );

        toast.add({
            severity: "success",
            summary: "Succès",
            detail: "Pourcentages encadrant enregistrés",
            life: 3000,
        });
    } catch (error) {
        handleApiError(
            error,
            "Erreur lors de la sauvegarde des pourcentages Encadrant"
        );
    } finally {
        savingEncadrantPercentages.value = false;
    }
}

async function saveRapportPercentages() {
    // Validate total is 100%
    if (calculateTotalRapport() !== 100) {
        toast.add({
            severity: "warn",
            summary: "Validation",
            detail: "La somme des pourcentages doit être égale à 100%",
            life: 5000,
        });
        return;
    }

    savingRapportPercentages.value = true;
    try {
        await ApiService.put(
            "/chef_de_departement/settings/percentages/rapport",
            pourcentagesRapport.value
        );

        toast.add({
            severity: "success",
            summary: "Succès",
            detail: "Pourcentages rapport enregistrés",
            life: 3000,
        });
    } catch (error) {
        handleApiError(
            error,
            "Erreur lors de la sauvegarde des pourcentages Rapport"
        );
    } finally {
        savingRapportPercentages.value = false;
    }
}

async function saveSoutenancePercentages() {
    // Validate total is 100%
    if (calculateTotalSoutenance() !== 100) {
        toast.add({
            severity: "warn",
            summary: "Validation",
            detail: "La somme des pourcentages doit être égale à 100%",
            life: 5000,
        });
        return;
    }

    savingSoutenancePercentages.value = true;
    try {
        await ApiService.put(
            "/chef_de_departement/settings/percentages/soutenance",
            pourcentagesSoutenance.value
        );

        toast.add({
            severity: "success",
            summary: "Succès",
            detail: "Pourcentages soutenance enregistrés",
            life: 3000,
        });
    } catch (error) {
        handleApiError(
            error,
            "Erreur lors de la sauvegarde des pourcentages Soutenance"
        );
    } finally {
        savingSoutenancePercentages.value = false;
    }
}

function confirmArchive() {
    const nextYear = getNextAcademicYear(currentYear.value.annee);

    confirm.require({
        message: `Archiver l'année ${currentYear.value.annee} et créer ${nextYear}?`,
        header: "Confirmation",
        icon: "pi pi-exclamation-triangle",
        acceptClass: "p-button-danger",
        accept: () => archiveCurrentYear(),
        reject: () => {},
    });
}

async function archiveCurrentYear() {
    archiving.value = true;
    try {
        const response = await ApiService.post(
            "/chef_de_departement/settings/academic-years/archive"
        );

        toast.add({
            severity: "success",
            summary: "Succès",
            detail: `Année ${currentYear.value.annee} archivée et ${response.annee} créée`,
            life: 5000,
        });

        // Refresh data
        await fetchSettings();
        await fetchAcademicYears();
    } catch (error) {
        handleApiError(error, "Erreur lors de l'archivage de l'année scolaire");
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
        severity: "error",
        summary: "Erreur",
        detail: errorMessage,
        life: 5000,
    });
}
</script>

<style scoped>
.settings-container {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

.page-header {
    margin-bottom: 2rem;
}

.page-title {
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
    margin: 0 0 0.5rem 0;
}

.page-subtitle {
    color: var(--text-color-secondary);
    font-size: 1.1rem;
    margin: 0;
}

.settings-layout {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.settings-card {
    margin-bottom: 0;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    transition: transform 0.2s, box-shadow 0.2s;
}

.settings-card:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
    padding: 1rem;
    background-color: rgba(var(--primary-color-rgb), 0.05);
    border-bottom: 1px solid rgba(var(--primary-color-rgb), 0.1);
}

.dark-mode .card-header {
    background-color: rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.card-title {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--primary-color);
}

.card-title i {
    font-size: 1.25rem;
}

/* Main percentages styling */
.main-percentages {
    grid-column: span 2;
}

.slider-container {
    height: 8px;
    background-color: var(--surface-200);
    border-radius: 4px;
    margin: 8px 0;
    overflow: hidden;
}

.progress-bar {
    height: 100%;
    transition: width 0.3s ease;
}

.field-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
}

.field-header label {
    font-weight: 600;
    color: var(--text-color);
}

.percentage-value {
    font-weight: 600;
    color: var(--primary-color);
}

.percentage-distribution {
    display: flex;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 1.5rem;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.distribution-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    padding: 12px 0;
    flex: 1;
    text-align: center;
}

.distribution-item .value {
    font-size: 1.2rem;
    font-weight: bold;
    margin-bottom: 4px;
}

.distribution-item .label {
    font-size: 0.8rem;
    opacity: 0.9;
}

/* Percentage validation */
.percentages-total {
    margin: 1.5rem 0;
    padding: 0.75rem;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.total-valid {
    background-color: rgba(var(--green-500-rgb), 0.1);
    border: 1px solid var(--green-500);
}

.total-invalid {
    background-color: rgba(var(--red-500-rgb), 0.1);
    border: 1px solid var(--red-500);
}

.total-indicator {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1rem;
}

.total-valid .total-indicator {
    color: var(--green-600);
}

.total-invalid .total-indicator {
    color: var(--red-600);
}

.total-indicator i {
    font-size: 1.25rem;
}

.validation-message {
    font-size: 0.85rem;
    color: var(--red-600);
    margin-left: 1.75rem;
}

.save-button {
    width: 100%;
    margin-top: 1rem;
}

/* Year card styling */
.year-card {
    background: linear-gradient(to right bottom, var(--surface-0), var(--surface-50));
}

.year-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
}

.current-year-tag {
    font-size: 1.25rem;
    padding: 0.5rem 1rem;
}

.archive-info {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    background-color: rgba(var(--primary-color-rgb), 0.05);
    padding: 1rem;
    border-radius: 6px;
    margin-bottom: 1rem;
}

.archive-info .info-icon {
    color: var(--primary-color);
    font-size: 1.25rem;
    margin-top: 0.125rem;
}

.archive-info p {
    margin: 0;
    font-size: 0.95rem;
    line-height: 1.5;
}

.archive-button {
    margin-top: 0.5rem;
}

.years-table {
    margin-top: 1rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.year-label {
    font-weight: 600;
}

.year-status-tag {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
}

/* Loader styles */
.loader-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    gap: 1rem;
}

.loader-container p {
    color: var(--text-color-secondary);
}

/* Sections and headers */
h3 {
    font-size: 1.1rem;
    font-weight: 600;
    margin: 0 0 0.75rem 0;
    color: var(--text-color);
}

.p-field {
    margin-bottom: 1.25rem;
}

.percentage-content, .year-content {
    padding: 1rem;
}

.current-year-info, .archive-section, .years-history {
    margin-bottom: 1.5rem;
}

.requirement-note {
    color: var(--text-color-secondary);
    font-size: 0.9rem;
    font-style: italic;
}

/* Responsive tweaks */
@media (max-width: 1200px) {
    .settings-layout {
        grid-template-columns: 1fr;
    }
    
    .main-percentages {
        grid-column: span 1;
    }
}

@media (max-width: 768px) {
    .percentage-distribution {
        flex-wrap: wrap;
    }
    
    .distribution-item {
        min-width: 50%;
        padding: 16px 0;
    }
    
    .year-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }
}

@media (max-width: 480px) {
    .distribution-item {
        min-width: 100%;
    }
}
</style>
<style scoped>
.settings-container {
    margin: 0 auto;
    padding: 1rem;
}

.settings-layout {
    display: flex;
    gap: 1.5rem;
    margin-top: 1.5rem;
}

/* Sidebar styles */
.settings-sidebar {
    width: 20%;
    background-color: var(--surface-card);
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    align-self: flex-start;
    position: sticky;
    top: 1.5rem;
}

.sidebar-title {
    font-size: 1.2rem;
    font-weight: 600;
    padding: 1rem;
    border-bottom: 1px solid var(--surface-border);
    color: var(--primary-color);
}

.nav-items {
    padding: 0.5rem 0;
}

.nav-item {
    display: flex;
    align-items: center;
    padding: 0.75rem 1rem;
    cursor: pointer;
    transition: all 0.2s;
    border-radius: 0;
}

.nav-item:hover {
    background-color: rgba(var(--primary-color-rgb), 0.05);
}

.nav-item.active {
    background-color: rgba(var(--primary-color-rgb), 0.1);
    border-left: 3px solid var(--primary-color);
    color: var(--primary-color);
    font-weight: 500;
}

.nav-item i {
    margin-right: 0.75rem;
    font-size: 1.1rem;
}

/* Content styles */
.settings-content {
    flex: 1;
    min-width: 0; /* Prevents content from overflowing the flex container */
}

.settings-content h1 {
    font-size: 1.6rem;
    color: var(--primary-color);
    margin: 0 0 1.25rem 0;
}

.settings-card {
    margin-bottom: 1.5rem;
    border-radius: 6px;
}

.card-description {
    color: var(--text-color-secondary);
    margin-bottom: 1.5rem;
}

/* Form styling */
.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    font-weight: 500;
    margin-bottom: 0.5rem;
}

/* Total display */
.total-display {
    padding: 0.75rem;
    border-radius: 4px;
    font-weight: 600;
    margin: 1rem 0;
    text-align: center;
}

.total-valid {
    background-color: rgba(var(--green-500-rgb), 0.1);
    color: var(--green-600);
}

.total-invalid {
    background-color: rgba(var(--red-500-rgb), 0.1);
    color: var(--red-600);
}

/* Buttons */
.save-button {
    width: 100%;
}

/* Year section */
.current-year {
    margin-bottom: 1rem;
}

.current-year label {
    display: block;
    font-weight: 500;
    margin-bottom: 0.5rem;
}

.year-badge {
    background-color: var(--green-500);
    color: white;
    font-weight: 600;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    display: inline-block;
}

.archive-section {
    margin: 1rem 0;
}

.archive-section p {
    margin-bottom: 1rem;
}

.archive-button {
    width: 100%;
}

.years-table {
    margin-top: 1rem;
}

h3 {
    font-size: 1.1rem;
    margin-top: 1rem;
    margin-bottom: 0.75rem;
}

/* Loading state */
.loader-container {
    display: flex;
    justify-content: center;
    padding: 2rem;
}

/* Responsive layout */
@media (max-width: 768px) {
    .settings-layout {
        flex-direction: column;
    }
    
    .settings-sidebar {
        width: 100%;
        position: static;
    }
    
    .nav-item {
        padding: 1rem;
    }
}
</style>