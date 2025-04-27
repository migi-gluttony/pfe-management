<template>
    <div class="jury-report-evaluation">
        <Toast />
        <ConfirmDialog />

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
                                @click="downloadReport(selectedReport)"
                            />
                            <Button
                                label="Voir le PDF"
                                icon="pi pi-eye"
                                @click="openPreviewDialog(selectedReport)"
                            />
                        </div>
                    </template>
                    <template #content>
                        <div class="report-info">
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
                                <div class="info-label">Date de soumission:</div>
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
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { useRoute } from 'vue-router';
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
import ConfirmDialog from "primevue/confirmdialog";

const route = useRoute();

// Component state
const reports = ref([]);
const loading = ref(false);
const selectedReport = ref(null);
const submitting = ref(false);
const searchQuery = ref("");
const soutenances = ref([]);
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

// Preview dialog state
const showPreviewDialog = ref(false);
const previewReport = ref(null);
const previewLoading = ref(false);
const previewError = ref(null);
const previewDataUrl = ref("");
const previewContentType = ref("");
const blobUrls = ref([]);

// Create a derived value for filtered reports
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
const confirm = useConfirm();

// Fetch data on component mount
onMounted(async () => {
  // Check if we came from the soutenances overview page first
  const selectedSoutenanceId = sessionStorage.getItem('selectedSoutenanceId');
  
  // Fetch reports first
  await fetchReports();
  
  if (selectedSoutenanceId) {
    // Clear the stored ID
    sessionStorage.removeItem('selectedSoutenanceId');
    
    // Fetch soutenances to get the binome ID
    try {
      const soutenancesResponse = await ApiService.get("/grading/jury/soutenances");
      const targetSoutenance = soutenancesResponse.find(s => s.id === parseInt(selectedSoutenanceId));
      
      if (targetSoutenance && targetSoutenance.binome) {
        // Find the report by matching binome ID
        const report = reports.value.find(r => r.binome?.id === targetSoutenance.binome.id);
        
        if (report) {
          // Select the report
          selectedReport.value = report;
          resetEvaluationForm();
          loadExistingEvaluation();
        } else {
          // If no report found, show notification
          toast.add({
            severity: 'warn',
            summary: 'Rapport non trouvé',
            detail: 'Aucun rapport n\'a été trouvé pour cette soutenance',
            life: 5000
          });
        }
      }
    } catch (error) {
      console.error('Error fetching soutenance details:', error);
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Impossible de récupérer les détails de la soutenance',
        life: 5000
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
  if (filteredReportsValue.value.length === 1) {
    selectedReport.value = filteredReportsValue.value[0];
    resetEvaluationForm();
    loadExistingEvaluation();
  }
}

// Format date for display
function formatDate(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("fr-FR", {
        year: "numeric",
        month: "long",
        day: "numeric"
    }).format(date);
}

// Format date and time for display
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return "";
    const dateTime = new Date(dateTimeString);
    return new Intl.DateTimeFormat("fr-FR", {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }).format(dateTime);
}

// Methods for fetching data
async function fetchReports() {
  loading.value = true;
  try {
    const response = await ApiService.get("/grading/jury/reports");
    reports.value = response;
    
    // Update the filteredReportsValue
    if (reports.value.length > 0) {
      filteredReportsValue.value = [...reports.value];
    }
  } catch (error) {
    handleApiError(error, "Erreur lors du chargement des rapports");
    reports.value = [];
    filteredReportsValue.value = [];
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
    resetEvaluationForm();
    loadExistingEvaluation();
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

// Load preview content
function loadPreviewContent() {
    if (!previewReport.value) return;
    
    previewLoading.value = true;
    previewError.value = null;
    
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_URL || "/api";
    
    // Fetch the file directly and create a blob URL to avoid X-Frame-Options issues
    fetch(`${baseUrl}/grading/jury/report-preview/${previewReport.value.id}`, {
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
    
    fetch(`${baseUrl}/grading/jury/report-download/${report.id}`, {
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

    confirm.require({
        message: "Êtes-vous sûr de vouloir soumettre cette évaluation ?",
        header: "Confirmation",
        icon: "pi pi-info-circle",
        acceptClass: "p-button-primary",
        accept: async () => {
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
    });
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