<template>
    <div class="jury-report-evaluation">
        <Toast />

        <!-- Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un rapport..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <div class="page-header">
            <h1 class="page-title">
                Évaluation des Rapports des Soutenances Assignées
            </h1>
        </div>

        <div v-if="loading" class="loading-container">
            <ProgressSpinner />
            <p>Chargement des rapports...</p>
        </div>

        <div v-else-if="reports.length === 0" class="empty-state">
            <i
                class="pi pi-info-circle"
                style="font-size: 3rem; color: var(--primary-color)"
            ></i>
            <h2>Aucun rapport à évaluer</h2>
            <p>
                Vous n'avez pas encore été assigné(e) à des soutenances ou les
                rapports n'ont pas encore été soumis.
            </p>
        </div>

        <div v-else class="content-container">
            <!-- Reports list -->
            <Card class="reports-card">
                <template #title>
                    <div class="card-title">
                        <i
                            class="pi pi-file-pdf"
                            style="font-size: 1.5rem; margin-right: 0.5rem"
                        ></i>
                        Rapports à Évaluer
                    </div>
                </template>
                <template #content>
                    <DataTable
                        :value="filteredReportsValue"
                        :loading="loading"
                        stripedRows
                        class="p-datatable-sm"
                        :paginator="filteredReportsValue.length > 10"
                        :rows="10"
                        v-model:selection="selectedReport"
                        selectionMode="single"
                        dataKey="id"
                        @row-select="onReportSelect"
                        emptyMessage="Aucun rapport trouvé pour votre recherche"
                    >
                        <Column
                            field="id"
                            header="ID"
                            style="width: 5rem"
                        ></Column>
                        <Column field="titre" header="Titre"></Column>
                        <Column header="Binôme">
                            <template #body="slotProps">
                                <div class="binome-students">
                                    <div class="student">
                                        <i class="pi pi-user"></i>
                                        <span
                                            >{{
                                                slotProps.data.binome?.etudiant1
                                                    ?.prenom
                                            }}
                                            {{
                                                slotProps.data.binome?.etudiant1
                                                    ?.nom
                                            }}</span
                                        >
                                    </div>
                                    <div
                                        v-if="slotProps.data.binome?.etudiant2"
                                        class="student"
                                    >
                                        <i class="pi pi-user"></i>
                                        <span
                                            >{{
                                                slotProps.data.binome?.etudiant2
                                                    ?.prenom
                                            }}
                                            {{
                                                slotProps.data.binome?.etudiant2
                                                    ?.nom
                                            }}</span
                                        >
                                    </div>
                                </div>
                            </template>
                        </Column>
                        <Column
                            field="dateSubmission"
                            header="Date de soumission"
                        >
                            <template #body="slotProps">
                                {{ formatDate(slotProps.data.dateSoumission) }}
                            </template>
                        </Column>
                        <Column header="Statut">
                            <template #body="slotProps">
                                <Tag
                                    :value="getEvaluationStatus(slotProps.data)"
                                    :severity="
                                        getEvaluationStatusSeverity(
                                            slotProps.data
                                        )
                                    "
                                />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>

            <!-- Report preview and evaluation form -->
            <div v-if="selectedReport" class="eval-section">
                <!-- Report Preview Card -->
                <Card class="report-preview-card">
                    <template #title>
                        <div class="card-title">
                            <i
                                class="pi pi-file-pdf"
                                style="font-size: 1.5rem; margin-right: 0.5rem"
                            ></i>
                            Rapport: {{ selectedReport.titre }}
                        </div>
                    </template>
                    <template #subtitle>
                        <div class="report-actions">
                            <Button
                                label="Télécharger"
                                icon="pi pi-download"
                                class="p-button-outlined p-button-primary"
                                @click="downloadReport"
                            />
                            <Button
                                label="Voir le PDF"
                                icon="pi pi-eye"
                                @click="previewReport"
                            />
                        </div>
                    </template>
                    <template #content>
                        <div v-if="showPdfPreview" class="pdf-preview">
                            <iframe
                                :src="
                                    getPdfViewUrl(
                                        selectedReport.localisationRapport
                                    )
                                "
                                width="100%"
                                height="500"
                                frameborder="0"
                                title="PDF Preview"
                            ></iframe>
                        </div>
                        <div v-else class="report-info">
                            <div class="info-row">
                                <div class="info-label">Titre:</div>
                                <div class="info-value">
                                    {{ selectedReport.titre }}
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Auteurs:</div>
                                <div class="info-value">
                                    {{
                                        selectedReport.binome?.etudiant1?.prenom
                                    }}
                                    {{ selectedReport.binome?.etudiant1?.nom }}
                                    <span
                                        v-if="selectedReport.binome?.etudiant2"
                                    >
                                        et
                                        {{
                                            selectedReport.binome?.etudiant2
                                                ?.prenom
                                        }}
                                        {{
                                            selectedReport.binome?.etudiant2
                                                ?.nom
                                        }}
                                    </span>
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Encadrant:</div>
                                <div class="info-value">
                                    {{
                                        selectedReport.binome?.encadrant?.prenom
                                    }}
                                    {{ selectedReport.binome?.encadrant?.nom }}
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">
                                    Date de soumission:
                                </div>
                                <div class="info-value">
                                    {{
                                        formatDateTime(
                                            selectedReport.dateSoumission
                                        )
                                    }}
                                </div>
                            </div>
                            <div class="report-description">
                                <p>
                                    Cliquez sur "Voir le PDF" pour consulter le
                                    rapport complet.
                                </p>
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Evaluation form -->
                <Card class="evaluation-card">
                    <template #title>
                        <div class="card-title">
                            <i
                                class="pi pi-pencil"
                                style="font-size: 1.5rem; margin-right: 0.5rem"
                            ></i>
                            Évaluation du Rapport
                        </div>
                    </template>
                    <template #content>
                        <div class="eval-form">
                            <!-- Technical content evaluation -->
                            <div class="form-field">
                                <label for="technicalScore"
                                    >Contenu Technique (0-20)</label
                                >
                                <div class="score-input">
                                    <Rating
                                        v-model="
                                            evaluationForm.technicalScoreRating
                                        "
                                        :cancel="false"
                                        :stars="4"
                                        @change="calculateTechnicalScore"
                                    />
                                    <InputNumber
                                        id="technicalScore"
                                        v-model="evaluationForm.technicalScore"
                                        :min="0"
                                        :max="20"
                                        :step="1"
                                        inputId="technicalScoreInput"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez la qualité technique et
                                    scientifique du contenu</small
                                >
                            </div>

                            <!-- Structure evaluation -->
                            <div class="form-field">
                                <label for="structureScore"
                                    >Structure et Organisation (0-20)</label
                                >
                                <div class="score-input">
                                    <Rating
                                        v-model="
                                            evaluationForm.structureScoreRating
                                        "
                                        :cancel="false"
                                        :stars="4"
                                        @change="calculateStructureScore"
                                    />
                                    <InputNumber
                                        id="structureScore"
                                        v-model="evaluationForm.structureScore"
                                        :min="0"
                                        :max="20"
                                        :step="1"
                                        inputId="structureScoreInput"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez l'organisation, la clarté et la
                                    cohérence du rapport</small
                                >
                            </div>

                            <!-- Originality evaluation -->
                            <div class="form-field">
                                <label for="originalityScore"
                                    >Originalité (0-20)</label
                                >
                                <div class="score-input">
                                    <Rating
                                        v-model="
                                            evaluationForm.originalityScoreRating
                                        "
                                        :cancel="false"
                                        :stars="4"
                                        @change="calculateOriginalityScore"
                                    />
                                    <InputNumber
                                        id="originalityScore"
                                        v-model="
                                            evaluationForm.originalityScore
                                        "
                                        :min="0"
                                        :max="20"
                                        :step="1"
                                        inputId="originalityScoreInput"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez l'originalité, la créativité et
                                    l'innovation du rapport</small
                                >
                            </div>

                            <!-- Comments -->
                            <div class="form-field">
                                <label for="commentaire">Commentaires</label>
                                <Textarea
                                    id="commentaire"
                                    v-model="evaluationForm.commentaire"
                                    rows="3"
                                    class="w-full"
                                    placeholder="Vos commentaires sur le rapport"
                                    autoResize
                                />
                            </div>

                            <div class="form-actions">
                                <Button
                                    label="Soumettre l'évaluation"
                                    icon="pi pi-check"
                                    @click="submitEvaluation"
                                    :loading="submitting"
                                    class="p-button-primary"
                                />
                            </div>
                        </div>
                    </template>
                </Card>
            </div>

            <Card v-else class="eval-section info-card">
                <template #content>
                    <div class="info-message">
                        <i
                            class="pi pi-info-circle"
                            style="font-size: 2rem; color: var(--primary-color)"
                        ></i>
                        <h3>Sélectionnez un rapport pour l'évaluer</h3>
                        <p>
                            Choisissez un rapport dans la liste pour commencer
                            votre évaluation
                        </p>
                    </div>
                </template>
            </Card>
        </div>

        <!-- PDF Dialog -->
        <Dialog
            v-model:visible="showPdfDialog"
            :modal="true"
            :style="{ width: '80vw', height: '80vh' }"
            header="Visualisation du Rapport"
            :maximizable="true"
        >
            <iframe
                v-if="showPdfDialog && selectedReport"
                :src="getPdfViewUrl(selectedReport.localisationRapport)"
                width="100%"
                height="100%"
                frameborder="0"
                title="PDF Viewer"
            ></iframe>
        </Dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useToast } from "primevue/usetoast";
import ApiService from "@/services/ApiService";
import AuthService from "@/services/AuthService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";

// Import PrimeVue components
import Card from "primevue/card";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import InputNumber from "primevue/inputnumber";
import Textarea from "primevue/textarea";
import Rating from "primevue/rating";
import Button from "primevue/button";
import Tag from "primevue/tag";
import Dialog from "primevue/dialog";
import Toast from "primevue/toast";
import ProgressSpinner from "primevue/progressspinner";

// Component state
const reports = ref([]);
const loading = ref(false);
const selectedReport = ref(null);
const submitting = ref(false);
const searchQuery = ref("");
const showPdfPreview = ref(false);
const showPdfDialog = ref(false);
const evaluationForm = ref({
    rapportId: null,
    technicalScore: 0,
    technicalScoreRating: 0,
    structureScore: 0,
    structureScoreRating: 0,
    originalityScore: 0,
    originalityScoreRating: 0,
    commentaire: "",
});

// Create a derived value for filtered reports rather than a computed property
// to avoid the "write operation failed: computed value is readonly" error
const filteredReportsValue = ref([]);

watch(
    [reports, searchQuery],
    ([newReports, newQuery]) => {
        // If no search query, use all reports
        if (!newQuery) {
            filteredReportsValue.value = [...newReports];
            return;
        }

        // Otherwise, filter based on search term
        const query = newQuery.toLowerCase();
        filteredReportsValue.value = newReports.filter((report) => {
            const reportTitle = report.titre?.toLowerCase() || "";
            const etudiant1Name =
                `${report.binome?.etudiant1?.prenom} ${report.binome?.etudiant1?.nom}`.toLowerCase();
            const etudiant2Name = report.binome?.etudiant2
                ? `${report.binome.etudiant2.prenom} ${report.binome.etudiant2.nom}`.toLowerCase()
                : "";

            return (
                reportTitle.includes(query) ||
                etudiant1Name.includes(query) ||
                etudiant2Name.includes(query)
            );
        });
    },
    { immediate: true }
);

// Get current user
const currentUser = AuthService.getCurrentUser();

// Services
const toast = useToast();

// Fetch data on component mount
onMounted(async () => {
    await fetchReports();
});

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
    searchQuery.value = query;
}

// Format date for display
function formatDate(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString();
}

// Format date and time for display
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return "";
    const dateTime = new Date(dateTimeString);
    return `${dateTime.toLocaleDateString()} ${dateTime.toLocaleTimeString()}`;
}

// Function to construct PDF viewer URL
function getPdfViewUrl(path) {
    // Check if path is already a full URL
    if (path && (path.startsWith("http://") || path.startsWith("https://"))) {
        return path;
    }

    // Otherwise, assume it's a relative path and needs the base URL
    // You might need a specific endpoint to serve files, adjust as needed
    return `/uploads/files/${path}`;
}

// Methods for fetching data
async function fetchReports() {
    loading.value = true;
    try {
        // This endpoint should now return only reports for soutenances where the jury is assigned
        const response = await ApiService.get("/grading/jury/reports");
        reports.value = response;
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des rapports");

        // Use empty array if API call fails
        reports.value = [];
    } finally {
        loading.value = false;
    }
}

// Check if report has already been evaluated
function getEvaluationStatus(report) {
    if (report.evaluated) {
        return "Évalué";
    } else {
        return "Non évalué";
    }
}

// Get severity for the status tag
function getEvaluationStatusSeverity(report) {
    if (report.evaluated) {
        return "success";
    } else {
        return "warning";
    }
}

// Handle report selection
function onReportSelect(event) {
    selectedReport.value = event.data;
    showPdfPreview.value = false;
    resetEvaluationForm();
    loadExistingEvaluation();
}

// Preview report in the card
function previewReport() {
    showPdfPreview.value = !showPdfPreview.value;
}

// Open report in a dialog
function viewReportInDialog() {
    showPdfDialog.value = true;
}

// Download report
function downloadReport() {
    if (!selectedReport.value || !selectedReport.value.localisationRapport) {
        toast.add({
            severity: "error",
            summary: "Erreur",
            detail: "Impossible de télécharger le rapport",
            life: 3000,
        });
        return;
    }

    // Create a link element to trigger download
    const link = document.createElement("a");
    link.href = getPdfViewUrl(selectedReport.value.localisationRapport);
    link.download = `${selectedReport.value.titre || "rapport"}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    toast.add({
        severity: "success",
        summary: "Téléchargement",
        detail: "Téléchargement du rapport initié",
        life: 3000,
    });
}

// Reset evaluation form
function resetEvaluationForm() {
    evaluationForm.value = {
        rapportId: selectedReport.value?.id,
        technicalScore: 0,
        technicalScoreRating: 0,
        structureScore: 0,
        structureScoreRating: 0,
        originalityScore: 0,
        originalityScoreRating: 0,
        commentaire: "",
    };
}

// Load existing evaluation if any
async function loadExistingEvaluation() {
    if (!selectedReport.value) return;

    try {
        const response = await ApiService.get(
            `/grading/jury/report-evaluations/${selectedReport.value.id}`
        );
        if (response) {
            evaluationForm.value = {
                rapportId: selectedReport.value.id,
                technicalScore: response.technicalScore,
                technicalScoreRating: response.technicalScore / 5,
                structureScore: response.structureScore,
                structureScoreRating: response.structureScore / 5,
                originalityScore: response.originalityScore,
                originalityScoreRating: response.originalityScore / 5,
                commentaire: response.commentaire,
            };
        }
    } catch (error) {
        // If 404, it means no evaluation exists yet, which is fine
        if (error.status !== 404) {
            handleApiError(error, "Erreur lors du chargement de l'évaluation");
        }
    }
}

// Calculate scores from rating component
function calculateTechnicalScore() {
    evaluationForm.value.technicalScore = Math.round(
        evaluationForm.value.technicalScoreRating * 5
    );
}

function calculateStructureScore() {
    evaluationForm.value.structureScore = Math.round(
        evaluationForm.value.structureScoreRating * 5
    );
}

function calculateOriginalityScore() {
    evaluationForm.value.originalityScore = Math.round(
        evaluationForm.value.originalityScoreRating * 5
    );
}

// Submit evaluation
async function submitEvaluation() {
    if (!validateEvaluationForm()) return;

    submitting.value = true;

    // Create a cleaned up payload without the rating properties
    const payload = {
        rapportId: evaluationForm.value.rapportId,
        technicalScore: evaluationForm.value.technicalScore,
        structureScore: evaluationForm.value.structureScore,
        originalityScore: evaluationForm.value.originalityScore,
        commentaire: evaluationForm.value.commentaire,
    };

    try {
        await ApiService.post("/grading/jury/rapport-evaluation", payload);

        // Update report in the list to mark as evaluated
        const index = reports.value.findIndex(
            (r) => r.id === selectedReport.value.id
        );
        if (index !== -1) {
            reports.value[index].evaluated = true;
            // Update the filtered list as well
            const filteredIndex = filteredReportsValue.value.findIndex(
                (r) => r.id === selectedReport.value.id
            );
            if (filteredIndex !== -1) {
                filteredReportsValue.value[filteredIndex].evaluated = true;
            }
        }

        toast.add({
            severity: "success",
            summary: "Évaluation soumise",
            detail: "Votre évaluation a été enregistrée avec succès",
            life: 3000,
        });
    } catch (error) {
        handleApiError(error, "Erreur lors de la soumission de l'évaluation");
    } finally {
        submitting.value = false;
    }
}

// Validate form before submission
function validateEvaluationForm() {
    if (!evaluationForm.value.rapportId) {
        showValidationError("Erreur de sélection du rapport");
        return false;
    }

    if (
        evaluationForm.value.technicalScore < 0 ||
        evaluationForm.value.technicalScore > 20 ||
        evaluationForm.value.structureScore < 0 ||
        evaluationForm.value.structureScore > 20 ||
        evaluationForm.value.originalityScore < 0 ||
        evaluationForm.value.originalityScore > 20
    ) {
        showValidationError("Les notes doivent être comprises entre 0 et 20");
        return false;
    }

    return true;
}

// Error handling
function handleApiError(error, defaultMessage) {
    console.error(defaultMessage, error);

    let errorMessage = defaultMessage;
    if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
    }

    toast.add({
        severity: "error",
        summary: "Erreur",
        detail: errorMessage,
        life: 5000,
    });
}

function showValidationError(message) {
    toast.add({
        severity: "warn",
        summary: "Validation",
        detail: message,
        life: 5000,
    });
}
</script>

<style scoped>
.jury-report-evaluation {
    width: 100%;
}

.page-header {
    margin-bottom: 2rem;
}

.page-title {
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
    margin: 0;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem;
    gap: 1rem;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem;
    text-align: center;
    color: var(--text-color-secondary);
}

.empty-state h2 {
    margin: 1rem 0 0.5rem;
    color: var(--text-color);
}

.content-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

.reports-card {
    width: 100%;
}

.eval-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    width: 100%;
}

.report-preview-card,
.evaluation-card {
    width: 100%;
}

.card-title {
    display: flex;
    align-items: center;
    font-size: 1.2rem;
    font-weight: 600;
}

.binome-students {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.student {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.report-actions {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.5rem;
}

.pdf-preview {
    width: 100%;
    height: 500px;
    border: 1px solid var(--surface-border);
    border-radius: 4px;
    overflow: hidden;
}

.report-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.info-row {
    display: flex;
    gap: 0.5rem;
}

.info-label {
    font-weight: 600;
    min-width: 150px;
}

.report-description {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--surface-border);
}

.eval-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-field {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-field label {
    font-weight: 600;
}

.form-field small {
    color: var(--text-color-secondary);
}

.score-input {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 1rem;
}

.info-card {
    display: flex;
    justify-content: center;
    align-items: center;
}

.info-message {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 2rem;
}

.info-message h3 {
    margin-top: 1rem;
    margin-bottom: 0.5rem;
}

.info-message p {
    color: var(--text-color-secondary);
}

/* Responsive layout */
@media (max-width: 992px) {
    .content-container {
        grid-template-columns: 1fr;
    }
}
</style>
