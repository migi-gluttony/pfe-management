<template>
    <div class="encadrant-grading">
        <Toast />

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
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
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
import Toast from "primevue/toast";

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

// Get current user
const currentUser = AuthService.getCurrentUser();

// Services
const toast = useToast();

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
});

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
    searchQuery.value = query;
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

// Submit evaluation
async function submitEvaluation() {
    if (!validateEvaluationForm()) return;

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
