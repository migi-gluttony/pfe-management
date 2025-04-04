<template>
    <div class="settings-container">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader title="Paramètres de l'application" />

        <div class="settings-layout">
            <!-- Overall Percentages Card -->
            <Card class="settings-card">
                <template #title>
                    <div class="card-title">
                        <i class="pi pi-percentage mr-2"></i>
                        <span>Pourcentages principaux des notes</span>
                    </div>
                </template>
                <template #content>
                    <div v-if="loading" class="p-d-flex p-jc-center">
                        <ProgressSpinner style="width: 50px; height: 50px" />
                    </div>
                    <div v-else class="percentage-content">
                        <div class="percentage-description mb-4">
                            <p>
                                Configurez les pourcentages principaux de calcul
                                pour les notes finales.
                                <br />La somme des pourcentages doit être égale
                                à 100%.
                            </p>
                        </div>

                        <div class="percentage-form">
                            <div class="p-field mb-3">
                                <label for="rapport"
                                    >Pourcentage Rapport (%)</label
                                >
                                <InputNumber
                                    id="rapport"
                                    v-model="pourcentages.pourcentageRapport"
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="soutenance"
                                    >Pourcentage Soutenance (%)</label
                                >
                                <InputNumber
                                    id="soutenance"
                                    v-model="pourcentages.pourcentageSoutenance"
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="encadrant"
                                    >Pourcentage Encadrant (%)</label
                                >
                                <InputNumber
                                    id="encadrant"
                                    v-model="pourcentages.pourcentageEncadrant"
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="percentages-total mt-4">
                                <Tag severity="info" class="p-mr-2">
                                    <b>Total: {{ calculateTotal() }}%</b>
                                    <i
                                        class="pi ml-2"
                                        :class="
                                            calculateTotal() === 100
                                                ? 'pi-check text-green-500'
                                                : 'pi-times text-red-500'
                                        "
                                    ></i>
                                </Tag>
                            </div>

                            <Button
                                label="Sauvegarder les pourcentages"
                                icon="pi pi-save"
                                class="p-button-primary mt-4 w-full"
                                :disabled="
                                    calculateTotal() !== 100 ||
                                    savingPercentages
                                "
                                :loading="savingPercentages"
                                @click="savePercentages"
                            />
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Encadrant Percentages Card -->
            <Card class="settings-card">
                <template #title>
                    <div class="card-title">
                        <i class="pi pi-user mr-2"></i>
                        <span>Détail des pourcentages Encadrant</span>
                    </div>
                </template>
                <template #content>
                    <div v-if="loading" class="p-d-flex p-jc-center">
                        <ProgressSpinner style="width: 50px; height: 50px" />
                    </div>
                    <div v-else class="percentage-content">
                        <div class="percentage-description mb-4">
                            <p>
                                Configurez les pourcentages détaillés pour
                                l'évaluation par l'encadrant.
                                <br />La somme des pourcentages doit être égale
                                à 100%.
                            </p>
                        </div>

                        <div class="percentage-form">
                            <div class="p-field mb-3">
                                <label for="encadrant-technical"
                                    >Compétences techniques (%)</label
                                >
                                <InputNumber
                                    id="encadrant-technical"
                                    v-model="
                                        pourcentagesEncadrant.pourcentageTechnical
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="encadrant-report"
                                    >Qualité du rapport (%)</label
                                >
                                <InputNumber
                                    id="encadrant-report"
                                    v-model="
                                        pourcentagesEncadrant.pourcentageReport
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="encadrant-progress"
                                    >Progression du travail (%)</label
                                >
                                <InputNumber
                                    id="encadrant-progress"
                                    v-model="
                                        pourcentagesEncadrant.pourcentageProgress
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="encadrant-professionalism"
                                    >Professionnalisme (%)</label
                                >
                                <InputNumber
                                    id="encadrant-professionalism"
                                    v-model="
                                        pourcentagesEncadrant.pourcentageProfessionalism
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="percentages-total mt-4">
                                <Tag severity="info" class="p-mr-2">
                                    <b
                                        >Total:
                                        {{ calculateTotalEncadrant() }}%</b
                                    >
                                    <i
                                        class="pi ml-2"
                                        :class="
                                            calculateTotalEncadrant() === 100
                                                ? 'pi-check text-green-500'
                                                : 'pi-times text-red-500'
                                        "
                                    ></i>
                                </Tag>
                            </div>

                            <Button
                                label="Sauvegarder les pourcentages"
                                icon="pi pi-save"
                                class="p-button-primary mt-4 w-full"
                                :disabled="
                                    calculateTotalEncadrant() !== 100 ||
                                    savingEncadrantPercentages
                                "
                                :loading="savingEncadrantPercentages"
                                @click="saveEncadrantPercentages"
                            />
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Rapport Percentages Card -->
            <Card class="settings-card">
                <template #title>
                    <div class="card-title">
                        <i class="pi pi-file mr-2"></i>
                        <span>Détail des pourcentages Rapport</span>
                    </div>
                </template>
                <template #content>
                    <div v-if="loading" class="p-d-flex p-jc-center">
                        <ProgressSpinner style="width: 50px; height: 50px" />
                    </div>
                    <div v-else class="percentage-content">
                        <div class="percentage-description mb-4">
                            <p>
                                Configurez les pourcentages détaillés pour
                                l'évaluation du rapport.
                                <br />La somme des pourcentages doit être égale
                                à 100%.
                            </p>
                        </div>

                        <div class="percentage-form">
                            <div class="p-field mb-3">
                                <label for="rapport-technical"
                                    >Contenu technique (%)</label
                                >
                                <InputNumber
                                    id="rapport-technical"
                                    v-model="
                                        pourcentagesRapport.pourcentageTechnical
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="rapport-structure"
                                    >Structure et organisation (%)</label
                                >
                                <InputNumber
                                    id="rapport-structure"
                                    v-model="
                                        pourcentagesRapport.pourcentageStructure
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="rapport-originality"
                                    >Originalité (%)</label
                                >
                                <InputNumber
                                    id="rapport-originality"
                                    v-model="
                                        pourcentagesRapport.pourcentageOriginality
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="percentages-total mt-4">
                                <Tag severity="info" class="p-mr-2">
                                    <b>Total: {{ calculateTotalRapport() }}%</b>
                                    <i
                                        class="pi ml-2"
                                        :class="
                                            calculateTotalRapport() === 100
                                                ? 'pi-check text-green-500'
                                                : 'pi-times text-red-500'
                                        "
                                    ></i>
                                </Tag>
                            </div>

                            <Button
                                label="Sauvegarder les pourcentages"
                                icon="pi pi-save"
                                class="p-button-primary mt-4 w-full"
                                :disabled="
                                    calculateTotalRapport() !== 100 ||
                                    savingRapportPercentages
                                "
                                :loading="savingRapportPercentages"
                                @click="saveRapportPercentages"
                            />
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Soutenance Percentages Card -->
            <Card class="settings-card">
                <template #title>
                    <div class="card-title">
                        <i class="pi pi-desktop mr-2"></i>
                        <span>Détail des pourcentages Soutenance</span>
                    </div>
                </template>
                <template #content>
                    <div v-if="loading" class="p-d-flex p-jc-center">
                        <ProgressSpinner style="width: 50px; height: 50px" />
                    </div>
                    <div v-else class="percentage-content">
                        <div class="percentage-description mb-4">
                            <p>
                                Configurez les pourcentages détaillés pour
                                l'évaluation de la soutenance.
                                <br />La somme des pourcentages doit être égale
                                à 100%.
                            </p>
                        </div>

                        <div class="percentage-form">
                            <div class="p-field mb-3">
                                <label for="soutenance-presentation"
                                    >Qualité de présentation (%)</label
                                >
                                <InputNumber
                                    id="soutenance-presentation"
                                    v-model="
                                        pourcentagesSoutenance.pourcentagePresentation
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="soutenance-qa"
                                    >Réponses aux questions (%)</label
                                >
                                <InputNumber
                                    id="soutenance-qa"
                                    v-model="
                                        pourcentagesSoutenance.pourcentageQa
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="p-field mb-3">
                                <label for="soutenance-timemgmt"
                                    >Gestion du temps (%)</label
                                >
                                <InputNumber
                                    id="soutenance-timemgmt"
                                    v-model="
                                        pourcentagesSoutenance.pourcentageTimeManagement
                                    "
                                    :min="0"
                                    :max="100"
                                    showButtons
                                    buttonLayout="horizontal"
                                    decrementButtonClass="p-button-danger"
                                    incrementButtonClass="p-button-success"
                                    incrementButtonIcon="pi pi-plus"
                                    decrementButtonIcon="pi pi-minus"
                                    class="w-full"
                                />
                            </div>

                            <div class="percentages-total mt-4">
                                <Tag severity="info" class="p-mr-2">
                                    <b
                                        >Total:
                                        {{ calculateTotalSoutenance() }}%</b
                                    >
                                    <i
                                        class="pi ml-2"
                                        :class="
                                            calculateTotalSoutenance() === 100
                                                ? 'pi-check text-green-500'
                                                : 'pi-times text-red-500'
                                        "
                                    ></i>
                                </Tag>
                            </div>

                            <Button
                                label="Sauvegarder les pourcentages"
                                icon="pi pi-save"
                                class="p-button-primary mt-4 w-full"
                                :disabled="
                                    calculateTotalSoutenance() !== 100 ||
                                    savingSoutenancePercentages
                                "
                                :loading="savingSoutenancePercentages"
                                @click="saveSoutenancePercentages"
                            />
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Academic Year Card -->
            <Card class="settings-card">
                <template #title>
                    <div class="card-title">
                        <i class="pi pi-calendar mr-2"></i>
                        <span>Gestion des années scolaires</span>
                    </div>
                </template>
                <template #content>
                    <div v-if="loading" class="p-d-flex p-jc-center">
                        <ProgressSpinner style="width: 50px; height: 50px" />
                    </div>
                    <div v-else class="year-content">
                        <div class="current-year-info mb-4">
                            <h3>Année scolaire courante</h3>
                            <div class="year-detail">
                                <Tag
                                    severity="success"
                                    class="current-year-tag"
                                >
                                    {{ currentYear.annee }}
                                </Tag>
                            </div>
                        </div>

                        <Divider />

                        <div class="archive-section mt-4 mb-4">
                            <h3>Archiver l'année courante</h3>
                            <p class="mb-3">
                                Archiver l'année courante va rendre toutes les
                                données en lecture seule. Une nouvelle année
                                scolaire ({{
                                    getNextAcademicYear(currentYear.annee)
                                }}) sera automatiquement créée. Les pourcentages
                                de l'année courante seront automatiquement
                                copiés.
                            </p>
                            <Button
                                label="Archiver l'année courante"
                                icon="pi pi-box"
                                class="p-button-warning"
                                @click="confirmArchive"
                                :loading="archiving"
                            />
                        </div>

                        <Divider />

                        <div class="years-history mt-4">
                            <h3>Historique des années scolaires</h3>
                            <DataTable
                                :value="academicYears"
                                class="mt-3"
                                :paginator="academicYears.length > 5"
                                :rows="5"
                                stripedRows
                                responsiveLayout="scroll"
                            >
                                <Column
                                    field="annee"
                                    header="Année scolaire"
                                ></Column>
                                <Column header="Statut">
                                    <template #body="slotProps">
                                        <Tag
                                            :severity="
                                                slotProps.data.courante
                                                    ? 'success'
                                                    : 'info'
                                            "
                                        >
                                            {{
                                                slotProps.data.courante
                                                    ? "Courante"
                                                    : "Archivée"
                                            }}
                                        </Tag>
                                    </template>
                                </Column>
                            </DataTable>
                        </div>
                    </div>
                </template>
            </Card>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
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
        const response = await ApiService.put(
            "/chef_de_departement/settings/percentages",
            pourcentages.value
        );

        toast.add({
            severity: "success",
            summary: "Pourcentages mis à jour",
            detail: "Les pourcentages principaux ont été sauvegardés avec succès",
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
        const response = await ApiService.put(
            "/chef_de_departement/settings/percentages/encadrant",
            pourcentagesEncadrant.value
        );

        toast.add({
            severity: "success",
            summary: "Pourcentages Encadrant mis à jour",
            detail: "Les pourcentages Encadrant ont été sauvegardés avec succès",
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
        const response = await ApiService.put(
            "/chef_de_departement/settings/percentages/rapport",
            pourcentagesRapport.value
        );

        toast.add({
            severity: "success",
            summary: "Pourcentages Rapport mis à jour",
            detail: "Les pourcentages Rapport ont été sauvegardés avec succès",
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
        const response = await ApiService.put(
            "/chef_de_departement/settings/percentages/soutenance",
            pourcentagesSoutenance.value
        );

        toast.add({
            severity: "success",
            summary: "Pourcentages Soutenance mis à jour",
            detail: "Les pourcentages Soutenance ont été sauvegardés avec succès",
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
        message: `Êtes-vous sûr de vouloir archiver l'année scolaire courante ${currentYear.value.annee}? Une nouvelle année ${nextYear} sera automatiquement créée. Cette action est irréversible. Les pourcentages de l'année courante seront automatiquement copiés pour la nouvelle année.`,
        header: "Confirmation d'archivage",
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
            summary: "Années mises à jour",
            detail: `L'année scolaire ${currentYear.value.annee} a été archivée et l'année ${response.annee} a été créée automatiquement avec les mêmes pourcentages`,
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

.settings-layout {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
    gap: 2rem;
    margin-top: 2rem;
}

.settings-card {
    margin-bottom: 2rem;
}

.card-title {
    display: flex;
    align-items: center;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--primary-color);
}

.current-year-tag {
    font-size: 1.25rem;
    padding: 0.5rem 1rem;
}

h3 {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 0.75rem;
    color: var(--text-color);
}

/* Responsive tweaks */
@media (max-width: 768px) {
    .settings-layout {
        grid-template-columns: 1fr;
    }
}
</style>
