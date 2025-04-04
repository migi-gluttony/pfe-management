<template>
    <div class="etudiant-view">
        <Toast />
        <ConfirmDialog />

        <div class="view-container">
            <div class="card">
                <div class="card-header">
                    <h1>Choisir un Sujet de PFE</h1>
                    <p v-if="hasPendingSuggestion">
                        Vous avez une suggestion de sujet en attente.
                    </p>
                    <p v-else-if="hasSujet">
                        Vous avez déjà choisi un sujet pour votre PFE.
                    </p>
                    <p v-else>
                        Choisissez un sujet pour votre projet de fin d'études.
                    </p>
                </div>

                <div class="card-content">
                    <div v-if="loading" class="loading-container">
                        <ProgressSpinner />
                        <p>Chargement des données...</p>
                    </div>

                    <!-- Has pending suggestion -->
                    <div
                        v-else-if="hasPendingSuggestion"
                        class="pending-suggestion-container"
                    >
                        <i class="pi pi-clock"></i>
                        <h2>Suggestion en attente</h2>
                        <p>
                            Votre suggestion de sujet est en cours d'évaluation.
                            Veuillez patienter jusqu'à ce qu'elle soit traitée.
                        </p>
                        <p class="secondary-text">
                            Vous serez notifié lorsque votre suggestion sera
                            acceptée ou refusée.
                        </p>
                    </div>

                    <!-- Already has selected sujet -->
                    <div v-else-if="hasSujet" class="selected-sujet-container">
                        <i class="pi pi-check-circle"></i>
                        <h2>Sujet sélectionné</h2>
                        <div v-if="selectedSujet" class="selected-sujet-card">
                            <h3>{{ selectedSujet.titre }}</h3>
                            <div class="sujet-meta">
                                <span class="theme-tag">{{
                                    selectedSujet.theme
                                }}</span>
                                <span class="filiere-tag">{{
                                    selectedSujet.filiereName
                                }}</span>
                            </div>
                            <p class="description">
                                {{ selectedSujet.description }}
                            </p>
                        </div>
                        <p v-else>
                            Vous avez déjà choisi un sujet pour votre PFE.
                        </p>
                        <Button
                            label="Retour au tableau de bord"
                            icon="pi pi-home"
                            @click="goToDashboard"
                            class="mt-4"
                        />
                    </div>

                    <!-- Choose sujet options -->
                    <div v-else class="choose-sujet-container">
                        <!-- Option cards -->
                        <div class="option-cards">
                            <!-- Option 1: Choose from list -->
                            <div
                                class="option-card"
                                @click="toggleSujetList"
                                :class="{ 'option-active': showSujetList }"
                            >
                                <div class="option-icon">
                                    <i class="pi pi-list"></i>
                                </div>
                                <div class="option-content">
                                    <h3>Choisir un sujet</h3>
                                    <p>
                                        Sélectionnez un sujet parmi les
                                        propositions disponibles
                                    </p>
                                </div>
                            </div>

                            <!-- Option 2: Random assignment -->
                            <div
                                class="option-card"
                                @click="confirmRandomSujet"
                            >
                                <div class="option-icon">
                                    <i class="pi pi-sync"></i>
                                </div>
                                <div class="option-content">
                                    <h3>Attribution aléatoire</h3>
                                    <p>
                                        Obtenez un sujet attribué aléatoirement
                                    </p>
                                </div>
                            </div>

                            <!-- Option 3: Suggest new sujet -->
                            <div
                                class="option-card"
                                @click="toggleSuggestionForm"
                                :class="{ 'option-active': showSuggestionForm }"
                            >
                                <div class="option-icon">
                                    <i class="pi pi-plus-circle"></i>
                                </div>
                                <div class="option-content">
                                    <h3>Proposer un sujet</h3>
                                    <p>
                                        Suggérez votre propre sujet pour
                                        approbation
                                    </p>
                                </div>
                            </div>
                        </div>

                        <!-- Sujet list (Option 1) -->
                        <div v-if="showSujetList" class="sujet-list-container">
                            <h3>Sujets disponibles</h3>
                            <div
                                v-if="availableSujets.length === 0"
                                class="empty-state"
                            >
                                <i class="pi pi-file"></i>
                                <p>Aucun sujet disponible pour votre filière</p>
                            </div>
                            <div v-else class="sujet-list">
                                <div
                                    v-for="sujet in availableSujets"
                                    :key="sujet.id"
                                    class="sujet-card"
                                >
                                    <div class="sujet-header">
                                        <h4>{{ sujet.titre }}</h4>
                                        <div class="sujet-meta">
                                            <span class="theme-tag">{{
                                                sujet.theme
                                            }}</span>
                                            <span class="filiere-tag">{{
                                                sujet.filiereName
                                            }}</span>
                                        </div>
                                    </div>
                                    <p class="description">
                                        {{ sujet.description }}
                                    </p>
                                    <div class="sujet-actions">
                                        <Button
                                            label="Sélectionner ce sujet"
                                            icon="pi pi-check"
                                            @click="confirmSelectSujet(sujet)"
                                            :loading="
                                                processingSubjectId === sujet.id
                                            "
                                            :disabled="isProcessingAnyAction"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Suggestion form (Option 3) -->
                        <div
                            v-if="showSuggestionForm"
                            class="suggestion-form-container"
                        >
                            <h3>Proposer un nouveau sujet</h3>
                            <div class="form-group">
                                <label for="titre"
                                    >Titre du sujet<span class="required"
                                        >*</span
                                    ></label
                                >
                                <InputText
                                    id="titre"
                                    v-model="suggestion.titre"
                                    class="w-full"
                                    :class="{ 'p-invalid': formErrors.titre }"
                                    placeholder="Entrez un titre concis et descriptif"
                                />
                                <small
                                    v-if="formErrors.titre"
                                    class="p-error"
                                    >{{ formErrors.titre }}</small
                                >
                            </div>

                            <div class="form-group">
                                <label for="theme"
                                    >Thème<span class="required">*</span></label
                                >
                                <InputText
                                    id="theme"
                                    v-model="suggestion.theme"
                                    class="w-full"
                                    :class="{ 'p-invalid': formErrors.theme }"
                                    placeholder="Ex: Intelligence Artificielle, IoT, Sécurité..."
                                />
                                <small
                                    v-if="formErrors.theme"
                                    class="p-error"
                                    >{{ formErrors.theme }}</small
                                >
                            </div>

                            <div class="form-group">
                                <label for="description"
                                    >Description détaillée<span class="required"
                                        >*</span
                                    ></label
                                >
                                <Textarea
                                    id="description"
                                    v-model="suggestion.description"
                                    rows="6"
                                    class="w-full"
                                    :class="{
                                        'p-invalid': formErrors.description,
                                    }"
                                    placeholder="Décrivez le sujet, les objectifs, les technologies envisagées..."
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
                                    label="Soumettre la suggestion"
                                    icon="pi pi-send"
                                    @click="submitSuggestion"
                                    :loading="processingSuggestion"
                                    :disabled="isProcessingAnyAction"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import ProgressSpinner from "primevue/progressspinner";

export default {
    name: "SujetSelectionView",
    components: {
        Toast,
        ConfirmDialog,
        Button,
        InputText,
        Textarea,
        ProgressSpinner,
    },
    setup() {
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
                    hasPendingSuggestion.value =
                        status.hasPendingSujetSuggestion;

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
                const response = await ApiService.get(
                    "/etudiant/sujet/available"
                );
                if (response.selectedSujet) {
                    selectedSujet.value = response.selectedSujet;
                }
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors du chargement du sujet sélectionné"
                );
            }
        };

        // Load available sujets for selection
        const loadAvailableSujets = async () => {
            try {
                const response = await ApiService.get(
                    "/etudiant/sujet/available"
                );
                availableSujets.value = response.availableSujets || [];
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors du chargement des sujets disponibles"
                );
            }
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
                const response = await ApiService.post(
                    "/etudiant/sujet/random"
                );

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
                handleError(
                    error,
                    "Erreur lors de l'attribution aléatoire du sujet"
                );
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
                await ApiService.post(
                    "/etudiant/sujet/suggest",
                    suggestion.value
                );

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
                handleError(
                    error,
                    "Erreur lors de la soumission de la suggestion"
                );
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
                formErrors.value.titre =
                    "Le titre doit contenir au moins 5 caractères";
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

        return {
            // State
            loading,
            hasBinome,
            hasSujet,
            hasPendingSuggestion,
            selectedSujet,
            availableSujets,
            suggestion,
            formErrors,

            // UI state
            showSujetList,
            showSuggestionForm,

            // Processing states
            processingSubjectId,
            processingRandom,
            processingSuggestion,
            isProcessingAnyAction,

            // Methods
            toggleSujetList,
            toggleSuggestionForm,
            confirmSelectSujet,
            confirmRandomSujet,
            submitSuggestion,
            cancelSuggestion,
            goToDashboard,
        };
    },
};
</script>

<style scoped>
.etudiant-view {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.view-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.card {
    background-color: var(--surface-card);
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.card-header {
    padding: 24px;
    background-color: var(--primary-color);
    color: white;
}

.card-header h1 {
    margin: 0 0 8px;
    font-size: 1.75rem;
}

.card-header p {
    margin: 0;
    opacity: 0.9;
}

.card-content {
    padding: 24px;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
}

.loading-container p {
    margin-top: 16px;
    color: var(--text-color-secondary);
}

/* Status containers */
.pending-suggestion-container,
.selected-sujet-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 30px 20px;
}

.pending-suggestion-container i,
.selected-sujet-container i {
    font-size: 3rem;
    color: var(--primary-color);
    margin-bottom: 16px;
}

.pending-suggestion-container h2,
.selected-sujet-container h2 {
    margin: 0 0 16px;
    color: var(--text-color);
}

.pending-suggestion-container p,
.selected-sujet-container p {
    margin: 0 0 8px;
    max-width: 600px;
}

.secondary-text {
    color: var(--text-color-secondary);
    font-size: 0.9rem;
}

/* Selected sujet card */
.selected-sujet-card {
    width: 100%;
    max-width: 800px;
    background-color: var(--surface-hover);
    border-radius: 8px;
    padding: 20px;
    margin-top: 16px;
    text-align: left;
}

.selected-sujet-card h3 {
    margin: 0 0 12px;
    color: var(--primary-color);
}

.sujet-meta {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    flex-wrap: wrap;
}

.theme-tag {
    background-color: var(--primary-color);
    color: white;
    padding: 4px 10px;
    border-radius: 16px;
    font-size: 0.85rem;
}

.filiere-tag {
    background-color: var(--surface-d);
    color: var(--text-color);
    padding: 4px 10px;
    border-radius: 16px;
    font-size: 0.85rem;
}

/* Option cards */
.option-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
}

.option-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background-color: var(--surface-section);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 2px solid transparent;
}

.option-card:hover {
    background-color: var(--surface-hover);
    transform: translateY(-2px);
}

.option-active {
    border-color: var(--primary-color);
    background-color: var(--primary-50);
}

.option-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 50px;
    height: 50px;
    background-color: var(--primary-color);
    border-radius: 50%;
    margin-right: 16px;
}

.option-icon i {
    color: white;
    font-size: 1.5rem;
}

.option-content h3 {
    margin: 0 0 4px;
    font-size: 1.1rem;
}

.option-content p {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 0.9rem;
}

/* Sujet list */
.sujet-list-container {
    margin-top: 20px;
}

.sujet-list-container h3 {
    margin: 0 0 16px;
    color: var(--primary-color);
    font-size: 1.2rem;
}

.sujet-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 40px 20px;
    background-color: var(--surface-hover);
    border-radius: 8px;
}

.empty-state i {
    font-size: 2rem;
    color: var(--text-color-secondary);
    margin-bottom: 16px;
}

.empty-state p {
    margin: 0;
    color: var(--text-color-secondary);
}

.sujet-card {
    background-color: var(--surface-hover);
    border-radius: 8px;
    padding: 20px;
    transition: all 0.2s ease;
}

.sujet-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.sujet-header {
    margin-bottom: 12px;
}

.sujet-header h4 {
    margin: 0 0 8px;
    color: var(--primary-color);
}

.description {
    margin: 0 0 16px;
    color: var(--text-color);
    line-height: 1.5;
}

.sujet-actions {
    display: flex;
    justify-content: flex-end;
}

/* Suggestion form */
.suggestion-form-container {
    margin-top: 20px;
}

.suggestion-form-container h3 {
    margin: 0 0 20px;
    color: var(--primary-color);
    font-size: 1.2rem;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
}

.required {
    color: red;
    margin-left: 4px;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
}

/* Utility classes */
.w-full {
    width: 100%;
}

.mt-4 {
    margin-top: 1rem;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .option-cards {
        grid-template-columns: 1fr;
    }

    .form-actions {
        flex-direction: column-reverse;
    }

    .form-actions button {
        width: 100%;
    }
}
</style>
