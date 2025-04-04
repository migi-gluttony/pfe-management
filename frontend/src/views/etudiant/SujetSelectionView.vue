<template>
    <div class="sujet-selection">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader
            title="Sélection de Sujet de PFE"
            subtitle="Choisissez un sujet pour votre projet de fin d'études"
        />

        <div class="content-container">
            <Card>
                <template #header>
                    <div class="card-header-custom">
                        <i class="pi pi-file-edit"></i>
                        <h2>Sujet de Projet de Fin d'Études</h2>
                    </div>
                </template>

                <template #content>
                    <div v-if="loading" class="loading-container">
                        <ProgressSpinner />
                        <p>Chargement des données...</p>
                    </div>

                    <!-- Has pending suggestion -->
                    <div
                        v-else-if="hasPendingSuggestion"
                        class="status-container pending-container"
                    >
                        <div class="status-icon">
                            <i class="pi pi-clock"></i>
                        </div>
                        <div class="status-content">
                            <h3>Suggestion en attente d'approbation</h3>
                            <p>
                                Votre suggestion de sujet est en cours
                                d'évaluation par le chef de département.
                            </p>
                            <Tag severity="warning" value="En attente" />
                        </div>
                    </div>

                    <!-- Already has selected sujet -->
                    <div
                        v-else-if="hasSujet"
                        class="status-container selected-container"
                    >
                        <div class="status-icon">
                            <i class="pi pi-check-circle"></i>
                        </div>
                        <div class="status-content">
                            <h3>Sujet sélectionné avec succès</h3>

                            <div
                                v-if="selectedSujet"
                                class="selected-sujet-details"
                            >
                                <h4>{{ selectedSujet.titre }}</h4>

                                <div class="sujet-tags">
                                    <Tag
                                        severity="info"
                                        :value="selectedSujet.theme"
                                    />
                                    <Tag
                                        severity="secondary"
                                        :value="selectedSujet.filiereName"
                                    />
                                </div>

                                <div class="description-section">
                                    <h5>Description</h5>
                                    <p>{{ selectedSujet.description }}</p>
                                </div>
                            </div>

                            <Button
                                label="Retour au tableau de bord"
                                icon="pi pi-home"
                                class="mt-3"
                                @click="goToDashboard"
                            />
                        </div>
                    </div>

                    <!-- Choose sujet options -->
                    <div v-else>
                        <h3 class="section-title">Choisissez une option</h3>

                        <!-- Option cards -->
                        <div class="option-cards">
                            <!-- Option 1: Choose from list -->
                            <div
                                class="option-card"
                                @click="toggleSujetList"
                                :class="{ 'option-active': showSujetList }"
                            >
                                <div class="option-icon bg-primary">
                                    <i class="pi pi-list"></i>
                                </div>
                                <div class="option-content">
                                    <h4>Sélection manuelle</h4>
                                    <p>
                                        Choisissez un sujet parmi la liste des
                                        propositions disponibles
                                    </p>
                                </div>
                            </div>

                            <!-- Option 2: Random assignment -->
                            <div
                                class="option-card"
                                @click="confirmRandomSujet"
                            >
                                <div class="option-icon bg-danger">
                                    <i class="pi pi-sync"></i>
                                </div>
                                <div class="option-content">
                                    <h4>Attribution aléatoire</h4>
                                    <p>
                                        Un sujet vous sera attribué au hasard
                                        parmi ceux disponibles
                                    </p>
                                </div>
                            </div>

                            <!-- Option 3: Suggest new sujet -->
                            <div
                                class="option-card"
                                @click="toggleSuggestionForm"
                                :class="{ 'option-active': showSuggestionForm }"
                            >
                                <div class="option-icon bg-success">
                                    <i class="pi pi-plus-circle"></i>
                                </div>
                                <div class="option-content">
                                    <h4>Proposer un sujet</h4>
                                    <p>
                                        Suggérez votre propre sujet pour
                                        approbation par votre encadrant
                                    </p>
                                </div>
                            </div>
                        </div>

                        <!-- Sujet list (Option 1) -->
                        <div v-if="showSujetList" class="sujets-container">
                            <div class="section-header">
                                <h3>Sujets disponibles</h3>
                                <div class="search-container">
                                    <span class="p-input-icon-left">
                                        <i class="pi pi-search" />
                                        <InputText
                                            v-model="searchTerm"
                                            placeholder="Rechercher un sujet..."
                                        />
                                    </span>
                                </div>
                            </div>

                            <div
                                v-if="filteredSujets.length === 0"
                                class="empty-state"
                            >
                                <i class="pi pi-file-o empty-icon"></i>
                                <h3>Aucun sujet disponible</h3>
                                <p>
                                    Aucun sujet n'est disponible pour votre
                                    filière actuellement.
                                </p>
                            </div>

                            <div v-else class="sujet-cards">
                                <Card
                                    v-for="sujet in filteredSujets"
                                    :key="sujet.id"
                                    class="sujet-card"
                                >
                                    <template #header>
                                        <div class="sujet-card-header">
                                            <h4>{{ sujet.titre }}</h4>
                                        </div>
                                    </template>
                                    <template #content>
                                        <div class="sujet-tags">
                                            <Tag
                                                severity="info"
                                                :value="sujet.theme"
                                            />
                                            <Tag
                                                severity="secondary"
                                                :value="sujet.filiereName"
                                            />
                                        </div>

                                        <div class="description-preview">
                                            <p>
                                                {{
                                                    truncateDescription(
                                                        sujet.description
                                                    )
                                                }}
                                            </p>
                                        </div>

                                        <Button
                                            label="Sélectionner ce sujet"
                                            icon="pi pi-check"
                                            class="p-button-primary mt-3 w-full"
                                            @click="confirmSelectSujet(sujet)"
                                            :loading="
                                                processingSubjectId === sujet.id
                                            "
                                            :disabled="isProcessingAnyAction"
                                        />
                                    </template>
                                </Card>
                            </div>
                        </div>

                        <!-- Suggestion form (Option 3) -->
                        <div
                            v-if="showSuggestionForm"
                            class="suggestion-form-container"
                        >
                            <h3 class="section-title">
                                Proposer un nouveau sujet
                            </h3>

                            <div class="form-container">
                                <div class="p-fluid">
                                    <div class="field">
                                        <label for="titre"
                                            >Titre du sujet<span
                                                class="required-field"
                                                >*</span
                                            ></label
                                        >
                                        <InputText
                                            id="titre"
                                            v-model="suggestion.titre"
                                            :class="{
                                                'p-invalid': formErrors.titre,
                                            }"
                                            placeholder="Titre concis et descriptif du projet"
                                        />
                                        <small
                                            v-if="formErrors.titre"
                                            class="p-error"
                                            >{{ formErrors.titre }}</small
                                        >
                                    </div>

                                    <div class="field">
                                        <label for="theme"
                                            >Thème du sujet<span
                                                class="required-field"
                                                >*</span
                                            ></label
                                        >
                                        <InputText
                                            id="theme"
                                            v-model="suggestion.theme"
                                            :class="{
                                                'p-invalid': formErrors.theme,
                                            }"
                                            placeholder="Ex: Intelligence Artificielle, Web, Mobile..."
                                        />
                                        <small
                                            v-if="formErrors.theme"
                                            class="p-error"
                                            >{{ formErrors.theme }}</small
                                        >
                                    </div>

                                    <div class="field">
                                        <label for="description"
                                            >Description détaillée<span
                                                class="required-field"
                                                >*</span
                                            ></label
                                        >
                                        <Textarea
                                            id="description"
                                            v-model="suggestion.description"
                                            rows="6"
                                            :class="{
                                                'p-invalid':
                                                    formErrors.description,
                                            }"
                                            placeholder="Décrivez le but du projet, les objectifs, les technologies envisagées..."
                                            autoResize
                                        />
                                        <small
                                            v-if="formErrors.description"
                                            class="p-error"
                                            >{{ formErrors.description }}</small
                                        >
                                    </div>

                                    <div class="form-actions">
                                        <Button
                                            label="Annuler"
                                            icon="pi pi-times"
                                            class="p-button-outlined p-button-secondary"
                                            @click="cancelSuggestion"
                                            :disabled="isProcessingAnyAction"
                                        />
                                        <Button
                                            label="Soumettre"
                                            icon="pi pi-send"
                                            class="p-button-primary"
                                            @click="submitSuggestion"
                                            :loading="processingSuggestion"
                                            :disabled="isProcessingAnyAction"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </Card>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// Components
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import Card from "primevue/card";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import Tag from "primevue/tag";
import ProgressSpinner from "primevue/progressspinner";
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";

// Router and services
const router = useRouter();
const toast = useToast();
const confirm = useConfirm();

// State management
const loading = ref(false);
const hasBinome = ref(false);
const hasSujet = ref(false);
const hasPendingSuggestion = ref(false);
const binomeId = ref(null);
const selectedSujet = ref(null);
const availableSujets = ref([]);
const searchTerm = ref("");

// UI state
const showSujetList = ref(false);
const showSuggestionForm = ref(false);

// Processing states
const processingSubjectId = ref(null);
const processingRandom = ref(false);
const processingSuggestion = ref(false);

// Computed properties
const isProcessingAnyAction = computed(() => {
    return (
        processingSubjectId.value !== null ||
        processingRandom.value ||
        processingSuggestion.value
    );
});

// Filter sujets based on search term
const filteredSujets = computed(() => {
    if (!searchTerm.value) return availableSujets.value;

    const term = searchTerm.value.toLowerCase();
    return availableSujets.value.filter(
        (sujet) =>
            sujet.titre.toLowerCase().includes(term) ||
            sujet.theme.toLowerCase().includes(term) ||
            sujet.description.toLowerCase().includes(term)
    );
});

// Form data for suggestion
const suggestion = ref({
    titre: "",
    theme: "",
    description: "",
});

// Form validation errors
const formErrors = ref({
    titre: "",
    theme: "",
    description: "",
});

// Check student status on mount
onMounted(async () => {
    await checkStudentStatus();
});

// Methods
// Check if student has a binome and sujet
const checkStudentStatus = async () => {
    loading.value = true;
    try {
        const status = await ApiService.get("/etudiant/status");

        hasBinome.value = status.hasBinome;
        hasSujet.value = status.hasSujet;
        binomeId.value = status.binomeId;

        // If no binome, redirect to binome selection
        if (!hasBinome.value) {
            router.push("/etudiant/binome");
            return;
        }

        // If already has sujet, load it
        if (hasSujet.value) {
            // Load the selected sujet
            await loadSelectedSujet();
        } else {
            // Check for pending suggestion
            hasPendingSuggestion.value = status.hasPendingSujetSuggestion;

            // If no pending suggestion, load available sujets
            if (!hasPendingSuggestion.value) {
                await loadAvailableSujets();
            }
        }
    } catch (error) {
        handleError(error, "Erreur lors de la vérification du statut");
    } finally {
        loading.value = false;
    }
};

// Load the current selected sujet if any
const loadSelectedSujet = async () => {
    try {
        const response = await ApiService.get("/etudiant/sujet/available");
        if (response.selectedSujet) {
            selectedSujet.value = response.selectedSujet;
        }
    } catch (error) {
        handleError(error, "Erreur lors du chargement du sujet sélectionné");
    }
};

// Load available sujets for selection
const loadAvailableSujets = async () => {
    try {
        const response = await ApiService.get("/etudiant/sujet/available");
        availableSujets.value = response.availableSujets || [];
    } catch (error) {
        handleError(error, "Erreur lors du chargement des sujets disponibles");
    }
};

// Truncate description for preview
const truncateDescription = (text, length = 150) => {
    if (!text) return "";
    if (text.length <= length) return text;
    return text.substring(0, length) + "...";
};

// Select a specific sujet
const selectSujet = async (sujetId) => {
    processingSubjectId.value = sujetId;
    try {
        const response = await ApiService.post(
            `/etudiant/sujet/select/${sujetId}`
        );

        // Update local state
        hasSujet.value = true;
        selectedSujet.value = response;

        toast.add({
            severity: "success",
            summary: "Sujet sélectionné",
            detail: "Vous avez choisi votre sujet de PFE avec succès",
            life: 3000,
        });

        // Reset UI state
        showSujetList.value = false;
    } catch (error) {
        handleError(error, "Erreur lors de la sélection du sujet");
    } finally {
        processingSubjectId.value = null;
    }
};

// Get a random sujet
const getRandomSujet = async () => {
    processingRandom.value = true;
    try {
        const response = await ApiService.post("/etudiant/sujet/random");

        // Update local state
        hasSujet.value = true;
        selectedSujet.value = response;

        toast.add({
            severity: "success",
            summary: "Sujet attribué",
            detail: "Un sujet de PFE vous a été attribué aléatoirement",
            life: 3000,
        });

        // Reset UI state
        showSujetList.value = false;
        showSuggestionForm.value = false;
    } catch (error) {
        handleError(error, "Erreur lors de l'attribution aléatoire du sujet");
    } finally {
        processingRandom.value = false;
    }
};

// Submit a sujet suggestion
const submitSuggestion = async () => {
    // Validate form
    if (!validateSuggestionForm()) {
        return;
    }

    processingSuggestion.value = true;
    try {
        await ApiService.post("/etudiant/sujet/suggest", suggestion.value);

        // Update local state
        hasPendingSuggestion.value = true;

        toast.add({
            severity: "success",
            summary: "Suggestion envoyée",
            detail: "Votre suggestion de sujet a été soumise et est en attente d'approbation",
            life: 3000,
        });

        // Reset form
        resetSuggestionForm();
        showSuggestionForm.value = false;
    } catch (error) {
        handleError(error, "Erreur lors de la soumission de la suggestion");
    } finally {
        processingSuggestion.value = false;
    }
};

// Validate the suggestion form
const validateSuggestionForm = () => {
    let isValid = true;

    // Reset errors
    formErrors.value = {
        titre: "",
        theme: "",
        description: "",
    };

    // Validate titre
    if (!suggestion.value.titre.trim()) {
        formErrors.value.titre = "Le titre est obligatoire";
        isValid = false;
    } else if (suggestion.value.titre.length < 5) {
        formErrors.value.titre = "Le titre doit contenir au moins 5 caractères";
        isValid = false;
    }

    // Validate theme
    if (!suggestion.value.theme.trim()) {
        formErrors.value.theme = "Le thème est obligatoire";
        isValid = false;
    }

    // Validate description
    if (!suggestion.value.description.trim()) {
        formErrors.value.description = "La description est obligatoire";
        isValid = false;
    } else if (suggestion.value.description.length < 50) {
        formErrors.value.description =
            "La description doit contenir au moins 50 caractères";
        isValid = false;
    }

    return isValid;
};

// Reset the suggestion form
const resetSuggestionForm = () => {
    suggestion.value = {
        titre: "",
        theme: "",
        description: "",
    };

    formErrors.value = {
        titre: "",
        theme: "",
        description: "",
    };
};

// UI control methods
const toggleSujetList = () => {
    showSujetList.value = !showSujetList.value;
    showSuggestionForm.value = false;
};

const toggleSuggestionForm = () => {
    showSuggestionForm.value = !showSuggestionForm.value;
    showSujetList.value = false;
};

const goToDashboard = () => {
    router.push("/dashboard");
};

// Confirm selecting a specific sujet
const confirmSelectSujet = (sujet) => {
    confirm.require({
        message: `Êtes-vous sûr de vouloir choisir le sujet "${sujet.titre}" ? Cette action est irréversible.`,
        header: "Confirmation de sélection",
        icon: "pi pi-info-circle",
        acceptClass: "p-button-primary",
        accept: () => selectSujet(sujet.id),
        reject: () => {},
    });
};

// Confirm random sujet assignment
const confirmRandomSujet = () => {
    confirm.require({
        message:
            "Êtes-vous sûr de vouloir qu'un sujet vous soit attribué aléatoirement ? Cette action est irréversible.",
        header: "Confirmation d'attribution aléatoire",
        icon: "pi pi-question-circle",
        acceptClass: "p-button-primary",
        accept: () => getRandomSujet(),
        reject: () => {},
    });
};

const cancelSuggestion = () => {
    resetSuggestionForm();
    showSuggestionForm.value = false;
};

// Error handling
const handleError = (error, defaultMessage) => {
    console.error(defaultMessage, error);
    let message = defaultMessage;

    if (error.message) {
        message = error.message;
    } else if (
        error.response &&
        error.response.data &&
        error.response.data.message
    ) {
        message = error.response.data.message;
    }

    toast.add({
        severity: "error",
        summary: "Erreur",
        detail: message,
        life: 5000,
    });
};
</script>

<style scoped>
.sujet-selection {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
}

.content-container {
    margin-top: 2rem;
}

/* Card Header */
.card-header-custom {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem;
    background-color: var(--primary-color);
    color: white;
    border-radius: 6px 6px 0 0;
}

.card-header-custom i {
    font-size: 1.5rem;
}

.card-header-custom h2 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
}

/* Loading container */
.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 0;
}

.loading-container p {
    margin-top: 1rem;
    color: var(--text-color-secondary);
}

/* Status containers */
.status-container {
    display: flex;
    align-items: flex-start;
    padding: 1.5rem;
    border-radius: 8px;
    margin-bottom: 1rem;
}

.pending-container {
    background-color: var(--yellow-50);
    border-left: 4px solid var(--yellow-500);
}

.selected-container {
    background-color: var(--green-50);
    border-left: 4px solid var(--green-500);
}

.status-icon {
    font-size: 2rem;
    margin-right: 1rem;
    color: var(--primary-color);
}

.pending-container .status-icon {
    color: var(--yellow-500);
}

.selected-container .status-icon {
    color: var(--green-500);
}

.status-content {
    flex: 1;
}

.status-content h3 {
    margin: 0 0 0.75rem;
    font-size: 1.25rem;
    color: var(--text-color);
}

.status-content p {
    margin: 0 0 1rem;
    color: var(--text-color-secondary);
}

/* Selected sujet details */
.selected-sujet-details {
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    margin: 1rem 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.selected-sujet-details h4 {
    margin: 0 0 1rem;
    font-size: 1.2rem;
    color: var(--primary-color);
}

.sujet-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.description-section h5 {
    margin: 0 0 0.5rem;
    font-size: 1rem;
    color: var(--text-color);
}

.description-section p {
    margin: 0;
    line-height: 1.5;
}

/* Section headers */
.section-title {
    margin: 0 0 1.5rem;
    color: var(--primary-color);
    font-size: 1.25rem;
    font-weight: 600;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
}

.section-header h3 {
    margin: 0;
    font-size: 1.25rem;
    color: var(--primary-color);
    font-weight: 600;
}

/* Option cards */
.option-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.option-card {
    display: flex;
    align-items: center;
    padding: 1.5rem;
    background-color: var(--surface-card);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 2px solid transparent;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.option-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.option-active {
    border-color: var(--primary-color);
    background-color: rgba(var(--primary-color-rgb), 0.05);
}

.option-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    margin-right: 1rem;
    color: white;
}

.bg-primary {
    background-color: var(--primary-color);
}

.bg-success {
    background-color: var(--green-500);
}

.bg-danger {
    background-color: var(--red-500);
}

.option-content h4 {
    margin: 0 0 0.5rem;
    font-size: 1.1rem;
    color: var(--text-color);
}

.option-content p {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 0.9rem;
}

/* Sujets container */
.sujets-container {
    margin-top: 2rem;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 3rem 0;
    background-color: var(--surface-hover);
    border-radius: 8px;
}

.empty-state .empty-icon {
    font-size: 3rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}

.empty-state h3 {
    margin: 0 0 0.5rem;
    color: var(--text-color);
    font-size: 1.25rem;
}

.empty-state p {
    margin: 0;
    color: var(--text-color-secondary);
}

/* Sujet cards */
.sujet-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
}

.sujet-card {
    transition: transform 0.2s, box-shadow 0.2s;
}

.sujet-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.sujet-card-header {
    padding: 1rem;
    background-color: var(--primary-color);
    color: white;
}

.sujet-card-header h4 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 500;
}

.description-preview {
    margin: 1rem 0;
    color: var(--text-color);
    line-height: 1.5;
}

/* Suggestion form */
.suggestion-form-container {
    margin-top: 2rem;
}

.form-container {
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.field {
    margin-bottom: 1.5rem;
}

.field label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.required-field {
    color: var(--red-500);
    margin-left: 0.25rem;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1.5rem;
}

/* Utility classes */
.p-fluid .p-inputtext {
    width: 100%;
}

.mt-3 {
    margin-top: 0.75rem;
}

.w-full {
    width: 100%;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.75rem;
    }

    .search-container {
        width: 100%;
    }

    .search-container .p-input-icon-left {
        width: 100%;
    }

    .search-container input {
        width: 100%;
    }

    .option-cards {
        grid-template-columns: 1fr;
    }

    .sujet-cards {
        grid-template-columns: 1fr;
    }

    .form-actions {
        flex-direction: column-reverse;
    }

    .form-actions button {
        width: 100%;
    }

    .status-container {
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    .status-icon {
        margin-right: 0;
        margin-bottom: 1rem;
    }
}
</style>
