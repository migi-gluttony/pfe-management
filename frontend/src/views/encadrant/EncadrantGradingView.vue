<template>
    <div class="encadrant-grading">
        <Toast />
        <ConfirmDialog />

        <!-- Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un binôme..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <div class="page-header">
            <h1 class="page-title">Évaluation des Binômes</h1>
        </div>

        <div class="content-container">
            <!-- Binomes assigned to the encadrant -->
            <Card class="binomes-card">
                <template #title>
                    <div class="card-title">
                        <i
                            class="pi pi-users"
                            style="font-size: 1.5rem; margin-right: 0.5rem"
                        ></i>
                        Mes Binômes
                    </div>
                </template>
                <template #content>
                    <DataTable
                        :value="filteredBinomesValue"
                        :loading="loading"
                        stripedRows
                        class="p-datatable-sm"
                        :paginator="filteredBinomesValue.length > 10"
                        :rows="10"
                        v-model:selection="selectedBinome"
                        selectionMode="single"
                        dataKey="id"
                        @row-select="onBinomeSelect"
                        emptyMessage="Aucun binôme assigné"
                    >
                        <Column
                            field="id"
                            header="ID"
                            style="width: 5rem"
                        ></Column>
                        <Column header="Étudiants">
                            <template #body="slotProps">
                                <div class="binome-students">
                                    <div class="student">
                                        <i class="pi pi-user"></i>
                                        <span
                                            >{{
                                                slotProps.data.etudiant1?.prenom
                                            }}
                                            {{
                                                slotProps.data.etudiant1?.nom
                                            }}</span
                                        >
                                    </div>
                                    <div
                                        v-if="slotProps.data.etudiant2"
                                        class="student"
                                    >
                                        <i class="pi pi-user"></i>
                                        <span
                                            >{{
                                                slotProps.data.etudiant2?.prenom
                                            }}
                                            {{
                                                slotProps.data.etudiant2?.nom
                                            }}</span
                                        >
                                    </div>
                                </div>
                            </template>
                        </Column>
                        <Column field="sujet.titre" header="Sujet"></Column>
                        <Column
                            field="filiereName"
                            header="Filière"
                            style="width: 8rem"
                        ></Column>
                        <Column header="Status">
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

            <!-- Evaluation form -->
            <Card v-if="selectedBinome" class="evaluation-card">
                <template #title>
                    <div class="card-title">
                        <i
                            class="pi pi-pencil"
                            style="font-size: 1.5rem; margin-right: 0.5rem"
                        ></i>
                        Évaluation du Binôme
                    </div>
                </template>
                <template #subtitle>
                    {{ selectedBinome.etudiant1?.prenom }}
                    {{ selectedBinome.etudiant1?.nom }}
                    <span v-if="selectedBinome.etudiant2">
                        et {{ selectedBinome.etudiant2?.prenom }}
                        {{ selectedBinome.etudiant2?.nom }}
                    </span>
                </template>
                <template #content>
                    <div class="eval-form">
                        <!-- Reports section integrated into evaluation form (MOVED TO TOP) -->
                        <div class="reports-section">
                            <h3 class="section-title">
                                <i class="pi pi-file-pdf" style="margin-right: 0.5rem"></i>
                                Rapports du Binôme
                            </h3>
                            
                            <div v-if="loadingReports" class="loading-container">
                                <ProgressSpinner />
                                <p>Chargement des rapports...</p>
                            </div>
                            
                            <div v-else-if="reports.length === 0" class="empty-reports">
                                <i class="pi pi-file-o empty-icon"></i>
                                <p>Ce binôme n'a pas encore soumis de rapport final.</p>
                            </div>
                            
                            <DataTable
                                v-else
                                :value="reports"
                                stripedRows
                                class="reports-table"
                                :rows="5"
                                emptyMessage="Aucun rapport trouvé"
                            >
                                <Column field="titre" header="Titre" sortable />
                                <Column header="Date" sortable sortField="dateSoumission">
                                    <template #body="slotProps">
                                        {{ formatDate(slotProps.data.dateSoumission) }}
                                    </template>
                                </Column>
                                <Column header="Actions">
                                    <template #body="slotProps">
                                        <div class="report-actions">
                                            <Button
                                                icon="pi pi-download"
                                                text
                                                @click="downloadReport(slotProps.data)"
                                                tooltip="Télécharger"
                                            />
                                            <Button
                                                icon="pi pi-eye"
                                                text
                                                @click="openPreviewDialog(slotProps.data)"
                                                tooltip="Aperçu"
                                            />
                                        </div>
                                    </template>
                                </Column>
                            </DataTable>
                        </div>
                        
                        <div class="grading-section">
                            <h3 class="section-title">
                                <i class="pi pi-star" style="margin-right: 0.5rem"></i>
                                Notes d'Évaluation
                            </h3>
                        
                            <!-- Technical skills evaluation -->
                            <div class="form-field">
                                <label for="technicalScore"
                                    >Compétences Techniques (0-20)</label
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
                                        inputId="minmax"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez les compétences techniques et
                                    l'implémentation du projet</small
                                >
                            </div>

                            <!-- Report quality evaluation -->
                            <div class="form-field">
                                <label for="reportScore"
                                    >Qualité du Rapport (0-20)</label
                                >
                                <div class="score-input">
                                    <Rating
                                        v-model="evaluationForm.reportScoreRating"
                                        :cancel="false"
                                        :stars="4"
                                        @change="calculateReportScore"
                                    />
                                    <InputNumber
                                        id="reportScore"
                                        v-model="evaluationForm.reportScore"
                                        :min="0"
                                        :max="20"
                                        :step="1"
                                        inputId="reportScoreInput"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez la qualité, la structure et le contenu
                                    du rapport</small
                                >
                            </div>

                            <!-- Progress tracking evaluation -->
                            <div class="form-field">
                                <label for="progressScore"
                                    >Suivi du Projet (0-20)</label
                                >
                                <div class="score-input">
                                    <Rating
                                        v-model="evaluationForm.progressScoreRating"
                                        :cancel="false"
                                        :stars="4"
                                        @change="calculateProgressScore"
                                    />
                                    <InputNumber
                                        id="progressScore"
                                        v-model="evaluationForm.progressScore"
                                        :min="0"
                                        :max="20"
                                        :step="1"
                                        inputId="progressScoreInput"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez le respect des délais et le suivi
                                    régulier du projet</small
                                >
                            </div>

                            <!-- Professionalism evaluation -->
                            <div class="form-field">
                                <label for="professionalismScore"
                                    >Professionnalisme (0-20)</label
                                >
                                <div class="score-input">
                                    <Rating
                                        v-model="
                                            evaluationForm.professionalismScoreRating
                                        "
                                        :cancel="false"
                                        :stars="4"
                                        @change="calculateProfessionalismScore"
                                    />
                                    <InputNumber
                                        id="professionalismScore"
                                        v-model="
                                            evaluationForm.professionalismScore
                                        "
                                        :min="0"
                                        :max="20"
                                        :step="1"
                                        inputId="professionalismScoreInput"
                                        showButtons
                                    />
                                </div>
                                <small
                                    >Évaluez le professionnalisme, la communication
                                    et l'autonomie</small
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
                                    placeholder="Vos commentaires et retours sur le travail du binôme"
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
                    </div>
                </template>
            </Card>

            <Card v-else class="evaluation-card info-card">
                <template #content>
                    <div class="info-message">
                        <i
                            class="pi pi-info-circle"
                            style="font-size: 2rem; color: var(--primary-color)"
                        ></i>
                        <h3>Sélectionnez un binôme pour l'évaluer</h3>
                        <p>
                            Choisissez un binôme dans la liste pour commencer
                            votre évaluation
                        </p>
                    </div>
                </template>
            </Card>
        </div>

        <!-- Preview dialog -->
        <Dialog
            v-model:visible="showPreviewDialog"
            :modal="true"
            :style="{ width: '90vw', height: '90vh' }"
            :header="previewReport ? previewReport.titre : 'Aperçu du rapport'"
            :maximizable="true"
            @show="loadPreviewContent"
        >
            <div class="preview-dialog-content">
                <div v-if="previewLoading" class="preview-loading">
                    <ProgressSpinner />
                    <p>Chargement du rapport en cours...</p>
                </div>
                <div v-else-if="previewError" class="preview-error">
                    <i class="pi pi-exclamation-triangle" style="font-size: 3rem; color: var(--red-500)"></i>
                    <h3>Erreur de prévisualisation</h3>
                    <p>{{ previewError }}</p>
                    <Button 
                        label="Télécharger plutôt" 
                        icon="pi pi-download" 
                        @click="downloadReport(previewReport)" 
                        class="mt-3"
                    />
                </div>
                <div v-else-if="previewContentType && previewContentType.startsWith('image/')" class="image-preview">
                    <img :src="previewDataUrl" :alt="previewReport && previewReport.titre" />
                </div>
                <div v-else-if="previewContentType === 'application/pdf'" class="pdf-preview">
                    <embed 
                        :src="previewDataUrl" 
                        type="application/pdf"
                        width="100%" 
                        height="100%"
                    />
                </div>
                <div v-else class="preview-not-available">
                    <i class="pi pi-file-export" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
                    <h3>Aperçu limité</h3>
                    <p>
                        Ce type de document ne peut pas être prévisualisé directement.
                    </p>
                    <Button 
                        label="Télécharger le document" 
                        icon="pi pi-download" 
                        @click="downloadReport(previewReport)" 
                        class="mt-3"
                    />
                </div>
            </div>
        </Dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { useRoute } from "vue-router";
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
import Toast from "primevue/toast";
import ProgressSpinner from "primevue/progressspinner";
import Dialog from "primevue/dialog";
import ConfirmDialog from "primevue/confirmdialog";

const route = useRoute();

// Component state
const binomes = ref([]);
const loading = ref(false);
const selectedBinome = ref(null);
const submitting = ref(false);
const searchQuery = ref("");
const evaluationForm = ref({
    binomeId: null,
    technicalScore: 0,
    technicalScoreRating: 0,
    reportScore: 0,
    reportScoreRating: 0,
    progressScore: 0,
    progressScoreRating: 0,
    professionalismScore: 0,
    professionalismScoreRating: 0,
    commentaire: "",
});

// Report state
const reports = ref([]);
const loadingReports = ref(false);
const selectedReport = ref(null);

// Preview dialog state
const showPreviewDialog = ref(false);
const previewReport = ref(null);
const previewLoading = ref(false);
const previewError = ref(null);
const previewDataUrl = ref("");
const previewContentType = ref("");
const blobUrls = ref([]);

// Get current user
const currentUser = AuthService.getCurrentUser();

// Services
const toast = useToast();
const confirm = useConfirm();

// Compute filteredBinomes with a different approach to avoid modifying computed directly
const filteredBinomesValue = computed(() => {
    if (!searchQuery.value) return binomes.value;

    const query = searchQuery.value.toLowerCase();
    return binomes.value.filter((binome) => {
        const etudiant1Name =
            `${binome.etudiant1?.prenom} ${binome.etudiant1?.nom}`.toLowerCase();
        const etudiant2Name = binome.etudiant2
            ? `${binome.etudiant2?.prenom} ${binome.etudiant2?.nom}`.toLowerCase()
            : "";
        const sujetTitre = binome.sujet?.titre.toLowerCase() || "";

        return (
            etudiant1Name.includes(query) ||
            etudiant2Name.includes(query) ||
            sujetTitre.includes(query)
        );
    });
});

// Fetch data on component mount
onMounted(async () => {
    await fetchBinomes();

    // Check if we came from the soutenances overview page
    const selectedBinomeId = sessionStorage.getItem("selectedBinomeId");
    if (selectedBinomeId) {
        // Find and select the binome
        const binome = binomes.value.find(
            (b) => b.id === parseInt(selectedBinomeId)
        );
        if (binome) {
            // Clear the stored ID
            sessionStorage.removeItem("selectedBinomeId");

            // Select the binome
            selectedBinome.value = binome;
            // Reset and load evaluation form
            resetEvaluationForm();
            loadExistingEvaluation();
            // Load reports
            fetchReports();

            // Show a toast notification
            toast.add({
                severity: "info",
                summary: "Binôme sélectionné",
                detail: `Évaluation du binôme ${binome.etudiant1?.prenom} ${
                    binome.etudiant1?.nom
                }${
                    binome.etudiant2
                        ? " et " +
                          binome.etudiant2?.prenom +
                          " " +
                          binome.etudiant2?.nom
                        : ""
                }`,
                life: 3000,
            });
        }
    }
});

// Clean up blob URLs on unmount
onBeforeUnmount(() => {
    blobUrls.value.forEach((url) => {
        URL.revokeObjectURL(url);
    });
});

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
    searchQuery.value = query;

    // If there's only one result after filtering, auto-select it
    if (filteredBinomesValue.value.length === 1) {
        selectedBinome.value = filteredBinomesValue.value[0];
        resetEvaluationForm();
        loadExistingEvaluation();
        fetchReports();
    }
}

// Methods for fetching data
async function fetchBinomes() {
    loading.value = true;
    try {
        // Updated endpoint to match the backend
        const response = await ApiService.get("/grading/encadrant/binomes");
        binomes.value = response;
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des binômes");
    } finally {
        loading.value = false;
    }
}

// Fetch reports for the selected binome
async function fetchReports() {
    if (!selectedBinome.value) return;
    
    loadingReports.value = true;
    reports.value = [];
    
    try {
        const response = await ApiService.get(`/grading/encadrant/binome-reports/${selectedBinome.value.id}`);
        reports.value = response;
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des rapports");
    } finally {
        loadingReports.value = false;
    }
}

// Preview report
function openPreviewDialog(report) {
    previewReport.value = report;
    previewLoading.value = true;
    previewError.value = null;
    previewDataUrl.value = "";
    previewContentType.value = "";
    showPreviewDialog.value = true;
}

// Load preview content using document evaluation approach
function loadPreviewContent() {
    if (!previewReport.value) return;
    
    previewLoading.value = true;
    previewError.value = null;
    
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_URL || "/api";
    
    // Fetch the file directly and create a blob URL to avoid X-Frame-Options issues
    fetch(`${baseUrl}/grading/encadrant/report-preview/${previewReport.value.id}`, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`,
        },
    })
    .then((response) => {
        if (!response.ok) {
            throw new Error(`Erreur de prévisualisation: ${response.status}`);
        }
        
        const contentType = response.headers.get("content-type");
        previewContentType.value = contentType || 'application/pdf';
        
        return response.blob();
    })
    .then((blob) => {
        // Create a blob URL which is immune to X-Frame-Options restrictions
        const url = URL.createObjectURL(blob);
        blobUrls.value.push(url);
        previewDataUrl.value = url;
        previewLoading.value = false;
    })
    .catch((error) => {
        console.error("Preview error:", error);
        previewError.value = error.message || "Impossible de prévisualiser le fichier";
        previewLoading.value = false;
    });
}

// Download a report
function downloadReport(report) {
    if (!report) return;
    
    toast.add({
        severity: "info",
        summary: "Téléchargement",
        detail: "Préparation du téléchargement...",
        life: 2000,
    });
    
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_URL || "/api";
    
    // Fix URL path to match controller endpoint
    fetch(`${baseUrl}/grading/encadrant/report-download/${report.id}`, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`,
        },
    })
    .then((response) => {
        if (!response.ok) {
            throw new Error(`Erreur de téléchargement: ${response.status}`);
        }
        
        let filename = "rapport.pdf";
        const disposition = response.headers.get("content-disposition");
        if (disposition && disposition.includes("filename=")) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(disposition);
            if (matches && matches[1]) {
                filename = matches[1].replace(/['"]/g, "");
            }
        }
        
        return response.blob().then((blob) => {
            return { blob, filename };
        });
    })
    .then(({ blob, filename }) => {
        const url = URL.createObjectURL(blob);
        blobUrls.value.push(url); // Store for cleanup
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        
        setTimeout(() => {
            document.body.removeChild(a);
        }, 100);
        
        toast.add({
            severity: "success",
            summary: "Téléchargement réussi",
            detail: `Fichier "${filename}" téléchargé avec succès`,
            life: 3000,
        });
    })
    .catch((error) => {
        console.error("Download error:", error);
        toast.add({
            severity: "error",
            summary: "Erreur de téléchargement",
            detail: error.message || "Impossible de télécharger le fichier",
            life: 3000,
        });
    });
}

// Check if binome has already been evaluated
function getEvaluationStatus(binome) {
    // In a real application, this would check if an evaluation exists already
    if (binome.noteEncadrant) {
        return "Évalué";
    } else {
        return "Non évalué";
    }
}

// Get severity for the status tag
function getEvaluationStatusSeverity(binome) {
    if (binome.noteEncadrant) {
        return "success";
    } else {
        return "warning";
    }
}

// Handle binome selection
function onBinomeSelect(event) {
    selectedBinome.value = event.data;
    resetEvaluationForm();
    loadExistingEvaluation();
    fetchReports();
}

// Reset evaluation form
function resetEvaluationForm() {
    evaluationForm.value = {
        binomeId: selectedBinome.value.id,
        technicalScore: 0,
        technicalScoreRating: 0,
        reportScore: 0,
        reportScoreRating: 0,
        progressScore: 0,
        progressScoreRating: 0,
        professionalismScore: 0,
        professionalismScoreRating: 0,
        commentaire: "",
    };
}

// Load existing evaluation if any
async function loadExistingEvaluation() {
    try {
        // Updated endpoint to match the backend
        const response = await ApiService.get(
            `/grading/encadrant/evaluations/${selectedBinome.value.id}`
        );
        if (response) {
            evaluationForm.value = {
                binomeId: selectedBinome.value.id,
                technicalScore: response.technicalScore,
                technicalScoreRating: response.technicalScore / 5, // Convert score to rating
                reportScore: response.reportScore,
                reportScoreRating: response.reportScore / 5, // Convert score to rating
                progressScore: response.progressScore,
                progressScoreRating: response.progressScore / 5, // Convert score to rating
                professionalismScore: response.professionalismScore,
                professionalismScoreRating: response.professionalismScore / 5, // Convert score to rating
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

function calculateReportScore() {
    evaluationForm.value.reportScore = Math.round(
        evaluationForm.value.reportScoreRating * 5
    );
}

function calculateProgressScore() {
    evaluationForm.value.progressScore = Math.round(
        evaluationForm.value.progressScoreRating * 5
    );
}

function calculateProfessionalismScore() {
    evaluationForm.value.professionalismScore = Math.round(
        evaluationForm.value.professionalismScoreRating * 5
    );
}

// Format date for display
function formatDate(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("fr-FR", {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}

// Submit evaluation
async function submitEvaluation() {
    if (!validateEvaluationForm()) return;

    confirm.require({
        message: "Êtes-vous sûr de vouloir soumettre cette évaluation ?",
        header: "Confirmation",
        icon: "pi pi-info-circle",
        acceptClass: "p-button-primary",
        accept: async () => {
            submitting.value = true;

            // Create a cleaned up payload without the rating properties
            const payload = {
                binomeId: evaluationForm.value.binomeId,
                technicalScore: evaluationForm.value.technicalScore,
                reportScore: evaluationForm.value.reportScore,
                progressScore: evaluationForm.value.progressScore,
                professionalismScore: evaluationForm.value.professionalismScore,
                commentaire: evaluationForm.value.commentaire,
            };

            try {
                const response = await ApiService.post(
                    "/grading/encadrant/evaluation",
                    payload
                );

                // Update binome in the list
                const index = binomes.value.findIndex(
                    (b) => b.id === selectedBinome.value.id
                );
                if (index !== -1) {
                    binomes.value[index].noteEncadrant = true;
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
    });
}

// Validate form before submission
function validateEvaluationForm() {
    if (!evaluationForm.value.binomeId) {
        showValidationError("Erreur de sélection du binôme");
        return false;
    }

    if (
        evaluationForm.value.technicalScore < 0 ||
        evaluationForm.value.technicalScore > 20 ||
        evaluationForm.value.reportScore < 0 ||
        evaluationForm.value.reportScore > 20 ||
        evaluationForm.value.progressScore < 0 ||
        evaluationForm.value.progressScore > 20 ||
        evaluationForm.value.professionalismScore < 0 ||
        evaluationForm.value.professionalismScore > 20
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
.encadrant-grading {
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

.content-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

.binomes-card,
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

.eval-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.grading-section {
    margin-top: 1.5rem;
    border-top: 1px solid var(--surface-border);
    padding-top: 1.5rem;
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

/* Report section styles */
.reports-section {
    margin-bottom: 1rem;
}

.section-title {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
}

.empty-reports {
    text-align: center;
    padding: 1.5rem;
    color: var(--text-color-secondary);
}

.reports-table {
    margin-top: 0.5rem;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
}

.loading-container p {
    margin-top: 1rem;
    color: var(--text-color-secondary);
}

.empty-icon {
    font-size: 3rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}

.report-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}

/* Preview dialog styles */
.preview-dialog-content {
    height: 80vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.preview-loading, .preview-error, .preview-not-available {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    height: 100%;
    padding: 2rem;
}

.image-preview {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    width: 100%;
    background-color: var(--surface-ground);
    overflow: hidden;
}

.image-preview img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.pdf-preview {
    height: 100%;
    width: 100%;
}

.pdf-preview embed {
    border: none;
    height: 100%;
    width: 100%;
}

.mt-3 {
    margin-top: 1rem;
}

/* Responsive layout */
@media (max-width: 992px) {
    .content-container {
        grid-template-columns: 1fr;
    }
}
</style>