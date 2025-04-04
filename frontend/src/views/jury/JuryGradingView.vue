<template>
    <div class="jury-grading">
        <Toast />

        <!-- Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher une soutenance..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <div class="page-header">
            <h1 class="page-title">Évaluation des Soutenances</h1>
            <div class="session-selector">
                <DatePicker
                    v-model="selectedDate"
                    placeholder="Sélectionner une date"
                    @date-select="filterSoutenancesByDate"
                    showIcon
                    dateFormat="dd/mm/yy"
                />
                <Button
                    label="Toutes les soutenances"
                    icon="pi pi-calendar-times"
                    class="p-button-outlined"
                    @click="resetDateFilter"
                />
            </div>
        </div>

        <div class="content-container">
            <!-- Soutenances assigned to the jury -->
            <Card class="soutenances-card">
                <template #title>
                    <div class="card-title">
                        <i
                            class="pi pi-calendar"
                            style="font-size: 1.5rem; margin-right: 0.5rem"
                        ></i>
                        Mes Soutenances
                    </div>
                </template>
                <template #content>
                    <DataTable
                        :value="filteredSoutenancesValue"
                        :loading="loading"
                        stripedRows
                        class="p-datatable-sm"
                        :paginator="filteredSoutenancesValue.length > 10"
                        :rows="10"
                        v-model:selection="selectedSoutenance"
                        selectionMode="single"
                        dataKey="id"
                        @row-select="onSoutenanceSelect"
                        emptyMessage="Aucune soutenance assignée"
                    >
                        <Column
                            field="id"
                            header="ID"
                            style="width: 5rem"
                        ></Column>
                        <Column field="date" header="Date">
                            <template #body="slotProps">
                                {{ formatDate(slotProps.data.date) }}
                            </template>
                        </Column>
                        <Column field="heure" header="Heure"></Column>
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
                        <Column field="salle.nom" header="Salle"></Column>
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

            <!-- Evaluation form for selected student -->
            <Card v-if="selectedStudent" class="evaluation-card">
                <template #title>
                    <div class="card-title">
                        <i
                            class="pi pi-pencil"
                            style="font-size: 1.5rem; margin-right: 0.5rem"
                        ></i>
                        Évaluation de l'Étudiant
                    </div>
                </template>
                <template #subtitle>
                    {{ selectedStudent.prenom }} {{ selectedStudent.nom }}
                </template>
                <template #content>
                    <div class="eval-form">
                        <!-- Presentation evaluation -->
                        <div class="form-field">
                            <label for="presentationScore"
                                >Qualité de la Présentation (0-20)</label
                            >
                            <div class="score-input">
                                <Rating
                                    v-model="
                                        evaluationForm.presentationScoreRating
                                    "
                                    :cancel="false"
                                    :stars="4"
                                    @change="calculatePresentationScore"
                                />
                                <InputNumber
                                    id="presentationScore"
                                    v-model="evaluationForm.presentationScore"
                                    :min="0"
                                    :max="20"
                                    :step="1"
                                    inputId="minmax"
                                    showButtons
                                />
                            </div>
                            <small
                                >Évaluez la qualité et la clarté de la
                                présentation</small
                            >
                        </div>

                        <!-- Q&A evaluation -->
                        <div class="form-field">
                            <label for="qaScore"
                                >Réponses aux Questions (0-20)</label
                            >
                            <div class="score-input">
                                <Rating
                                    v-model="evaluationForm.qaScoreRating"
                                    :cancel="false"
                                    :stars="4"
                                    @change="calculateQAScore"
                                />
                                <InputNumber
                                    id="qaScore"
                                    v-model="evaluationForm.qaScore"
                                    :min="0"
                                    :max="20"
                                    :step="1"
                                    inputId="minmax"
                                    showButtons
                                />
                            </div>
                            <small
                                >Évaluez la qualité des réponses aux
                                questions</small
                            >
                        </div>

                        <!-- Time management evaluation -->
                        <div class="form-field">
                            <label for="timeManagementScore"
                                >Gestion du Temps (0-20)</label
                            >
                            <div class="score-input">
                                <Rating
                                    v-model="
                                        evaluationForm.timeManagementScoreRating
                                    "
                                    :cancel="false"
                                    :stars="4"
                                    @change="calculateTimeScore"
                                />
                                <InputNumber
                                    id="timeManagementScore"
                                    v-model="evaluationForm.timeManagementScore"
                                    :min="0"
                                    :max="20"
                                    :step="1"
                                    inputId="minmax"
                                    showButtons
                                />
                            </div>
                            <small>Évaluez le respect du temps alloué</small>
                        </div>

                        <!-- Comments -->
                        <div class="form-field">
                            <label for="commentaire">Commentaires</label>
                            <Textarea
                                id="commentaire"
                                v-model="evaluationForm.commentaire"
                                rows="3"
                                class="w-full"
                                placeholder="Vos commentaires sur la prestation de l'étudiant"
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

            <!-- Student selection if soutenance is selected -->
            <Card v-else-if="selectedSoutenance" class="evaluation-card">
                <template #title>
                    <div class="card-title">
                        <i
                            class="pi pi-users"
                            style="font-size: 1.5rem; margin-right: 0.5rem"
                        ></i>
                        Sélectionner un Étudiant
                    </div>
                </template>
                <template #content>
                    <div class="students-selection">
                        <p class="instructions">
                            Veuillez sélectionner un étudiant à évaluer :
                        </p>

                        <div
                            class="student-option"
                            @click="
                                selectStudent(
                                    selectedSoutenance.binome.etudiant1
                                )
                            "
                        >
                            <Avatar icon="pi pi-user" size="large" />
                            <div class="student-info">
                                <h3>
                                    {{
                                        selectedSoutenance.binome.etudiant1
                                            .prenom
                                    }}
                                    {{
                                        selectedSoutenance.binome.etudiant1.nom
                                    }}
                                </h3>
                                <p>
                                    {{
                                        getStudentEvaluationStatus(
                                            selectedSoutenance.binome.etudiant1
                                        )
                                    }}
                                </p>
                            </div>
                            <Button
                                icon="pi pi-arrow-right"
                                class="p-button-rounded p-button-outlined"
                            />
                        </div>

                        <div
                            v-if="selectedSoutenance.binome.etudiant2"
                            class="student-option"
                            @click="
                                selectStudent(
                                    selectedSoutenance.binome.etudiant2
                                )
                            "
                        >
                            <Avatar icon="pi pi-user" size="large" />
                            <div class="student-info">
                                <h3>
                                    {{
                                        selectedSoutenance.binome.etudiant2
                                            .prenom
                                    }}
                                    {{
                                        selectedSoutenance.binome.etudiant2.nom
                                    }}
                                </h3>
                                <p>
                                    {{
                                        getStudentEvaluationStatus(
                                            selectedSoutenance.binome.etudiant2
                                        )
                                    }}
                                </p>
                            </div>
                            <Button
                                icon="pi pi-arrow-right"
                                class="p-button-rounded p-button-outlined"
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
                        <h3>Sélectionnez une soutenance</h3>
                        <p>
                            Choisissez une soutenance dans la liste pour
                            commencer votre évaluation
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
import DatePicker from "primevue/datepicker"; // Changed from Calendar to DatePicker
import Avatar from "primevue/avatar";
import Toast from "primevue/toast";

const route = useRoute();

// Component state
const soutenances = ref([]);
const loading = ref(false);
const selectedSoutenance = ref(null);
const selectedStudent = ref(null);
const submitting = ref(false);
const searchQuery = ref("");
const selectedDate = ref(null);
const evaluationForm = ref({
    soutenanceId: null,
    etudiantId: null,
    presentationScore: 0,
    presentationScoreRating: 0, // Added separate property for Rating component
    qaScore: 0,
    qaScoreRating: 0, // Added separate property for Rating component
    timeManagementScore: 0,
    timeManagementScoreRating: 0, // Added separate property for Rating component
    commentaire: "",
});

// Get current user
const currentUser = AuthService.getCurrentUser();

// Services
const toast = useToast();

// Compute filteredSoutenances with a different approach to avoid modifying computed directly
const filteredSoutenancesValue = computed(() => {
    let filtered = [...soutenances.value];

    // Apply date filter if selected
    if (selectedDate.value) {
        const formattedSelectedDate = new Date(selectedDate.value)
            .toISOString()
            .split("T")[0];

        filtered = filtered.filter((soutenance) => {
            return soutenance.date === formattedSelectedDate;
        });
    }

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();

        filtered = filtered.filter((soutenance) => {
            const etudiant1Name =
                `${soutenance.binome?.etudiant1?.prenom} ${soutenance.binome?.etudiant1?.nom}`.toLowerCase();
            const etudiant2Name = soutenance.binome?.etudiant2
                ? `${soutenance.binome.etudiant2.prenom} ${soutenance.binome.etudiant2.nom}`.toLowerCase()
                : "";
            const salleName = soutenance.salle?.nom.toLowerCase() || "";

            return (
                etudiant1Name.includes(query) ||
                etudiant2Name.includes(query) ||
                salleName.includes(query)
            );
        });
    }

    return filtered;
});

// Fetch data on component mount
onMounted(async () => {
    await fetchSoutenances();

    // Check if we came from the soutenances overview page
    const selectedSoutenanceId = sessionStorage.getItem("selectedSoutenanceId");
    if (selectedSoutenanceId) {
        // Find and select the soutenance
        const soutenance = soutenances.value.find(
            (s) => s.id === parseInt(selectedSoutenanceId)
        );
        if (soutenance) {
            // Clear the stored ID
            sessionStorage.removeItem("selectedSoutenanceId");

            // Check if the soutenance is today
            if (isSoutenanceToday(soutenance)) {
                selectedSoutenance.value = soutenance;
            } else {
                toast.add({
                    severity: "warn",
                    summary: "Évaluation non disponible",
                    detail: "L'évaluation des soutenances n'est disponible que le jour même",
                    life: 5000,
                });
            }
        }
    }
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

// Methods for fetching data
async function fetchSoutenances() {
    loading.value = true;
    try {
        // Updated endpoint to match the backend
        const response = await ApiService.get("/grading/jury/soutenances");
        soutenances.value = response;
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des soutenances");
    } finally {
        loading.value = false;
    }
}

// Filter soutenances by date
function filterSoutenancesByDate() {
    if (!selectedDate.value) return;

    // Convert to date objects for proper comparison
    const filterDate = new Date(selectedDate.value);
    filterDate.setHours(0, 0, 0, 0);

    // Reset selections
    resetSelection();

    // Toast confirmation
    toast.add({
        severity: "info",
        summary: "Filtre appliqué",
        detail: `Affichage des soutenances du ${formatDate(
            selectedDate.value
        )}`,
        life: 3000,
    });
}

// Reset date filter
function resetDateFilter() {
    selectedDate.value = null;
    resetSelection();
}

// Reset selection
function resetSelection() {
    selectedSoutenance.value = null;
    selectedStudent.value = null;
    resetEvaluationForm();
}

// Check if soutenance has already been evaluated
function getEvaluationStatus(soutenance) {
    // In a real application, this would check if an evaluation exists already
    // For now, let's use a placeholder logic
    if (soutenance.evaluations && soutenance.evaluations.length > 0) {
        return "Partiellement évalué";
    } else {
        return "Non évalué";
    }
}

// Get severity for the status tag
function getEvaluationStatusSeverity(soutenance) {
    if (soutenance.evaluations && soutenance.evaluations.length > 0) {
        return "info";
    } else {
        return "warning";
    }
}

// Check if student has already been evaluated
function getStudentEvaluationStatus(student) {
    // In a real application, this would check if an evaluation exists already
    // For now, let's use a placeholder logic
    if (
        selectedSoutenance.value?.evaluations?.find(
            (e) => e.etudiantId === student.id
        )
    ) {
        return "Évaluation complétée";
    } else {
        return "Non évalué";
    }
}

// Handle soutenance selection
function onSoutenanceSelect(event) {
    selectedSoutenance.value = event.data;
    selectedStudent.value = null;
    resetEvaluationForm();
}

// Select a student to evaluate
function selectStudent(student) {
    selectedStudent.value = student;
    resetEvaluationForm();
    evaluationForm.value.soutenanceId = selectedSoutenance.value.id;
    evaluationForm.value.etudiantId = student.id;
    loadExistingEvaluation();
}

// Reset evaluation form
function resetEvaluationForm() {
    evaluationForm.value = {
        soutenanceId: null,
        etudiantId: null,
        presentationScore: 0,
        presentationScoreRating: 0,
        qaScore: 0,
        qaScoreRating: 0,
        timeManagementScore: 0,
        timeManagementScoreRating: 0,
        commentaire: "",
    };
}

// Load existing evaluation if any
async function loadExistingEvaluation() {
    if (!selectedSoutenance.value || !selectedStudent.value) return;

    try {
        // Updated endpoint to match the backend
        const response = await ApiService.get(
            `/grading/jury/evaluations/${selectedSoutenance.value.id}/${selectedStudent.value.id}`
        );

        if (response) {
            evaluationForm.value = {
                soutenanceId: selectedSoutenance.value.id,
                etudiantId: selectedStudent.value.id,
                presentationScore: response.presentationScore,
                presentationScoreRating: response.presentationScore / 5, // Convert score to rating value
                qaScore: response.qaScore,
                qaScoreRating: response.qaScore / 5, // Convert score to rating value
                timeManagementScore: response.timeManagementScore,
                timeManagementScoreRating: response.timeManagementScore / 5, // Convert score to rating value
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
function calculatePresentationScore() {
    evaluationForm.value.presentationScore = Math.round(
        evaluationForm.value.presentationScoreRating * 5
    );
}

function calculateQAScore() {
    evaluationForm.value.qaScore = Math.round(
        evaluationForm.value.qaScoreRating * 5
    );
}

function calculateTimeScore() {
    evaluationForm.value.timeManagementScore = Math.round(
        evaluationForm.value.timeManagementScoreRating * 5
    );
}

// Submit evaluation
async function submitEvaluation() {
    if (!validateEvaluationForm()) return;
    // Check if the soutenance is today
    if (!isSoutenanceToday(selectedSoutenance.value)) {
        toast.add({
            severity: "error",
            summary: "Action non autorisée",
            detail: "Vous ne pouvez évaluer les soutenances que le jour même",
            life: 5000,
        });
        return;
    }
    submitting.value = true;

    const payload = {
        soutenanceId: evaluationForm.value.soutenanceId,
        etudiantId: evaluationForm.value.etudiantId,
        presentationScore: evaluationForm.value.presentationScore,
        qaScore: evaluationForm.value.qaScore,
        timeManagementScore: evaluationForm.value.timeManagementScore,
        commentaire: evaluationForm.value.commentaire,
    };

    try {
        const response = await ApiService.post(
            "/grading/jury/soutenance-evaluation",
            payload
        );

        // Update soutenance in the list to mark as evaluated
        const index = soutenances.value.findIndex(
            (s) => s.id === selectedSoutenance.value.id
        );
        if (index !== -1) {
            if (!soutenances.value[index].evaluations) {
                soutenances.value[index].evaluations = [];
            }

            soutenances.value[index].evaluations.push({
                etudiantId: selectedStudent.value.id,
            });
        }

        toast.add({
            severity: "success",
            summary: "Évaluation soumise",
            detail: "Votre évaluation a été enregistrée avec succès",
            life: 3000,
        });

        // Return to student selection
        selectedStudent.value = null;
    } catch (error) {
        handleApiError(error, "Erreur lors de la soumission de l'évaluation");
    } finally {
        submitting.value = false;
    }
}

// Validate form before submission
function validateEvaluationForm() {
    if (
        !evaluationForm.value.soutenanceId ||
        !evaluationForm.value.etudiantId
    ) {
        showValidationError(
            "Erreur de sélection de la soutenance ou de l'étudiant"
        );
        return false;
    }

    if (
        evaluationForm.value.presentationScore < 0 ||
        evaluationForm.value.presentationScore > 20 ||
        evaluationForm.value.qaScore < 0 ||
        evaluationForm.value.qaScore > 20 ||
        evaluationForm.value.timeManagementScore < 0 ||
        evaluationForm.value.timeManagementScore > 20
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
//  check if a soutenance is scheduled for today
function isSoutenanceToday(soutenance) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const soutenanceDate = new Date(soutenance.date);
    soutenanceDate.setHours(0, 0, 0, 0);
    return soutenanceDate.getTime() === today.getTime();
}
</script>

<style scoped>
.jury-grading {
    width: 100%;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    flex-wrap: wrap;
    gap: 1rem;
}

.page-title {
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
    margin: 0;
}

.session-selector {
    display: flex;
    gap: 0.5rem;
}

.content-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

.soutenances-card,
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

.students-selection {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.instructions {
    font-size: 1rem;
    margin: 0.5rem 0 1rem;
    color: var(--text-color-secondary);
}

.student-option {
    display: flex;
    align-items: center;
    padding: 1rem;
    border-radius: 8px;
    border: 1px solid var(--surface-border);
    transition: all 0.2s ease;
    cursor: pointer;
    gap: 1rem;
}

.student-option:hover {
    background-color: var(--surface-hover);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.student-info {
    flex: 1;
}

.student-info h3 {
    margin: 0 0 0.25rem 0;
    font-size: 1.1rem;
}

.student-info p {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 0.9rem;
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

    .page-header {
        flex-direction: column;
        align-items: flex-start;
    }

    .session-selector {
        width: 100%;
    }
}
</style>
