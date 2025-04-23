<template>
    <div class="sujet-selection">
        <Toast />
        <ConfirmDialog />

        <div class="content-container">
            <!-- Status Display for Pending Suggestion or Already Selected Sujet -->
            <SujetStatusDisplay
                v-if="hasPendingSuggestion || hasSujet"
                :hasPendingSuggestion="hasPendingSuggestion"
                :hasSujet="hasSujet"
                :selectedSujet="selectedSujet"
                @go-to-dashboard="goToDashboard"
            />

            <!-- Sujet Selection Options -->
            <div v-else>
                <!-- Header with "Proposer un sujet" button -->
                <SujetSelectionHeader
                    :disabled="isProcessingAnyAction"
                    @open-suggestion="openSuggestionDialog"
                />

                <!-- Options for selection (manual/random) -->
                <SujetOptionsList
                    :showSujetList="showSujetList"
                    :isProcessing="isProcessingAnyAction"
                    @toggle-sujet-list="toggleSujetList"
                    @random-assignment="confirmRandomSujet"
                />

                <!-- List of available subjects -->
                <SujetsList
                    :sujets="availableSujets"
                    :showSujetList="showSujetList"
                    :processingSubjectId="processingSubjectId"
                    :isProcessingAnyAction="isProcessingAnyAction"
                    @select-sujet="confirmSelectSujet"
                />
            </div>
        </div>

        <!-- Suggestion Modal Dialog -->
        <SujetSuggestionModal
            v-model:visible="showSuggestionDialog"
            :submitting="processingSuggestion"
            @submit="submitSuggestion"
            @cancel="closeSuggestionDialog"
        />
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";

// Custom components
import SujetSelectionHeader from "@/components/etudiant/sujetSelection/SujetSelectionHeader.vue";
import SujetOptionsList from "@/components/etudiant/sujetSelection/SujetOptionsList.vue";
import SujetsList from "@/components/etudiant/sujetSelection/SujetsList.vue";
import SujetSuggestionModal from "@/components/etudiant/sujetSelection/SujetSuggestionModal.vue";
import SujetStatusDisplay from "@/components/etudiant/sujetSelection/SujetStatusDisplay.vue";

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
const showSuggestionDialog = ref(false);

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
    } catch (error) {
        handleError(error, "Erreur lors de l'attribution aléatoire du sujet");
    } finally {
        processingRandom.value = false;
    }
};

// Submit a sujet suggestion
const submitSuggestion = async (formData) => {
    processingSuggestion.value = true;
    try {
        await ApiService.post("/etudiant/sujet/suggest", formData);

        // Update local state
        hasPendingSuggestion.value = true;

        toast.add({
            severity: "success",
            summary: "Suggestion envoyée",
            detail: "Votre suggestion de sujet a été soumise et est en attente d'approbation",
            life: 3000,
        });

        // Close dialog
        showSuggestionDialog.value = false;
    } catch (error) {
        handleError(error, "Erreur lors de la soumission de la suggestion");
    } finally {
        processingSuggestion.value = false;
    }
};

// UI control methods
const toggleSujetList = () => {
    showSujetList.value = !showSujetList.value;
};

// Modal control methods
const openSuggestionDialog = () => {
    showSuggestionDialog.value = true;
};

const closeSuggestionDialog = () => {
    showSuggestionDialog.value = false;
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
    margin: 0 auto;
}

.content-container {
    margin-top: 2rem;
}
</style>
